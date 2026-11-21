package com.zstore.dao;

import com.zstore.config.DBConnection;
import com.zstore.model.Tag;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    public Tag findById(long idTag) throws SQLException {
        String sql = "SELECT t.ID_Tag, t.Nama_Tag, COUNT(pt.ID_Produk) AS product_count "
                + "FROM Tags t "
                + "LEFT JOIN Product_Tags pt ON pt.ID_Tag = t.ID_Tag "
                + "WHERE t.ID_Tag = ? "
                + "GROUP BY t.ID_Tag, t.Nama_Tag";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTag);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Tag t = new Tag();
                    t.setIdTag(rs.getLong("ID_Tag"));
                    t.setNamaTag(rs.getString("Nama_Tag"));
                    t.setProductCount(rs.getInt("product_count"));
                    return t;
                }
            }
        }
        return null;
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

    public Map<Long, List<Tag>> findByProductIds(List<Long> productIds) throws SQLException {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < productIds.size(); i++) {
            if (i > 0) {
                placeholders.append(',');
            }
            placeholders.append('?');
        }
        String sql = "SELECT pt.ID_Produk, t.ID_Tag, t.Nama_Tag "
                + "FROM Product_Tags pt "
                + "INNER JOIN Tags t ON t.ID_Tag = pt.ID_Tag "
                + "WHERE pt.ID_Produk IN (" + placeholders + ") "
                + "ORDER BY t.Nama_Tag ASC";
        Map<Long, List<Tag>> map = new HashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < productIds.size(); i++) {
                ps.setLong(i + 1, productIds.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long pid = rs.getLong("ID_Produk");
                    Tag t = new Tag();
                    t.setIdTag(rs.getLong("ID_Tag"));
                    t.setNamaTag(rs.getString("Nama_Tag"));
                    map.computeIfAbsent(pid, k -> new ArrayList<>()).add(t);
                }
            }
        }
        return map;
    }

    public long create(String namaTag) throws SQLException {
        String sql = "INSERT INTO Tags (Nama_Tag) VALUES (?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, namaTag.trim());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return 0;
    }

    public boolean update(long idTag, String namaTag) throws SQLException {
        String clean = namaTag == null ? "" : namaTag.trim();
        if (idTag <= 0 || clean.isEmpty()) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT ID_Tag FROM Tags WHERE LOWER(Nama_Tag) = LOWER(?) AND ID_Tag <> ?")) {
                check.setString(1, clean);
                check.setLong(2, idTag);
                try (ResultSet rs = check.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException("Tag \"" + clean + "\" sudah ada.");
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement("UPDATE Tags SET Nama_Tag = ? WHERE ID_Tag = ?")) {
                ps.setString(1, clean);
                ps.setLong(2, idTag);
                return ps.executeUpdate() > 0;
            }
        }
    }

    /**
     * Resolves a list of tag names (case-insensitive), inserting any that do not exist yet,
     * and returns their Tag IDs.
     */
    public long[] findOrCreateByNames(List<String> names) throws SQLException {
        if (names == null || names.isEmpty()) {
            return new long[0];
        }
        Set<Long> resolved = new LinkedHashSet<>();
        try (Connection conn = DBConnection.getConnection()) {
            Map<String, Long> existingMap = loadExistingMap(conn);
            for (String raw : names) {
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                String clean = raw.trim();
                String key = clean.toLowerCase(Locale.ROOT);
                Long id = existingMap.get(key);
                if (id == null) {
                    id = insertIgnoringDuplicate(conn, clean);
                    if (id != null && id > 0) {
                        existingMap.put(key, id);
                    }
                }
                if (id != null && id > 0) {
                    resolved.add(id);
                }
            }
        }
        long[] ids = new long[resolved.size()];
        int idx = 0;
        for (Long id : resolved) {
            ids[idx++] = id;
        }
        return ids;
    }

    /**
     * Creates any tags from the given list that do not yet exist (case-insensitive).
     * Returns the number of newly created tags.
     */
    public int createMissingByNames(List<String> names) throws SQLException {
        if (names == null || names.isEmpty()) {
            return 0;
        }
        int created = 0;
        try (Connection conn = DBConnection.getConnection()) {
            Map<String, Long> existingMap = loadExistingMap(conn);
            for (String raw : names) {
                if (raw == null || raw.trim().isEmpty()) {
                    continue;
                }
                String clean = raw.trim();
                String key = clean.toLowerCase(Locale.ROOT);
                if (!existingMap.containsKey(key)) {
                    Long id = insertIgnoringDuplicate(conn, clean);
                    if (id != null && id > 0) {
                        existingMap.put(key, id);
                        created++;
                    }
                }
            }
        }
        return created;
    }

    public boolean delete(long idTag) throws SQLException {
        String sql = "DELETE FROM Tags WHERE ID_Tag = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idTag);
            return ps.executeUpdate() > 0;
        }
    }

    private Map<String, Long> loadExistingMap(Connection conn) throws SQLException {
        Map<String, Long> map = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT ID_Tag, Nama_Tag FROM Tags");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("Nama_Tag");
                if (name != null) {
                    map.put(name.trim().toLowerCase(Locale.ROOT), rs.getLong("ID_Tag"));
                }
            }
        }
        return map;
    }

    private Long insertIgnoringDuplicate(Connection conn, String namaTag) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Tags (Nama_Tag) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, namaTag);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        } catch (SQLException dup) {
            try (PreparedStatement lookup = conn.prepareStatement(
                    "SELECT ID_Tag FROM Tags WHERE LOWER(Nama_Tag) = LOWER(?)")) {
                lookup.setString(1, namaTag);
                try (ResultSet rs = lookup.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong("ID_Tag");
                    }
                }
            }
            throw dup;
        }
        return null;
    }
}
