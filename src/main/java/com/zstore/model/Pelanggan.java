package com.zstore.model;

import com.zstore.util.FormatUtil;
import java.sql.Date;

public class Pelanggan {
    private long idPelanggan;
    private long idLevel;
    private String namaLevel;
    private int diskonLevel;
    private String namaPelanggan;
    private String gmailPelanggan;
    private String passwordHash;
    private String role;
    private Date resellerValidDate;
    private long totalPembelian;
    private long saldo;
    private String statusAkun;
    private String avatarImg;

    public long getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(long idPelanggan) { this.idPelanggan = idPelanggan; }

    public long getIdLevel() { return idLevel; }
    public void setIdLevel(long idLevel) { this.idLevel = idLevel; }

    public String getNamaLevel() { return namaLevel; }
    public void setNamaLevel(String namaLevel) { this.namaLevel = namaLevel; }

    public int getDiskonLevel() { return diskonLevel; }
    public void setDiskonLevel(int diskonLevel) { this.diskonLevel = diskonLevel; }

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public String getGmailPelanggan() { return gmailPelanggan; }
    public void setGmailPelanggan(String gmailPelanggan) { this.gmailPelanggan = gmailPelanggan; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Date getResellerValidDate() { return resellerValidDate; }
    public void setResellerValidDate(Date resellerValidDate) { this.resellerValidDate = resellerValidDate; }

    public long getTotalPembelian() { return totalPembelian; }
    public void setTotalPembelian(long totalPembelian) { this.totalPembelian = totalPembelian; }

    public long getSaldo() { return saldo; }
    public void setSaldo(long saldo) { this.saldo = saldo; }

    public String getStatusAkun() { return statusAkun; }
    public void setStatusAkun(String statusAkun) { this.statusAkun = statusAkun; }

    public String getAvatarImg() { return avatarImg; }
    public void setAvatarImg(String avatarImg) { this.avatarImg = avatarImg; }

    public boolean isHasAvatar() {
        return !FormatUtil.trimOrEmpty(avatarImg).isEmpty();
    }

    public String avatarSrc(String contextPath) {
        return isHasAvatar() ? FormatUtil.resolveImageUrl(contextPath, avatarImg, "") : "";
    }

    public String getAvatarFileName() {
        return FormatUtil.extractFileName(avatarImg);
    }

    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }

    public String getInitial() {
        if (namaPelanggan != null && !namaPelanggan.isEmpty()) {
            return namaPelanggan.substring(0, 1).toUpperCase();
        }
        return "U";
    }
}
