package com.zstore.model;

import java.util.ArrayList;
import java.util.List;

public class Produk {
    private long idProduk;
    private long idProvider;
    private String providerNama;
    private String namaProduk;
    private int hargaProduk;
    private String jenisProduk;
    private String statusProduk;
    private String deskripsi;
    private String thumbnailImg;
    private int availableStock;
    private int soldStock;
    private List<Tag> tags = new ArrayList<>();

    public long getIdProduk() { return idProduk; }
    public void setIdProduk(long idProduk) { this.idProduk = idProduk; }

    public long getIdProvider() { return idProvider; }
    public void setIdProvider(long idProvider) { this.idProvider = idProvider; }

    public String getProviderNama() { return providerNama; }
    public void setProviderNama(String providerNama) { this.providerNama = providerNama; }

    public String getNamaProduk() { return namaProduk; }
    public void setNamaProduk(String namaProduk) { this.namaProduk = namaProduk; }

    public int getHargaProduk() { return hargaProduk; }
    public void setHargaProduk(int hargaProduk) { this.hargaProduk = hargaProduk; }

    public String getJenisProduk() { return jenisProduk; }
    public void setJenisProduk(String jenisProduk) { this.jenisProduk = jenisProduk; }

    public String getStatusProduk() { return statusProduk; }
    public void setStatusProduk(String statusProduk) { this.statusProduk = statusProduk; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getThumbnailImg() { return thumbnailImg; }
    public void setThumbnailImg(String thumbnailImg) { this.thumbnailImg = thumbnailImg; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public int getSoldStock() { return soldStock; }
    public void setSoldStock(int soldStock) { this.soldStock = soldStock; }

    public int getTotalStock() { return availableStock + soldStock; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}

