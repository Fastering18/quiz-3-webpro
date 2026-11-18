package com.zstore.model;

import java.sql.Timestamp;

public class Invoice {
    private long idInvoice;
    private String provider;
    private String providerPaymentId;
    private String paymentLink;
    private String externalId;
    private int fee;
    private Long netAmount;
    private Timestamp paidAt;
    private Timestamp expiresAt;
    private String status;

    public long getIdInvoice() { return idInvoice; }
    public void setIdInvoice(long idInvoice) { this.idInvoice = idInvoice; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderPaymentId() { return providerPaymentId; }
    public void setProviderPaymentId(String providerPaymentId) { this.providerPaymentId = providerPaymentId; }

    public String getPaymentLink() { return paymentLink; }
    public void setPaymentLink(String paymentLink) { this.paymentLink = paymentLink; }

    public String getExternalId() { return externalId; }
    public void setExternalId(String externalId) { this.externalId = externalId; }

    public int getFee() { return fee; }
    public void setFee(int fee) { this.fee = fee; }

    public Long getNetAmount() { return netAmount; }
    public void setNetAmount(Long netAmount) { this.netAmount = netAmount; }

    public Timestamp getPaidAt() { return paidAt; }
    public void setPaidAt(Timestamp paidAt) { this.paidAt = paidAt; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isExternalPaymentLink() {
        return paymentLink != null && (paymentLink.startsWith("http://") || paymentLink.startsWith("https://"));
    }
}
