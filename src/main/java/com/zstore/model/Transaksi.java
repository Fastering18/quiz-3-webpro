package com.zstore.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Transaksi {
    private long idTrx;
    private Long idPelanggan;
    private String namaPelanggan;
    private long idInvoice;
    private Invoice invoice;
    private Timestamp tanggalTrx;
    private long totalPembayaran;
    private String metodePembayaran;
    private String status;
    private String gmailTrx;
    private String namaPenerima;
    private List<DetailTransaksi> details = new ArrayList<>();

    public long getIdTrx() { return idTrx; }
    public void setIdTrx(long idTrx) { this.idTrx = idTrx; }

    public Long getIdPelanggan() { return idPelanggan; }
    public void setIdPelanggan(Long idPelanggan) { this.idPelanggan = idPelanggan; }

    public String getNamaPelanggan() { return namaPelanggan; }
    public void setNamaPelanggan(String namaPelanggan) { this.namaPelanggan = namaPelanggan; }

    public long getIdInvoice() { return idInvoice; }
    public void setIdInvoice(long idInvoice) { this.idInvoice = idInvoice; }

    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }

    public Timestamp getTanggalTrx() { return tanggalTrx; }
    public void setTanggalTrx(Timestamp tanggalTrx) { this.tanggalTrx = tanggalTrx; }

    public long getTotalPembayaran() { return totalPembayaran; }
    public void setTotalPembayaran(long totalPembayaran) { this.totalPembayaran = totalPembayaran; }

    public String getMetodePembayaran() { return metodePembayaran; }
    public void setMetodePembayaran(String metodePembayaran) { this.metodePembayaran = metodePembayaran; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getGmailTrx() { return gmailTrx; }
    public void setGmailTrx(String gmailTrx) { this.gmailTrx = gmailTrx; }

    public String getNamaPenerima() { return namaPenerima; }
    public void setNamaPenerima(String namaPenerima) { this.namaPenerima = namaPenerima; }

    public List<DetailTransaksi> getDetails() { return details; }
    public void setDetails(List<DetailTransaksi> details) { this.details = details; }

    public String getStatusClass() {
        if (status == null) {
            return "pending";
        }
        String s = status.toLowerCase();
        if (s.equals("completed") || s.equals("berhasil") || s.equals("success")) {
            return "completed";
        }
        if (s.equals("failed") || s.equals("gagal") || s.equals("cancelled") || s.equals("expired")) {
            return "failed";
        }
        return "pending";
    }
}

