package com.zstore.model;

public class DetailTransaksi {
    private long idDetailTrx;
    private long idTrx;
    private long idStk;
    private String namaProduk;
    private String kodeLisensi;
    private int hargaBeli;

    public long getIdDetailTrx() { return idDetailTrx; }
    public void setIdDetailTrx(long idDetailTrx) { this.idDetailTrx = idDetailTrx; }

    public long getIdTrx() { return idTrx; }
    public void setIdTrx(long idTrx) { this.idTrx = idTrx; }

    public long getIdStk() { return idStk; }
    public void setIdStk(long idStk) { this.idStk = idStk; }

    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }

    public String getKodeLisensi() { return kodeLisensi; }
    public void setKodeLisensi(String kodeLisensi) { this.kodeLisensi = kodeLisensi; }

    public int getHargaBeli() { return hargaBeli; }
    public void setHargaBeli(int hargaBeli) { this.hargaBeli = hargaBeli; }
}

