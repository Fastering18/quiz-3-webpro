package com.zstore.dao;

import com.zstore.config.DBConnection;
import com.zstore.model.Produk;
import com.zstore.model.Tag;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProdukDAO {

    private static final String BASE_SELECT =
            "SELECT p.ID_Produk, p.ID_Provider, pr.Nama AS Provider_Nama, p.Nama_Produk, "
            + "p.Harga_produk, p.Jenis_Produk, p.Status_Produk, p.Deskripsi, p.Thumbnail_Img, "
            + "COALESCE(SUM(CASE WHEN s.status = 'Tersedia' THEN 1 ELSE 0 END), 0) AS available_stock, "
            + "COALESCE(SUM(CASE WHEN s.status <> 'Tersedia' AND s.ID_Stk IS NOT NULL THEN 1 ELSE 0 END), 0) AS sold_stock "
            + "FROM Produk p "
            + "INNER JOIN Provider pr ON pr.ID_Provider = p.ID_Provider "
            + "LEFT JOIN Stocks s ON s.ID_Produk = p.ID_Produk ";

    private static final String GROUP_ORDER =
            "GROUP BY p.ID_Produk, p.ID_Provider, pr.Nama, p.Nama_Produk, "
            + "p.Harga_produk, p.Jenis_Produk, p.Status_Produk, p.Deskripsi, p.Thumbnail_Img "
            + "ORDER BY p.ID_Produk ASC";

    public List<Produk> findAll() throws SQLException {
        return findFiltered(0, 0, null);
    }

    public List<Produk> findFiltered(long providerId, long tagId, String query) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT);
        List<Object> params = new ArrayList<>();

        if (tagId > 0) {
            sql.append("INNER JOIN Product_Tags pt ON pt.ID_Produk = p.ID_Produk ");
        }

        List<String> where = new ArrayList<>();
        if (providerId > 0) {
            where.add("p.ID_Provider = ?");
            params.add(providerId);
        }
        if (tagId > 0) {
            where.add("pt.ID_Tag = ?");
            params.add(tagId);
        }
        if (query != null && !query.trim().isEmpty()) {
            where.add("(LOWER(p.Nama_Produk) LIKE ? OR LOWER(pr.Nama) LIKE ? OR LOWER(p.Deskripsi) LIKE ?)");
            String like = "%" + query.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (!where.isEmpty()) {
            sql.append("WHERE ").append(String.join(" AND ", where)).append(" ");
        }
        sql.append(GROUP_ORDER);

        List<Produk> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object val = params.get(i);
                if (val instanceof Long) {
                    ps.setLong(i + 1, (Long) val);
                } else {
                    ps.setString(i + 1, String.valueOf(val));
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Produk findById(long idProduk) throws SQLException {
        String sql = BASE_SELECT + "WHERE p.ID_Produk = ? " + GROUP_ORDER;
        Produk produk = null;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idProduk);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    produk = mapRow(rs);
                }
            }
        }
        if (produk != null) {
            produk.setTags(new TagDAO().findByProductId(produk.getIdProduk()));
        }
        return produk;
    }

    public long create(Produk p, long[] tagIds) throws SQLException {
        String sql = "INSERT INTO Produk (ID_Provider, Nama_Produk, Harga_produk, Jenis_Produk, Status_Produk, Deskripsi, Thumbnail_Img) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, p.getIdProvider());
                ps.setString(2, p.getNamaProduk());
                ps.setInt(3, p.getHargaProduk());
                ps.setString(4, p.getJenisProduk());
                ps.setString(5, p.getStatusProduk());
                ps.setString(6, p.getDeskripsi());
                ps.setString(7, p.getThumbnailImg());
                ps.executeUpdate();

                long newId = 0;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        newId = keys.getLong(1);
                    }
                }
                if (newId > 0 && tagIds != null && tagIds.length > 0) {
                    syncTags(conn, newId, tagIds);
                }
                conn.commit();
                return newId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean update(Produk p, long[] tagIds) throws SQLException {
        String sql = "UPDATE Produk SET ID_Provider = ?, Nama_Produk = ?, Harga_produk = ?, "
                + "Jenis_Produk = ?, Status_Produk = ?, Deskripsi = ?, Thumbnail_Img = ? "
                + "WHERE ID_Produk = ?";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, p.getIdProvider());
                ps.setString(2, p.getNamaProduk());
                ps.setInt(3, p.getHargaProduk());
                ps.setString(4, p.getJenisProduk());
                ps.setString(5, p.getStatusProduk());
                ps.setString(6, p.getDeskripsi());
                ps.setString(7, p.getThumbnailImg());
                ps.setLong(8, p.getIdProduk());
                int updated = ps.executeUpdate();

                try (PreparedStatement delTags = conn.prepareStatement("DELETE FROM Product_Tags WHERE ID_Produk = ?")) {
                    delTags.setLong(1, p.getIdProduk());
                    delTags.executeUpdate();
                }
                if (tagIds != null && tagIds.length > 0) {
                    syncTags(conn, p.getIdProduk(), tagIds);
                }
                conn.commit();
                return updated > 0;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public boolean delete(long idProduk) throws SQLException {
        String sql = "DELETE FROM Produk WHERE ID_Produk = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idProduk);
            return ps.executeUpdate() > 0;
        }
    }

    private void syncTags(Connection conn, long idProduk, long[] tagIds) throws SQLException {
        String sql = "INSERT IGNORE INTO Product_Tags (ID_Produk, ID_Tag) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (long tagId : tagIds) {
                if (tagId > 0) {
                    ps.setLong(1, idProduk);
                    ps.setLong(2, tagId);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    private Produk mapRow(ResultSet rs) throws SQLException {
        Produk p = new Produk();
        p.setIdProduk(rs.getLong("ID_Produk"));
        p.setIdProvider(rs.getLong("ID_Provider"));
        p.setProviderNama(rs.getString("Provider_Nama"));
        p.setNamaProduk(rs.getString("Nama_Produk"));
        p.setHargaProduk(rs.getInt("Harga_produk"));
        p.setJenisProduk(rs.getString("Jenis_Produk"));
        p.setStatusProduk(rs.getString("Status_Produk"));
        p.setDeskripsi(rs.getString("Deskripsi"));
        p.setThumbnailImg(rs.getString("Thumbnail_Img"));
        p.setAvailableStock(rs.getInt("available_stock"));
        p.setSoldStock(rs.getInt("sold_stock"));
        return p;
    }
}

