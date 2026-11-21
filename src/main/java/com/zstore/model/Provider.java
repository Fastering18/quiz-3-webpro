package com.zstore.model;

import com.zstore.util.FormatUtil;

public class Provider {
    private static final String DEFAULT_THUMB = "/static/Image/Logo/Logo.png";

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

    public String thumbnailSrc(String contextPath) {
        return FormatUtil.resolveImageUrl(contextPath, thumbnailImg, DEFAULT_THUMB);
    }

    public String getThumbnailFileName() {
        return FormatUtil.extractFileName(thumbnailImg);
    }

    public boolean isHasCustomThumbnail() {
        String trimmed = FormatUtil.trimOrEmpty(thumbnailImg);
        return !trimmed.isEmpty() && !DEFAULT_THUMB.equals(trimmed);
    }

    public int getProductCount() { return productCount; }
    public void setProductCount(int productCount) { this.productCount = productCount; }
}
