package com.example.productsaleandroid.models;

import java.io.Serializable;

public class UserVoucher implements Serializable {
    public int userVoucherId;
    public int userId;
    public boolean redeemed;
    public Voucher voucher;

    public static class Voucher implements Serializable {
        public int voucherId;
        public String code;
        public int discountPercent;
        public String expiryDate;
        public int quantity;
        public String createdAt;
    }
}
