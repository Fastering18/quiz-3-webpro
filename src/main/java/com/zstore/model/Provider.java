package com.zstore.model;

public class Provider {
    private long idProvider;
    private String nama;
    private String thumbnailImg;
    private int productCount;

    public long getIdProvider() { return idProvider; }
    public void setIdProvider(long idProvider) { this.idProvider = idProvider; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getThumbnailImg() { return thumbnailImg; }
    public void setThumbnailImg(String thumbnailImg) { this.thumbnailImg = thumbnailImg; }

    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }
}

