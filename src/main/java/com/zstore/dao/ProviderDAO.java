package com.zstore.dao;

import com.zstore.config.DBConnection;
import com.zstore.model.Provider;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProviderDAO {

    public List<Provider> findAll() throws SQLException {
        String sql = "SELECT pr.ID_Provider, pr.Nama, pr.Thumbnail_Img, COUNT(p.ID_Produk) AS product_count "
                + "FROM Provider pr "
                + "LEFT JOIN Produk p ON p.ID_Provider = pr.ID_Provider "
                + "GROUP BY pr.ID_Provider, pr.Nama, pr.Thumbnail_Img "
                + "ORDER BY pr.ID_Provider ASC";
        List<Provider> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Provider findById(long idProvider) throws SQLException {
        String sql = "SELECT pr.ID_Provider, pr.Nama, pr.Thumbnail_Img, COUNT(p.ID_Produk) AS product_count "
                + "FROM Provider pr "
                + "LEFT JOIN Produk p ON p.ID_Provider = pr.ID_Provider "
                + "WHERE pr.ID_Provider = ? "
                + "GROUP BY pr.ID_Provider, pr.Nama, pr.Thumbnail_Img";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idProvider);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public long create(Provider provider) throws SQLException {
        String sql = "INSERT INTO Provider (Nama, Thumbnail_Img) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, provider.getNama());
            ps.setString(2, provider.getThumbnailImg());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return 0;
    }

    public boolean update(Provider provider) throws SQLException {
        String sql = "UPDATE Provider SET Nama = ?, Thumbnail_Img = ? WHERE ID_Provider = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, provider.getNama());
            ps.setString(2, provider.getThumbnailImg());
            ps.setLong(3, provider.getIdProvider());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long idProvider) throws SQLException {
        String sql = "DELETE FROM Provider WHERE ID_Provider = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idProvider);
            return ps.executeUpdate() > 0;
        }
    }

    private Provider mapRow(ResultSet rs) throws SQLException {
        Provider pr = new Provider();
        pr.setIdProvider(rs.getLong("ID_Provider"));
        pr.setNama(rs.getString("Nama"));
        pr.setThumbnailImg(rs.getString("Thumbnail_Img"));
        pr.setProductCount(rs.getInt("product_count"));
        return pr;
    }
}

