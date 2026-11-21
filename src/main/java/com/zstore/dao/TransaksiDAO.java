package com.zstore.dao;

import com.zstore.config.DBConnection;
import com.zstore.config.PaymentConfig;
import com.zstore.model.DetailTransaksi;
import com.zstore.model.Invoice;
import com.zstore.model.Transaksi;
import com.zstore.payment.SumopodClient;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransaksiDAO {

    private static final String BASE_SELECT =
            "SELECT t.*, pl.Nama_Pelanggan, "
            + "i.Provider AS Invoice_Provider, i.Provider_Payment_ID, i.Payment_Link, "
            + "i.External_ID, i.Fee, i.Net_Amount, i.Paid_At, i.Expires_At, i.Status AS Invoice_Status "
            + "FROM Transaksi t "
            + "LEFT JOIN Pelanggan pl ON pl.ID_Pelanggan = t.ID_Pelanggan "
            + "LEFT JOIN Invoice i ON i.ID_Invoice = t.ID_Invoice ";

    private final SumopodClient sumopodClient = new SumopodClient();

    /**
     * Atomically creates an Invoice, Transaksi, Detail_Transaksi rows, claims available Stocks,
     * and creates a Sumopod QRIS payment when Sumopod API credentials are configured.
     * Only Sumopod QRIS payment is supported.
     */
    public long createCheckout(Long idPelanggan, String buyerName, String buyerEmail,
                               long idProduk, int qty, String paymentMethod, int discountPercent)
            throws SQLException {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        long idTrx;
        long idInvoice;
        long subtotal;
        String orderId = "ZST-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        int expireMinutes = PaymentConfig.getExpireMinutes();
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + expireMinutes * 60L * 1000L);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int basePrice;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT Harga_produk FROM Produk WHERE ID_Produk = ?")) {
                    ps.setLong(1, idProduk);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalStateException("Produk tidak ditemukan");
                        }
                        basePrice = rs.getInt("Harga_produk");
                    }
                }

                int unitPrice = basePrice;
                if (discountPercent > 0 && discountPercent < 100) {
                    unitPrice = (int) Math.round(basePrice * (100.0 - discountPercent) / 100.0);
                }
                subtotal = (long) unitPrice * qty;

                List<Long> stockIds = new ArrayList<>();
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ID_Stk FROM Stocks WHERE ID_Produk = ? AND status = 'Tersedia' "
                        + "ORDER BY ID_Stk ASC LIMIT ? FOR UPDATE")) {
                    ps.setLong(1, idProduk);
                    ps.setInt(2, qty);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            stockIds.add(rs.getLong("ID_Stk"));
                        }
                    }
                }

                if (stockIds.size() < qty) {
                    throw new IllegalStateException("Stok lisensi tidak mencukupi (tersedia " + stockIds.size() + ")");
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Invoice (Provider, Payment_Link, External_ID, Fee, Net_Amount, Paid_At, Expires_At, Status) "
                        + "VALUES (?, ?, ?, 0, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, SumopodClient.PROVIDER_NAME);
                    ps.setString(2, "invoice?order_id=" + orderId);
                    ps.setString(3, orderId);
                    ps.setLong(4, subtotal);
                    ps.setNull(5, Types.TIMESTAMP);
                    ps.setTimestamp(6, expiresAt);
                    ps.setString(7, "Pending");
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to create invoice");
                        }
                        idInvoice = keys.getLong(1);
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO Transaksi (ID_Pelanggan, ID_Invoice, Total_Pembayaran, Metode_Pembayaran, Status, Gmail_trx, Nama_Penerima) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    if (idPelanggan != null) {
                        ps.setLong(1, idPelanggan);
                    } else {
                        ps.setNull(1, Types.BIGINT);
                    }
                    ps.setLong(2, idInvoice);
                    ps.setLong(3, subtotal);
                    ps.setString(4, "QRIS");
                    ps.setString(5, "Unpaid");
                    ps.setString(6, buyerEmail);
                    ps.setString(7, buyerName);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Failed to create transaction");
                        }
                        idTrx = keys.getLong(1);
                    }
                }

                try (PreparedStatement claimPs = conn.prepareStatement(
                        "UPDATE Stocks SET status = 'Terjual', claimedAt = CURDATE() WHERE ID_Stk = ?");
                     PreparedStatement detailPs = conn.prepareStatement(
                        "INSERT INTO Detail_Transaksi (ID_Trx, ID_Stk, Harga_beli) VALUES (?, ?, ?)")) {
                    for (Long stockId : stockIds) {
                        claimPs.setLong(1, stockId);
                        claimPs.addBatch();

                        detailPs.setLong(1, idTrx);
                        detailPs.setLong(2, stockId);
                        detailPs.setInt(3, unitPrice);
                        detailPs.addBatch();
                    }
                    claimPs.executeBatch();
                    detailPs.executeBatch();
                }

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new SQLException(e);
            }
        }

        // Call Sumopod QRIS API outside the SQL transaction so row locks aren't held during network I/O.
        if (sumopodClient.isConfigured()) {
            int expiresInHours = Math.max(1, Math.min(24, (int) Math.ceil(expireMinutes / 60.0)));
            try {
                SumopodClient.PaymentResult result = sumopodClient.createPayment(orderId, subtotal, expiresInHours);
                attachSumopodPayment(idInvoice, idTrx, result, subtotal, expiresAt);
            } catch (Exception e) {
                markPurchaseFailed(idTrx, idInvoice);
                throw new IllegalStateException("Gagal membuat pembayaran QRIS Sumopod: " + e.getMessage(), e);
            }
        }

        return idTrx;
    }

    private void attachSumopodPayment(long idInvoice, long idTrx, SumopodClient.PaymentResult result,
                                      long subtotal, Timestamp localExpiresAt) throws SQLException {
        long netAmt = result.getNetAmount() > 0 ? result.getNetAmount() : subtotal;
        long totalWithFee = netAmt + result.getFee();
        Timestamp effectiveExpiry = localExpiresAt;
        if (result.getExpiresAt() != null && result.getExpiresAt().before(localExpiresAt)) {
            effectiveExpiry = result.getExpiresAt();
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Invoice SET Provider = ?, Provider_Payment_ID = ?, Payment_Link = ?, "
                        + "Fee = ?, Net_Amount = ?, Expires_At = ? WHERE ID_Invoice = ?")) {
                    ps.setString(1, SumopodClient.PROVIDER_NAME);
                    ps.setString(2, result.getPaymentId());
                    ps.setString(3, result.getPaymentLinkUrl());
                    ps.setInt(4, result.getFee());
                    ps.setLong(5, netAmt);
                    ps.setTimestamp(6, effectiveExpiry);
                    ps.setLong(7, idInvoice);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Transaksi SET Total_Pembayaran = ? WHERE ID_Trx = ?")) {
                    ps.setLong(1, totalWithFee);
                    ps.setLong(2, idTrx);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void markPurchaseFailed(long idTrx, long idInvoice) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                releaseStocks(conn, idTrx);
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Transaksi SET Status = 'Failed' WHERE ID_Trx = ? AND Status = 'Unpaid'")) {
                    ps.setLong(1, idTrx);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Invoice SET Status = 'Cancelled' WHERE ID_Invoice = ? AND Status = 'Pending'")) {
                    ps.setLong(1, idInvoice);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Applies a settled webhook or debug-simulated payment outcome idempotently by order_id (External_ID).
     */
    public boolean applyPaymentEvent(SumopodClient.PaymentEvent ev) throws SQLException {
        if (ev == null || !ev.isSettled() || ev.getOrderId() == null || ev.getOrderId().isEmpty()) {
            return false;
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                long idTrx;
                long idInvoice;
                Long idPelanggan;
                long currentTotal;
                int fee;
                String invoiceStatus;

                String findSql = "SELECT t.ID_Trx, t.ID_Invoice, t.ID_Pelanggan, t.Total_Pembayaran, "
                        + "i.Fee, i.Status AS Invoice_Status "
                        + "FROM Transaksi t "
                        + "INNER JOIN Invoice i ON i.ID_Invoice = t.ID_Invoice "
                        + "WHERE i.External_ID = ? FOR UPDATE";

                try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                    ps.setString(1, ev.getOrderId());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        idTrx = rs.getLong("ID_Trx");
                        idInvoice = rs.getLong("ID_Invoice");
                        long pid = rs.getLong("ID_Pelanggan");
                        idPelanggan = rs.wasNull() ? null : pid;
                        currentTotal = rs.getLong("Total_Pembayaran");
                        fee = rs.getInt("Fee");
                        invoiceStatus = rs.getString("Invoice_Status");
                    }
                }

                // Idempotent: ignore if already settled.
                if (!"Pending".equalsIgnoreCase(invoiceStatus)) {
                    conn.commit();
                    return false;
                }

                if ("PAID".equals(ev.getStatus())) {
                    long finalTotal = ev.getNetAmount() != null ? ev.getNetAmount() + fee : currentTotal;
                    Timestamp paidAt = ev.getPaidAt() != null
                            ? ev.getPaidAt()
                            : new Timestamp(System.currentTimeMillis());

                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Invoice SET Status = 'Paid', Net_Amount = COALESCE(?, Net_Amount), Paid_At = ? "
                            + "WHERE ID_Invoice = ?")) {
                        if (ev.getNetAmount() != null) {
                            ps.setLong(1, ev.getNetAmount());
                        } else {
                            ps.setNull(1, Types.BIGINT);
                        }
                        ps.setTimestamp(2, paidAt);
                        ps.setLong(3, idInvoice);
                        ps.executeUpdate();
                    }

                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Transaksi SET Status = 'Completed', Total_Pembayaran = ? WHERE ID_Trx = ?")) {
                        ps.setLong(1, finalTotal);
                        ps.setLong(2, idTrx);
                        ps.executeUpdate();
                    }

                    if (idPelanggan != null) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Pelanggan SET Total_Pembelian = Total_Pembelian + ? WHERE ID_Pelanggan = ?")) {
                            ps.setLong(1, finalTotal);
                            ps.setLong(2, idPelanggan);
                            ps.executeUpdate();
                        }
                    }
                } else {
                    boolean expired = "EXPIRED".equals(ev.getStatus());
                    String newInvStatus = expired ? "Expired" : "Cancelled";
                    String newTrxStatus = expired ? "Expired" : "Failed";

                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Invoice SET Status = ? WHERE ID_Invoice = ?")) {
                        ps.setString(1, newInvStatus);
                        ps.setLong(2, idInvoice);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE Transaksi SET Status = ? WHERE ID_Trx = ?")) {
                        ps.setString(1, newTrxStatus);
                        ps.setLong(2, idTrx);
                        ps.executeUpdate();
                    }
                    releaseStocks(conn, idTrx);
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    /**
     * Sweeps any Pending invoices past their Expires_At timestamp and frees reserved license keys.
     */
    public int expireStalePendingTransactions() throws SQLException {
        List<String> staleOrderIds = new ArrayList<>();
        String sql = "SELECT i.External_ID FROM Invoice i "
                + "INNER JOIN Transaksi t ON t.ID_Invoice = i.ID_Invoice "
                + "WHERE i.Status = 'Pending' AND t.Status = 'Unpaid' AND i.Expires_At <= NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ext = rs.getString("External_ID");
                if (ext != null && !ext.isEmpty()) {
                    staleOrderIds.add(ext);
                }
            }
        }
        int count = 0;
        for (String orderId : staleOrderIds) {
            if (applyPaymentEvent(new SumopodClient.PaymentEvent(orderId, "EXPIRED", null, null, true))) {
                count++;
            }
        }
        return count;
    }

    public List<Transaksi> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY t.ID_Trx DESC";
        return queryList(sql, null);
    }

    public List<Transaksi> findByPelanggan(long idPelanggan) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.ID_Pelanggan = ? ORDER BY t.ID_Trx DESC";
        return queryList(sql, idPelanggan);
    }

    public Transaksi findById(long idTrx) throws SQLException {
        String sql = BASE_SELECT + "WHERE t.ID_Trx = ?";
        Transaksi trx = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTrx);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    trx = mapRow(rs);
                }
            }
        }
        if (trx != null) {
            trx.setDetails(findDetails(trx.getIdTrx()));
        }
        return trx;
    }

    public Transaksi findByOrderId(String orderId) throws SQLException {
        if (orderId == null || orderId.trim().isEmpty()) {
            return null;
        }
        String sql = BASE_SELECT + "WHERE UPPER(TRIM(i.External_ID)) = UPPER(?)";
        Transaksi trx = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, orderId.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    trx = mapRow(rs);
                }
            }
        }
        if (trx != null) {
            trx.setDetails(findDetails(trx.getIdTrx()));
        }
        return trx;
    }

    public boolean confirmPayment(long idTrx) throws SQLException {
        return updateStatus(idTrx, "Completed");
    }

    /**
     * Atomically transitions a transaction to any valid status (Unpaid, Completed, Failed, Expired, Cancelled),
     * synchronizing Invoice status/Paid_At/Expires_At, Stock availability, and customer Total_Pembelian.
     */
    public boolean updateStatus(long idTrx, String rawNewStatus) throws SQLException {
        String newStatus = normalizeStatus(rawNewStatus);
        if (newStatus == null) {
            throw new IllegalArgumentException("Status transaksi tidak valid: " + rawNewStatus);
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long idPelanggan;
                long idInvoice;
                long totalPembayaran;
                String oldStatus;

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ID_Pelanggan, ID_Invoice, Total_Pembayaran, Status "
                        + "FROM Transaksi WHERE ID_Trx = ? FOR UPDATE")) {
                    ps.setLong(1, idTrx);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        long pid = rs.getLong("ID_Pelanggan");
                        idPelanggan = rs.wasNull() ? null : pid;
                        idInvoice = rs.getLong("ID_Invoice");
                        totalPembayaran = rs.getLong("Total_Pembayaran");
                        oldStatus = rs.getString("Status");
                    }
                }

                if (newStatus.equalsIgnoreCase(oldStatus)) {
                    conn.commit();
                    return true;
                }

                boolean wasCompleted = "Completed".equalsIgnoreCase(oldStatus);
                boolean willBeCompleted = "Completed".equalsIgnoreCase(newStatus);
                boolean wasReleased = isReleasedStatus(oldStatus);
                boolean willBeReleased = isReleasedStatus(newStatus);

                if (!wasReleased && willBeReleased) {
                    releaseStocks(conn, idTrx);
                } else if (wasReleased && !willBeReleased) {
                    reclaimStocks(conn, idTrx);
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE Transaksi SET Status = ? WHERE ID_Trx = ?")) {
                    ps.setString(1, newStatus);
                    ps.setLong(2, idTrx);
                    ps.executeUpdate();
                }

                if (idInvoice > 0) {
                    if (willBeCompleted) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Invoice SET Status = 'Paid', Paid_At = COALESCE(Paid_At, NOW()) WHERE ID_Invoice = ?")) {
                            ps.setLong(1, idInvoice);
                            ps.executeUpdate();
                        }
                    } else if ("Unpaid".equalsIgnoreCase(newStatus)) {
                        int expireMinutes = PaymentConfig.getExpireMinutes();
                        Timestamp newExpiry = new Timestamp(System.currentTimeMillis() + expireMinutes * 60L * 1000L);
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Invoice SET Status = 'Pending', Paid_At = NULL, "
                                + "Expires_At = CASE WHEN Expires_At <= NOW() THEN ? ELSE Expires_At END "
                                + "WHERE ID_Invoice = ?")) {
                            ps.setTimestamp(1, newExpiry);
                            ps.setLong(2, idInvoice);
                            ps.executeUpdate();
                        }
                    } else {
                        String invStatus = "Expired".equalsIgnoreCase(newStatus) ? "Expired" : "Cancelled";
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Invoice SET Status = ?, Paid_At = NULL WHERE ID_Invoice = ?")) {
                            ps.setString(1, invStatus);
                            ps.setLong(2, idInvoice);
                            ps.executeUpdate();
                        }
                    }
                }

                if (idPelanggan != null) {
                    if (!wasCompleted && willBeCompleted) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Pelanggan SET Total_Pembelian = Total_Pembelian + ? WHERE ID_Pelanggan = ?")) {
                            ps.setLong(1, totalPembayaran);
                            ps.setLong(2, idPelanggan);
                            ps.executeUpdate();
                        }
                    } else if (wasCompleted && !willBeCompleted) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Pelanggan SET Total_Pembelian = GREATEST(0, Total_Pembelian - ?) WHERE ID_Pelanggan = ?")) {
                            ps.setLong(1, totalPembayaran);
                            ps.setLong(2, idPelanggan);
                            ps.executeUpdate();
                        }
                    }
                }

                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new SQLException(e);
            }
        }
    }

    /**
     * Cancels and deletes a transaction, releasing claimed stocks back to 'Tersedia'
     * if the transaction was not completed, and adjusting customer Total_Pembelian if it was.
     */
    public boolean deleteTransaction(long idTrx) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long idPelanggan = null;
                long idInvoice = 0;
                long totalPembayaran = 0;
                String status = null;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT ID_Pelanggan, ID_Invoice, Total_Pembayaran, Status FROM Transaksi WHERE ID_Trx = ? FOR UPDATE")) {
                    ps.setLong(1, idTrx);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        long pid = rs.getLong("ID_Pelanggan");
                        idPelanggan = rs.wasNull() ? null : pid;
                        idInvoice = rs.getLong("ID_Invoice");
                        totalPembayaran = rs.getLong("Total_Pembayaran");
                        status = rs.getString("Status");
                    }
                }

                if ("Completed".equalsIgnoreCase(status)) {
                    if (idPelanggan != null) {
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE Pelanggan SET Total_Pembelian = GREATEST(0, Total_Pembelian - ?) WHERE ID_Pelanggan = ?")) {
                            ps.setLong(1, totalPembayaran);
                            ps.setLong(2, idPelanggan);
                            ps.executeUpdate();
                        }
                    }
                } else {
                    releaseStocks(conn, idTrx);
                }

                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Detail_Transaksi WHERE ID_Trx = ?")) {
                    ps.setLong(1, idTrx);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Transaksi WHERE ID_Trx = ?")) {
                    ps.setLong(1, idTrx);
                    ps.executeUpdate();
                }
                if (idInvoice > 0) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Invoice WHERE ID_Invoice = ?")) {
                        ps.setLong(1, idInvoice);
                        ps.executeUpdate();
                    }
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    private void releaseStocks(Connection conn, long idTrx) throws SQLException {
        String sql = "UPDATE Stocks s "
                + "INNER JOIN Detail_Transaksi dt ON dt.ID_Stk = s.ID_Stk "
                + "SET s.status = 'Tersedia', s.claimedAt = NULL "
                + "WHERE dt.ID_Trx = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTrx);
            ps.executeUpdate();
        }
    }

    private void reclaimStocks(Connection conn, long idTrx) throws SQLException {
        String checkSql = "SELECT s.ID_Stk, s.status FROM Stocks s "
                + "INNER JOIN Detail_Transaksi dt ON dt.ID_Stk = s.ID_Stk "
                + "WHERE dt.ID_Trx = ? FOR UPDATE";
        List<Long> stockIds = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setLong(1, idTrx);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!"Tersedia".equalsIgnoreCase(rs.getString("status"))) {
                        throw new IllegalStateException(
                                "Sebagian stok lisensi transaksi ini sudah dipakai pesanan lain.");
                    }
                    stockIds.add(rs.getLong("ID_Stk"));
                }
            }
        }
        if (!stockIds.isEmpty()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE Stocks SET status = 'Terjual', claimedAt = CURDATE() WHERE ID_Stk = ?")) {
                for (Long sid : stockIds) {
                    ps.setLong(1, sid);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private static boolean isReleasedStatus(String status) {
        return "Failed".equalsIgnoreCase(status)
                || "Expired".equalsIgnoreCase(status)
                || "Cancelled".equalsIgnoreCase(status);
    }

    private static String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        switch (status.trim().toLowerCase()) {
            case "unpaid": return "Unpaid";
            case "completed": return "Completed";
            case "failed": return "Failed";
            case "expired": return "Expired";
            case "cancelled": return "Cancelled";
            default: return null;
        }
    }

    private List<Transaksi> queryList(String sql, Long idPelanggan) throws SQLException {
        List<Transaksi> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (idPelanggan != null) {
                ps.setLong(1, idPelanggan);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        if (!list.isEmpty()) {
            List<Long> trxIds = new ArrayList<>(list.size());
            for (Transaksi t : list) {
                trxIds.add(t.getIdTrx());
            }
            java.util.Map<Long, List<DetailTransaksi>> detailsMap = findDetailsByTrxIds(trxIds);
            for (Transaksi t : list) {
                List<DetailTransaksi> dList = detailsMap.get(t.getIdTrx());
                if (dList != null) {
                    t.setDetails(dList);
                }
            }
        }
        return list;
    }

    private java.util.Map<Long, List<DetailTransaksi>> findDetailsByTrxIds(List<Long> trxIds) throws SQLException {
        java.util.Map<Long, List<DetailTransaksi>> map = new java.util.HashMap<>();
        if (trxIds == null || trxIds.isEmpty()) {
            return map;
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < trxIds.size(); i++) {
            if (i > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
        }
        String sql = "SELECT dt.ID_DetailTrx, dt.ID_Trx, dt.ID_Stk, dt.Harga_beli, "
                + "s.kode_lisensi, p.Nama_Produk "
                + "FROM Detail_Transaksi dt "
                + "INNER JOIN Stocks s ON s.ID_Stk = dt.ID_Stk "
                + "INNER JOIN Produk p ON p.ID_Produk = s.ID_Produk "
                + "WHERE dt.ID_Trx IN (" + placeholders + ") "
                + "ORDER BY dt.ID_DetailTrx ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < trxIds.size(); i++) {
                ps.setLong(i + 1, trxIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetailTransaksi d = new DetailTransaksi();
                    d.setIdDetailTrx(rs.getLong("ID_DetailTrx"));
                    d.setIdTrx(rs.getLong("ID_Trx"));
                    d.setIdStk(rs.getLong("ID_Stk"));
                    d.setHargaBeli(rs.getInt("Harga_beli"));
                    d.setKodeLisensi(rs.getString("kode_lisensi"));
                    d.setNamaProduk(rs.getString("Nama_Produk"));
                    map.computeIfAbsent(d.getIdTrx(), k -> new ArrayList<>()).add(d);
                }
            }
        }
        return map;
    }

    private List<DetailTransaksi> findDetails(long idTrx) throws SQLException {
        String sql = "SELECT dt.ID_DetailTrx, dt.ID_Trx, dt.ID_Stk, dt.Harga_beli, "
                + "s.kode_lisensi, p.Nama_Produk "
                + "FROM Detail_Transaksi dt "
                + "INNER JOIN Stocks s ON s.ID_Stk = dt.ID_Stk "
                + "INNER JOIN Produk p ON p.ID_Produk = s.ID_Produk "
                + "WHERE dt.ID_Trx = ? "
                + "ORDER BY dt.ID_DetailTrx ASC";
        List<DetailTransaksi> details = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTrx);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetailTransaksi d = new DetailTransaksi();
                    d.setIdDetailTrx(rs.getLong("ID_DetailTrx"));
                    d.setIdTrx(rs.getLong("ID_Trx"));
                    d.setIdStk(rs.getLong("ID_Stk"));
                    d.setHargaBeli(rs.getInt("Harga_beli"));
                    d.setKodeLisensi(rs.getString("kode_lisensi"));
                    d.setNamaProduk(rs.getString("Nama_Produk"));
                    details.add(d);
                }
            }
        }
        return details;
    }

    private Transaksi mapRow(ResultSet rs) throws SQLException {
        Transaksi t = new Transaksi();
        t.setIdTrx(rs.getLong("ID_Trx"));
        long pid = rs.getLong("ID_Pelanggan");
        t.setIdPelanggan(rs.wasNull() ? null : pid);
        t.setNamaPelanggan(rs.getString("Nama_Pelanggan"));
        t.setIdInvoice(rs.getLong("ID_Invoice"));
        t.setTanggalTrx(rs.getTimestamp("Tanggal_Trx"));
        t.setTotalPembayaran(rs.getLong("Total_Pembayaran"));
        t.setMetodePembayaran(rs.getString("Metode_Pembayaran"));
        t.setStatus(rs.getString("Status"));
        t.setGmailTrx(rs.getString("Gmail_trx"));
        t.setNamaPenerima(rs.getString("Nama_Penerima"));

        Invoice inv = new Invoice();
        inv.setIdInvoice(t.getIdInvoice());
        inv.setProvider(rs.getString("Invoice_Provider"));
        inv.setProviderPaymentId(rs.getString("Provider_Payment_ID"));
        inv.setPaymentLink(rs.getString("Payment_Link"));
        inv.setExternalId(rs.getString("External_ID"));
        inv.setFee(rs.getInt("Fee"));
        long netAmt = rs.getLong("Net_Amount");
        inv.setNetAmount(rs.wasNull() ? null : netAmt);
        inv.setPaidAt(rs.getTimestamp("Paid_At"));
        inv.setExpiresAt(rs.getTimestamp("Expires_At"));
        inv.setStatus(rs.getString("Invoice_Status"));
        t.setInvoice(inv);
        return t;
    }
}
