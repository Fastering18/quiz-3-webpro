package com.zstore.model;

import java.sql.Date;

public class Stock {
    private long idStk;
    private long idProduk;
    private String namaProduk;
    private String kodeLisensi;
    private long creditValue;
    private Date claimedAt;
    private Date expiredAt;
    private String status;

    public long getIdStk() { return idStk; }
    public void setIdStk(long idStk) { this.idStk = idStk; }

    public long getIdProduk() { return idProduk; }
    public void setIdProduk(long idProduk) { this.idProduk = idProduk; }

    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }

    public String getKodeLisensi() { return kodeLisensi; }
    public void setKodeLisensi(String kodeLisensi) { this.kodeLisensi = kodeLisensi; }

    public long getCreditValue() { return creditValue; }
    public void setCreditValue(long creditValue) { this.creditValue = creditValue; }

    public Date getClaimedAt() { return claimedAt; }
    public void setClaimedAt(Date claimedAt) { this.claimedAt = claimedAt; }

    public Date getExpiredAt() { return expiredAt; }
    public void setExpiredAt(Date expiredAt) { this.expiredAt = expiredAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

