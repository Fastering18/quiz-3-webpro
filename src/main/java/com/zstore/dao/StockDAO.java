package com.zstore.dao;

import com.zstore.config.DBConnection;
import com.zstore.model.Stock;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class StockDAO {

    public List<Stock> findAll(long filterProductId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT s.ID_Stk, s.ID_Produk, p.Nama_Produk, s.kode_lisensi, s.Credit_Value, "
                + "s.claimedAt, s.expiredAt, s.status "
                + "FROM Stocks s "
                + "INNER JOIN Produk p ON p.ID_Produk = s.ID_Produk ");
        if (filterProductId > 0) {
            sql.append("WHERE s.ID_Produk = ? ");
        }
        sql.append("ORDER BY s.ID_Stk DESC LIMIT 200");

        List<Stock> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (filterProductId > 0) {
                ps.setLong(1, filterProductId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Stock findById(long idStk) throws SQLException {
        String sql = "SELECT s.ID_Stk, s.ID_Produk, p.Nama_Produk, s.kode_lisensi, s.Credit_Value, "
                + "s.claimedAt, s.expiredAt, s.status "
                + "FROM Stocks s "
                + "INNER JOIN Produk p ON p.ID_Produk = s.ID_Produk "
                + "WHERE s.ID_Stk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idStk);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public long create(Stock s) throws SQLException {
        String sql = "INSERT INTO Stocks (ID_Produk, kode_lisensi, Credit_Value, expiredAt, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, s.getIdProduk());
            ps.setString(2, s.getKodeLisensi());
            ps.setLong(3, s.getCreditValue());
            if (s.getExpiredAt() != null) {
                ps.setDate(4, s.getExpiredAt());
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, s.getStatus() != null ? s.getStatus() : "Tersedia");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return 0;
    }

    public int createBatch(long idProduk, List<String> licenseKeys, Date expiredAt) throws SQLException {
        String sql = "INSERT INTO Stocks (ID_Produk, kode_lisensi, Credit_Value, expiredAt, status) "
                + "VALUES (?, ?, 0, ?, 'Tersedia')";
        int count = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (String key : licenseKeys) {
                String trimmed = key.trim();
                if (trimmed.isEmpty()) {
                    continue;
                }
                ps.setLong(1, idProduk);
                ps.setString(2, trimmed);
                if (expiredAt != null) {
                    ps.setDate(3, expiredAt);
                } else {
                    ps.setNull(3, Types.DATE);
                }
                ps.addBatch();
                count++;
            }
            if (count > 0) {
                ps.executeBatch();
            }
        }
        return count;
    }

    public boolean update(Stock s) throws SQLException {
        String sql = "UPDATE Stocks SET ID_Produk = ?, kode_lisensi = ?, Credit_Value = ?, "
                + "expiredAt = ?, status = ? WHERE ID_Stk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, s.getIdProduk());
            ps.setString(2, s.getKodeLisensi());
            ps.setLong(3, s.getCreditValue());
            if (s.getExpiredAt() != null) {
                ps.setDate(4, s.getExpiredAt());
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setString(5, s.getStatus());
            ps.setLong(6, s.getIdStk());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long idStk) throws SQLException {
        String sql = "DELETE FROM Stocks WHERE ID_Stk = ? AND status = 'Tersedia'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idStk);
            return ps.executeUpdate() > 0;
        }
    }

    private Stock mapRow(ResultSet rs) throws SQLException {
        Stock s = new Stock();
        s.setIdStk(rs.getLong("ID_Stk"));
        s.setIdProduk(rs.getLong("ID_Produk"));
        s.setNamaProduk(rs.getString("Nama_Produk"));
        s.setKodeLisensi(rs.getString("kode_lisensi"));
        s.setCreditValue(rs.getLong("Credit_Value"));
        s.setClaimedAt(rs.getDate("claimedAt"));
        s.setExpiredAt(rs.getDate("expiredAt"));
        s.setStatus(rs.getString("status"));
        return s;
    }
}

