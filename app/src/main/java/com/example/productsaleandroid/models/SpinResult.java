package com.example.productsaleandroid.models;

public class SpinResult {
    private boolean success;
    private String message;
    private Data data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData() { return data; }

    public static class Data {
        private Voucher voucher;

        public Voucher getVoucher() { return voucher; }
    }

    public static class Voucher {
        private int voucherId;
        private String code;
        private int discountPercent;
        private String expiryDate;
        private int quantity;
        private String createdAt;

        public int getVoucherId() { return voucherId; }
        public String getCode() { return code; }
        public int getDiscountPercent() { return discountPercent; }
        public String getExpiryDate() { return expiryDate; }
        public int getQuantity() { return quantity; }
        public String getCreatedAt() { return createdAt; }
    }
}
