package com.zstore.dao;

import com.zstore.config.DBConnection;
import com.zstore.model.LevelPelanggan;
import com.zstore.model.Pelanggan;
import com.zstore.util.PasswordUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PelangganDAO {

    private static final String BASE_SELECT =
            "SELECT pl.ID_Pelanggan, pl.ID_Level, lv.Nama_level, lv.Diskon_level, "
            + "pl.Nama_Pelanggan, pl.Gmail_Pelanggan, pl.Password_Hash, pl.Role, "
            + "pl.Reseller_Valid_Date, pl.Total_Pembelian, pl.Saldo, pl.Status_Akun "
            + "FROM Pelanggan pl "
            + "INNER JOIN Level_Pelanggan lv ON lv.ID_Level = pl.ID_Level ";

    public List<Pelanggan> findAll() throws SQLException {
        String sql = BASE_SELECT + "ORDER BY pl.ID_Pelanggan ASC";
        List<Pelanggan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<LevelPelanggan> findAllLevels() throws SQLException {
        String sql = "SELECT ID_Level, Nama_level, Diskon_level, Harga_level FROM Level_Pelanggan ORDER BY ID_Level ASC";
        List<LevelPelanggan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LevelPelanggan lv = new LevelPelanggan();
                lv.setIdLevel(rs.getLong("ID_Level"));
                lv.setNamaLevel(rs.getString("Nama_level"));
                lv.setDiskonLevel(rs.getInt("Diskon_level"));
                lv.setHargaLevel(rs.getInt("Harga_level"));
                list.add(lv);
            }
        }
        return list;
    }

    public Pelanggan findById(long idPelanggan) throws SQLException {
        String sql = BASE_SELECT + "WHERE pl.ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPelanggan);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Pelanggan findByEmail(String email) throws SQLException {
        String sql = BASE_SELECT + "WHERE LOWER(pl.Gmail_Pelanggan) = LOWER(?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public long register(String nama, String email, String plainPassword) throws SQLException {
        String sql = "INSERT INTO Pelanggan (ID_Level, Nama_Pelanggan, Gmail_Pelanggan, Password_Hash, Role, Saldo, Status_Akun) "
                + "VALUES (1, ?, ?, ?, 'Customer', 0, 'Active')";
        String hash = PasswordUtil.hashPassword(plainPassword);
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nama);
            ps.setString(2, email);
            ps.setString(3, hash);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }
        return 0;
    }

    public boolean updateProfile(long idPelanggan, String nama, String newPasswordOrBlank) throws SQLException {
        if (newPasswordOrBlank != null && !newPasswordOrBlank.trim().isEmpty()) {
            String sql = "UPDATE Pelanggan SET Nama_Pelanggan = ?, Password_Hash = ? WHERE ID_Pelanggan = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nama);
                ps.setString(2, PasswordUtil.hashPassword(newPasswordOrBlank.trim()));
                ps.setLong(3, idPelanggan);
                return ps.executeUpdate() > 0;
            }
        } else {
            String sql = "UPDATE Pelanggan SET Nama_Pelanggan = ? WHERE ID_Pelanggan = ?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, nama);
                ps.setLong(2, idPelanggan);
                return ps.executeUpdate() > 0;
            }
        }
    }

    public boolean topUpSaldo(long idPelanggan, long amount) throws SQLException {
        if (amount <= 0) {
            return false;
        }
        String sql = "UPDATE Pelanggan SET Saldo = Saldo + ? WHERE ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, amount);
            ps.setLong(2, idPelanggan);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean adminUpdate(long idPelanggan, long idLevel, long saldo, String statusAkun) throws SQLException {
        String sql = "UPDATE Pelanggan SET ID_Level = ?, Saldo = ?, Status_Akun = ? WHERE ID_Pelanggan = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idLevel);
            ps.setLong(2, saldo);
            ps.setString(3, statusAkun);
            ps.setLong(4, idPelanggan);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(long idPelanggan) throws SQLException {
        String sql = "DELETE FROM Pelanggan WHERE ID_Pelanggan = ? AND Role <> 'Admin'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPelanggan);
            return ps.executeUpdate() > 0;
        }
    }

    private Pelanggan mapRow(ResultSet rs) throws SQLException {
        Pelanggan pl = new Pelanggan();
        pl.setIdPelanggan(rs.getLong("ID_Pelanggan"));
        pl.setIdLevel(rs.getLong("ID_Level"));
        pl.setNamaLevel(rs.getString("Nama_level"));
        pl.setDiskonLevel(rs.getInt("Diskon_level"));
        pl.setNamaPelanggan(rs.getString("Nama_Pelanggan"));
        pl.setGmailPelanggan(rs.getString("Gmail_Pelanggan"));
        pl.setPasswordHash(rs.getString("Password_Hash"));
        pl.setRole(rs.getString("Role"));
        pl.setResellerValidDate(rs.getDate("Reseller_Valid_Date"));
        pl.setTotalPembelian(rs.getLong("Total_Pembelian"));
        pl.setSaldo(rs.getLong("Saldo"));
        pl.setStatusAkun(rs.getString("Status_Akun"));
        return pl;
    }
}

