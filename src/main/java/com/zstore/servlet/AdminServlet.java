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
import com.zstore.model.Transaksi;
import com.zstore.storage.S3Client;
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
import javax.servlet.http.Part;

public class AdminServlet extends HttpServlet {

    private static final String DEFAULT_THUMB = "/static/Image/Logo/Logo.png";

    private final ProviderDAO providerDAO = new ProviderDAO();
    private final ProdukDAO produkDAO = new ProdukDAO();
    private final TagDAO tagDAO = new TagDAO();
    private final StockDAO stockDAO = new StockDAO();
    private final PelangganDAO pelangganDAO = new PelangganDAO();
    private final TransaksiDAO transaksiDAO = new TransaksiDAO();
    private final S3Client s3Client = new S3Client();

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
        long editTagId = FormatUtil.parseLongOrDefault(req.getParameter("editTag"), 0);

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
            if (editTagId > 0) {
                req.setAttribute("editingTag", tagDAO.findById(editTagId));
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
                case "tag_update":
                    handleTagUpdate(req, session);
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
        } catch (IllegalArgumentException | IllegalStateException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            session.setAttribute("flashError", "Operasi database gagal: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin?tab=" + tab);
    }

    private void handleProviderCreate(HttpServletRequest req, HttpSession session)
            throws SQLException, IOException, ServletException {
        String nama = FormatUtil.trimOrEmpty(req.getParameter("nama"));
        if (nama.isEmpty()) {
            session.setAttribute("flashError", "Nama provider wajib diisi.");
            return;
        }
        String thumb = resolveUploadedThumbnail(req, null, "providers", session);
        if (thumb == null) {
            return;
        }
        Provider pr = new Provider();
        pr.setNama(nama);
        pr.setThumbnailImg(thumb);
        providerDAO.create(pr);
        session.setAttribute("flashSuccess", "Provider baru ditambahkan.");
    }

    private void handleProviderUpdate(HttpServletRequest req, HttpSession session)
            throws SQLException, IOException, ServletException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_provider"), 0);
        String nama = FormatUtil.trimOrEmpty(req.getParameter("nama"));
        if (id <= 0 || nama.isEmpty()) {
            session.setAttribute("flashError", "Data provider tidak valid.");
            return;
        }
        Provider existing = providerDAO.findById(id);
        if (existing == null) {
            session.setAttribute("flashError", "Provider tidak ditemukan.");
            return;
        }
        String oldThumb = existing.getThumbnailImg();
        String newThumb = resolveUploadedThumbnail(req, oldThumb, "providers", session);
        if (newThumb == null) {
            return;
        }

        Provider pr = new Provider();
        pr.setIdProvider(id);
        pr.setNama(nama);
        pr.setThumbnailImg(newThumb);
        providerDAO.update(pr);

        cleanupOldS3Upload(oldThumb, newThumb);
        session.setAttribute("flashSuccess", "Provider berhasil diperbarui.");
    }

    private void handleProviderDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_provider"), 0);
        try {
            Provider existing = providerDAO.findById(id);
            if (providerDAO.delete(id)) {
                if (existing != null) {
                    cleanupOldS3Upload(existing.getThumbnailImg(), null);
                }
                session.setAttribute("flashSuccess", "Provider dihapus.");
            } else {
                session.setAttribute("flashError", "Provider tidak ditemukan.");
            }
        } catch (SQLException e) {
            session.setAttribute("flashError", "Provider tidak dapat dihapus karena masih memiliki produk.");
        }
    }

    private void handleProductCreate(HttpServletRequest req, HttpSession session)
            throws SQLException, IOException, ServletException {
        Produk p = readProductForm(req);
        if (p.getNamaProduk().isEmpty() || p.getIdProvider() <= 0 || p.getHargaProduk() <= 0) {
            session.setAttribute("flashError", "Provider, nama produk, dan harga positif wajib diisi.");
            return;
        }
        String thumb = resolveUploadedThumbnail(req, null, "products", session);
        if (thumb == null) {
            return;
        }
        p.setThumbnailImg(thumb);

        long[] tagIds = resolveProductTagIds(req);
        produkDAO.create(p, tagIds);
        session.setAttribute("flashSuccess", "Produk baru berhasil ditambahkan.");
    }

    private void handleProductUpdate(HttpServletRequest req, HttpSession session)
            throws SQLException, IOException, ServletException {
        Produk p = readProductForm(req);
        p.setIdProduk(FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0));
        if (p.getIdProduk() <= 0 || p.getNamaProduk().isEmpty() || p.getHargaProduk() <= 0) {
            session.setAttribute("flashError", "Data produk tidak valid.");
            return;
        }
        Produk existing = produkDAO.findById(p.getIdProduk());
        if (existing == null) {
            session.setAttribute("flashError", "Produk tidak ditemukan.");
            return;
        }
        String oldThumb = existing.getThumbnailImg();
        String newThumb = resolveUploadedThumbnail(req, oldThumb, "products", session);
        if (newThumb == null) {
            return;
        }
        p.setThumbnailImg(newThumb);

        long[] tagIds = resolveProductTagIds(req);
        produkDAO.update(p, tagIds);
        cleanupOldS3Upload(oldThumb, newThumb);
        session.setAttribute("flashSuccess", "Produk berhasil diperbarui.");
    }

    private void handleProductDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long id = FormatUtil.parseLongOrDefault(req.getParameter("id_produk"), 0);
        try {
            Produk existing = produkDAO.findById(id);
            if (produkDAO.delete(id)) {
                if (existing != null) {
                    cleanupOldS3Upload(existing.getThumbnailImg(), null);
                }
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
        String raw = FormatUtil.trimOrEmpty(req.getParameter("nama_tag"));
        List<String> names = FormatUtil.splitCommaTags(raw);
        if (names.isEmpty()) {
            session.setAttribute("flashError", "Nama tag wajib diisi.");
            return;
        }
        int created = tagDAO.createMissingByNames(names);
        if (created > 0) {
            session.setAttribute("flashSuccess", created + " tag baru ditambahkan.");
        } else {
            session.setAttribute("flashError", "Tag tersebut sudah ada.");
        }
    }

    private void handleTagUpdate(HttpServletRequest req, HttpSession session) throws SQLException {
        long idTag = FormatUtil.parseLongOrDefault(req.getParameter("id_tag"), 0);
        String namaTag = FormatUtil.trimOrEmpty(req.getParameter("nama_tag"));
        if (idTag <= 0 || namaTag.isEmpty()) {
            session.setAttribute("flashError", "Data tag tidak valid.");
            return;
        }
        if (tagDAO.update(idTag, namaTag)) {
            session.setAttribute("flashSuccess", "Tag berhasil diperbarui.");
        } else {
            session.setAttribute("flashError", "Tag tidak ditemukan.");
        }
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
        Transaksi trx = transaksiDAO.findById(idTrx);
        if (trx == null) {
            session.setAttribute("flashError", "Transaksi #" + idTrx + " tidak ditemukan.");
            return;
        }
        transaksiDAO.updateStatus(idTrx, status);
        String label = (trx.getInvoice() != null && trx.getInvoice().getExternalId() != null)
                ? trx.getInvoice().getExternalId()
                : ("#" + idTrx);
        refreshSessionUserIfMatches(session, trx.getIdPelanggan());
        session.setAttribute("flashSuccess", "Status transaksi " + label + " diubah menjadi " + status + ".");
    }

    private void handleTrxDelete(HttpServletRequest req, HttpSession session) throws SQLException {
        long idTrx = FormatUtil.parseLongOrDefault(req.getParameter("id_trx"), 0);
        Transaksi trx = transaksiDAO.findById(idTrx);
        if (trx == null) {
            session.setAttribute("flashError", "Transaksi tidak ditemukan.");
            return;
        }
        String label = (trx.getInvoice() != null && trx.getInvoice().getExternalId() != null)
                ? trx.getInvoice().getExternalId()
                : ("#" + idTrx);
        if (transaksiDAO.deleteTransaction(idTrx)) {
            refreshSessionUserIfMatches(session, trx.getIdPelanggan());
            session.setAttribute("flashSuccess", "Transaksi " + label + " dihapus.");
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
        return p;
    }

    private long[] resolveProductTagIds(HttpServletRequest req) throws SQLException {
        String tagsCsv = req.getParameter("tags_csv");
        if (tagsCsv != null) {
            List<String> names = FormatUtil.splitCommaTags(tagsCsv);
            return tagDAO.findOrCreateByNames(names);
        }
        return parseIdArray(req.getParameterValues("tag_ids"));
    }

    /**
     * Resolves thumbnail image from uploaded multipart file, removal flag, or existing value.
     * Returns null when validation or S3 upload fails (with flashError already set).
     */
    private String resolveUploadedThumbnail(HttpServletRequest req, String existingThumb,
                                            String s3Folder, HttpSession session)
            throws IOException, ServletException {
        boolean removeThumb = "1".equals(FormatUtil.trimOrEmpty(req.getParameter("remove_thumbnail")));
        Part filePart = getFilePartIfMultipart(req, "thumbnail_file");

        if (filePart != null && filePart.getSize() > 0) {
            try {
                return s3Client.uploadImagePart(filePart, s3Folder);
            } catch (IOException e) {
                session.setAttribute("flashError", e.getMessage());
                return null;
            }
        }
        if (removeThumb) {
            return DEFAULT_THUMB;
        }
        if (existingThumb != null && !existingThumb.trim().isEmpty()) {
            return existingThumb.trim();
        }
        String fallbackParam = FormatUtil.trimOrEmpty(req.getParameter("thumbnail_img"));
        return fallbackParam.isEmpty() ? DEFAULT_THUMB : fallbackParam;
    }

    private void cleanupOldS3Upload(String oldUrl, String newUrl) {
        if (oldUrl == null || oldUrl.isEmpty() || oldUrl.equals(newUrl)) {
            return;
        }
        // Only delete user-uploaded objects under products/ or providers/; keep migrated catalog seeds untouched.
        if (oldUrl.contains("/products/") || oldUrl.contains("/providers/")) {
            s3Client.deleteByUrl(oldUrl);
        }
    }

    private Part getFilePartIfMultipart(HttpServletRequest req, String partName)
            throws IOException, ServletException {
        String ct = req.getContentType();
        if (ct == null || !ct.toLowerCase().startsWith("multipart/")) {
            return null;
        }
        return req.getPart(partName);
    }

    private void refreshSessionUserIfMatches(HttpSession session, Long affectedPelangganId) throws SQLException {
        if (session == null || affectedPelangganId == null) {
            return;
        }
        Object obj = session.getAttribute("user");
        if (obj instanceof Pelanggan && affectedPelangganId.equals(((Pelanggan) obj).getIdPelanggan())) {
            Pelanggan fresh = pelangganDAO.findById(affectedPelangganId);
            if (fresh != null) {
                session.setAttribute("user", fresh);
            }
        }
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
