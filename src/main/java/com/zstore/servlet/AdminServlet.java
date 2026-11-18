package com.zstore.servlet;

import com.zstore.dao.PelangganDAO;
import com.zstore.dao.ProdukDAO;
import com.zstore.dao.ProviderDAO;
import com.zstore.dao.StockDAO;
import com.zstore.dao.TagDAO;
import com.zstore.dao.TransaksiDAO;
import com.zstore.model.Pelanggan;
import com.zstore.model.Produk;
import com.zstore.model.Provider;
import com.zstore.model.Stock;
import com.zstore.util.FormatUtil;
import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AdminServlet extends HttpServlet {

    private final ProviderDAO providerDAO = new ProviderDAO();
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final TagDAO tagDAO = new TagDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Pelanggan currentUser = requireAdmin(req, resp);
        if (currentUser == null) {
            return;
        }

        String tab = FormatUtil.trimOrEmpty(req.getParameter("tab"));
        if (tab.isEmpty()) {
            tab = "products";
        }
        long filterProduct = FormatUtil.parseLongOrDefault(req.getParameter("filterProduct"), 0);
        long editProductId = FormatUtil.parseLongOrDefault(req.getParameter("editProduct"), 0);
        long editProviderId = FormatUtil.parseLongOrDefault(req.getParameter("editProvider"), 0);
        long editStockId = FormatUtil.parseLongOrDefault(req.getParameter("editStock"), 0);

        try {
            req.setAttribute("activeTab", tab);
            req.setAttribute("filterProduct", filterProduct);
            req.setAttribute("providers", providerDAO.findAll());
            req.setAttribute("products", produkDAO.findAll());
            req.setAttribute("tags", tagDAO.findAll());
            req.setAttribute("stocks", stockDAO.findAll(filterProduct));
            req.setAttribute("customers", pelangganDAO.findAll());
            req.setAttribute("levels", pelangganDAO.findAllLevels());
            req.setAttribute("transactions", transaksiDAO.findAll());

            if (editProductId > 0) {
                req.setAttribute("editingProduct", produkDAO.findById(editProductId));
            }
            if (editProviderId > 0) {
                req.setAttribute("editingProvider", providerDAO.findById(editProviderId));
            }
            if (editStockId > 0) {
                req.setAttribute("editingStock", stockDAO.findById(editStockId));
            }

            req.setAttribute("pageTitle", "Admin Panel | Z-Store");
            req.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException("Database error loading admin panel", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Pelanggan currentUser = requireAdmin(req, resp);
        if (currentUser == null) {
            return;
        }

        HttpSession session = req.getSession();
        String action = FormatUtil.trimOrEmpty(req.getParameter("action"));
        String tab = FormatUtil.trimOrEmpty(req.getParameter("tab"));
        if (tab.isEmpty()) {
            tab = "products";
        }

        try {
            switch (action) {
                case "provider_create":
                    handleProviderCreate(req, session);
                    tab = "providers";
                    break;
                case "provider_update":
                    handleProviderUpdate(req, session);
                    tab = "providers";
                    break;
                case "provider_delete":
                    handleProviderDelete(req, session);
                    tab = "providers";
                    break;
                case "product_create":
                    handleProductCreate(req, session);
                    tab = "products";
                    break;
                case "product_update":
                    handleProductUpdate(req, session);
                    tab = "products";
                    break;
                case "product_delete":
                    handleProductDelete(req, session);
                    tab = "products";
                    break;
                case "stock_create":
                    handleStockCreate(req, session);
                    tab = "stocks";
                    break;
                case "stock_batch":
                    handleStockBatch(req, session);
                    tab = "stocks";
                    break;
                case "stock_update":
                    handleStockUpdate(req, session);
                    tab = "stocks";
                    break;
                case "stock_delete":
                    handleStockDelete(req, session);
                    tab = "stocks";
                    break;
                case "tag_create":
                    handleTagCreate(req, session);
                    tab = "tags";
                    break;
                case "tag_delete":
                    handleTagDelete(req, session);
                    tab = "tags";
                    break;
                case "customer_update":
                    handleCustomerUpdate(req, session);
                    tab = "customers";
                    break;
                case "customer_delete":
                    handleCustomerDelete(req, session);
                    tab = "customers";
                    break;
                case "trx_update":
                    handleTrxUpdate(req, session);
                    tab = "transactions";
                    break;
                case "trx_delete":
                    handleTrxDelete(req, session);
                    tab = "transactions";
                    break;
                default:
                    session.setAttribute("flashError", "Aksi admin tidak dikenal.");
                    break;
            }
        } catch (SQLException e) {
            session.setAttribute("flashError", "Operasi database gagal: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin?tab=" + tab);
    }

    private void handleProviderCreate(HttpServletRequest req, HttpSession session) throws SQLException {
        String nama = FormatUtil.trimOrEmpty(req.getParameter("nama"));
        String thumb = FormatUtil.trimOrEmpty(req.getParameter("thumbnail_img"));
        if (nama.isEmpty()) {
            session.setAttribute("flashError", "Nama provider wajib diisi.");
            return;
        }
        Provider pr = new Provider();
        pr.setNama(nama);
        pr.setThumbnailImg(thumb.isEmpty() ? "/static/Image/Logo/Logo.png" : thumb);
        providerDAO.create(pr);
        session.setAttribute("flashSuccess", "Provider baru ditambahkan.");
    }

    private void handleProviderUpdate(HttpServletRequest req, HttpSession session) throws SQLException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_provider"), 0);
        String nama = FormatUtil.trimOrEmpty(req.getParameter("nama"));
        String thumb = FormatUtil.trimOrEmpty(req.getParameter("thumbnail_img"));
        if (id <= 0 || nama.isEmpty()) {
            session.setAttribute("flashError", "Data provider tidak valid.");
            return;
        }
        Provider pr = new Provider();
        pr.setIdProvider(id);
        pr.setNama(nama);
        pr.setThumbnailImg(thumb.isEmpty() ? "/static/Image/Logo/Logo.png" : thumb);
        providerDAO.update(pr);
        session.setAttribute("flashSuccess", "Provider berhasil diperbarui.");
    }

    private void handleProviderDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_provider"), 0);
        try {
            if (providerDAO.delete(id)) {
                session.setAttribute("flashSuccess", "Provider dihapus.");
            } else {
                session.setAttribute("flashError", "Provider tidak ditemukan.");
            }
        } catch (SQLException e) {
            session.setAttribute("flashError", "Provider tidak dapat dihapus karena masih memiliki produk.");
        }
    }

    private void handleProductCreate(HttpServletRequest req, HttpSession session) throws SQLException {
        Produk p = readProductForm(req);
        if (p.getNamaProduk().isEmpty() || p.getIdProvider() <= 0 || p.getHargaProduk() <= 0) {
            session.setAttribute("flashError", "Provider, nama produk, dan harga positif wajib diisi.");
            return;
        }
        long[] tagIds = parseIdArray(req.getParameterValues("tag_ids"));
        produkDAO.create(p, tagIds);
        session.setAttribute("flashSuccess", "Produk baru berhasil ditambahkan.");
    }

    private void handleProductUpdate(HttpServletRequest req, HttpSession session) throws SQLException {
        Produk p = readProductForm(req);
        p.setIdProduk(FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0));
        if (p.getIdProduk() <= 0 || p.getNamaProduk().isEmpty() || p.getHargaProduk() <= 0) {
            session.setAttribute("flashError", "Data produk tidak valid.");
            return;
        }
        long[] tagIds = parseIdArray(req.getParameterValues("tag_ids"));
        produkDAO.update(p, tagIds);
        session.setAttribute("flashSuccess", "Produk berhasil diperbarui.");
    }

    private void handleProductDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0);
        try {
            if (produkDAO.delete(id)) {
                session.setAttribute("flashSuccess", "Produk berhasil dihapus.");
            } else {
                session.setAttribute("flashError", "Produk tidak ditemukan.");
            }
        } catch (SQLException e) {
            session.setAttribute("flashError", "Produk tidak dapat dihapus karena stoknya sudah terhubung ke transaksi.");
        }
    }

    private void handleStockCreate(HttpServletRequest req, HttpSession session) throws SQLException {
        long idProduk = FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0);
        String key = FormatUtil.trimOrEmpty(req.getParameter("kode_lisensi"));
        String expStr = FormatUtil.trimOrEmpty(req.getParameter("expired_at"));
        if (idProduk <= 0 || key.isEmpty()) {
            session.setAttribute("flashError", "Produk dan kode lisensi wajib diisi.");
            return;
        }
        Stock s = new Stock();
        s.setIdProduk(idProduk);
        s.setKodeLisensi(key);
        s.setStatus("Tersedia");
        s.setExpiredAt(expStr.isEmpty() ? null : Date.valueOf(expStr));
        stockDAO.create(s);
        session.setAttribute("flashSuccess", "Stok lisensi ditambahkan.");
    }

    private void handleStockBatch(HttpServletRequest req, HttpSession session) throws SQLException {
        long idProduk = FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0);
        String rawKeys = req.getParameter("kode_lisensi_batch");
        String expStr = FormatUtil.trimOrEmpty(req.getParameter("expired_at"));
        if (idProduk <= 0 || rawKeys == null || rawKeys.trim().isEmpty()) {
            session.setAttribute("flashError", "Produk dan daftar kode lisensi wajib diisi.");
            return;
        }
        String[] lines = rawKeys.split("\\r?\\n");
        List<String> keys = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                keys.add(line.trim());
            }
        }
        Date exp = expStr.isEmpty() ? null : Date.valueOf(expStr);
        int inserted = stockDAO.createBatch(idProduk, keys, exp);
        session.setAttribute("flashSuccess", inserted + " kode lisensi berhasil ditambahkan.");
    }

    private void handleStockUpdate(HttpServletRequest req, HttpSession session) throws SQLException {
        long idStk = FormatUtil.parseLongOrDefault(req.getParameter("id_stk"), 0);
        long idProduk = FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0);
        String key = FormatUtil.trimOrEmpty(req.getParameter("kode_lisensi"));
        String status = FormatUtil.trimOrEmpty(req.getParameter("status"));
        String expStr = FormatUtil.trimOrEmpty(req.getParameter("expired_at"));
        if (idStk <= 0 || idProduk <= 0 || key.isEmpty()) {
            session.setAttribute("flashError", "Data stok tidak valid.");
            return;
        }
        Stock s = new Stock();
        s.setIdStk(idStk);
        s.setIdProduk(idProduk);
        s.setKodeLisensi(key);
        s.setStatus(status.isEmpty() ? "Tersedia" : status);
        s.setExpiredAt(expStr.isEmpty() ? null : Date.valueOf(expStr));
        stockDAO.update(s);
        session.setAttribute("flashSuccess", "Stok lisensi berhasil diperbarui.");
    }

    private void handleStockDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long idStk = FormatUtil.parseLongOrDefault(req.getParameter("id_stk"), 0);
        if (stockDAO.delete(idStk)) {
            session.setAttribute("flashSuccess", "Stok lisensi dihapus.");
        } else {
            session.setAttribute("flashError", "Hanya stok berstatus Tersedia yang dapat dihapus.");
        }
    }

    private void handleTagCreate(HttpServletRequest req, HttpSession session) throws SQLException {
        String namaTag = FormatUtil.trimOrEmpty(req.getParameter("nama_tag"));
        if (namaTag.isEmpty()) {
            session.setAttribute("flashError", "Nama tag wajib diisi.");
            return;
        }
        tagDAO.create(namaTag);
        session.setAttribute("flashSuccess", "Tag baru ditambahkan.");
    }

    private void handleTagDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long idTag = FormatUtil.parseLongOrDefault(req.getParameter("id_tag"), 0);
        if (tagDAO.delete(idTag)) {
            session.setAttribute("flashSuccess", "Tag dihapus.");
        } else {
            session.setAttribute("flashError", "Tag tidak ditemukan.");
        }
    }

    private void handleCustomerUpdate(HttpServletRequest req, HttpSession session) throws SQLException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_pelanggan"), 0);
        long idLevel = FormatUtil.parseLongOrDefault(req.getParameter("id_level"), 1);
        long saldo = FormatUtil.parseLongOrDefault(req.getParameter("saldo"), 0);
        String status = FormatUtil.trimOrEmpty(req.getParameter("status_akun"));
        if (id <= 0) {
            session.setAttribute("flashError", "ID pelanggan tidak valid.");
            return;
        }
        pelangganDAO.adminUpdate(id, idLevel, Math.max(0, saldo), status.isEmpty() ? "Active" : status);
        session.setAttribute("flashSuccess", "Data pelanggan diperbarui.");
    }

    private void handleCustomerDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_pelanggan"), 0);
        if (pelangganDAO.delete(id)) {
            session.setAttribute("flashSuccess", "Akun pelanggan dihapus.");
        } else {
            session.setAttribute("flashError", "Akun Admin atau akun tidak ditemukan tidak dapat dihapus.");
        }
    }

    private void handleTrxUpdate(HttpServletRequest req, HttpSession session) throws SQLException {
        long idTrx = FormatUtil.parseLongOrDefault(req.getParameter("id_trx"), 0);
        String status = FormatUtil.trimOrEmpty(req.getParameter("status"));
        if (idTrx <= 0 || status.isEmpty()) {
            session.setAttribute("flashError", "Data transaksi tidak valid.");
            return;
        }
        transaksiDAO.updateStatus(idTrx, status);
        session.setAttribute("flashSuccess", "Status transaksi #" + idTrx + " diubah menjadi " + status + ".");
    }

    private void handleTrxDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long idTrx = FormatUtil.parseLongOrDefault(req.getParameter("id_trx"), 0);
        if (transaksiDAO.deleteTransaction(idTrx)) {
            session.setAttribute("flashSuccess", "Transaksi #" + idTrx + " dihapus.");
        } else {
            session.setAttribute("flashError", "Transaksi tidak ditemukan.");
        }
    }

    private Produk readProductForm(HttpServletRequest req) {
        Produk p = new Produk();
        p.setIdProvider(FormatUtil.parseLongOrDefault(req.getParameter("id_provider"), 0));
        p.setNamaProduk(FormatUtil.trimOrEmpty(req.getParameter("nama_produk")));
        p.setHargaProduk(FormatUtil.parseIntOrDefault(req.getParameter("harga_produk"), 0));
        String jenis = FormatUtil.trimOrEmpty(req.getParameter("jenis_produk"));
        p.setJenisProduk(jenis.isEmpty() ? "Item" : jenis);
        String status = FormatUtil.trimOrEmpty(req.getParameter("status_produk"));
        p.setStatusProduk(status.isEmpty() ? "Available" : status);
        p.setDeskripsi(FormatUtil.trimOrEmpty(req.getParameter("deskripsi")));
        String thumb = FormatUtil.trimOrEmpty(req.getParameter("thumbnail_img"));
        p.setThumbnailImg(thumb.isEmpty() ? "/static/Image/Logo/Logo.png" : thumb);
        return p;
    }

    private long[] parseIdArray(String[] raw) {
        if (raw == null) {
            return new long[0];
        }
        long[] ids = new long[raw.length];
        for (int i = 0; i < raw.length; i++) {
            ids[i] = FormatUtil.parseLongOrDefault(raw[i], 0);
        }
        return ids;
    }

    private Pelanggan requireAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Pelanggan user = session != null ? (Pelanggan) session.getAttribute("user") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        if (!user.isAdmin()) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return null;
        }
        return user;
    }
}

