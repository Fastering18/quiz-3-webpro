package com.zstore.dao;

import com.zstore.config.DBConnection;
import com.zstore.model.Tag;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {

    public List<Tag> findAll() throws SQLException {
        String sql = "SELECT t.ID_Tag, t.Nama_Tag, COUNT(pt.ID_Produk) AS product_count "
                + "FROM Tags t "
                + "LEFT JOIN Product_Tags pt ON pt.ID_Tag = t.ID_Tag "
                + "GROUP BY t.ID_Tag, t.Nama_Tag "
                + "ORDER BY t.Nama_Tag ASC";
        List<Tag> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Tag t = new Tag();
                t.setIdTag(rs.getLong("ID_Tag"));
                t.setNamaTag(rs.getString("Nama_Tag"));
                t.setProductCount(rs.getInt("product_count"));
                list.add(t);
            }
        }
        return list;
    }

    public List<Tag> findByProductId(long idProduk) throws SQLException {
        String sql = "SELECT t.ID_Tag, t.Nama_Tag "
                + "FROM Tags t "
                + "INNER JOIN Product_Tags pt ON pt.ID_Tag = t.ID_Tag "
                + "WHERE pt.ID_Produk = ? "
                + "ORDER BY t.Nama_Tag ASC";
        List<Tag> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idProduk);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tag t = new Tag();
                    t.setIdTag(rs.getLong("ID_Tag"));
                    t.setNamaTag(rs.getString("Nama_Tag"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    public long create(String namaTag) throws SQLException {
        String sql = "INSERT INTO Tags (Nama_Tag) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, namaTag);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return 0;
    }

    public boolean delete(long idTag) throws SQLException {
        String sql = "DELETE FROM Tags WHERE ID_Tag = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTag);
            return ps.executeUpdate() > 0;
        }
    }
}

