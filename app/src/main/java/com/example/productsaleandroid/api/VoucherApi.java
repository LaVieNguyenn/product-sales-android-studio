package com.example.productsaleandroid.api;

import com.example.productsaleandroid.VoucherListActivity;
import com.example.productsaleandroid.models.ApplyVoucherResponse;
import com.example.productsaleandroid.models.UserVoucher;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

import java.util.List;
import java.util.Map;

public interface VoucherApi {
    @GET("api/vouchers/my")
    Call<VoucherListActivity.VoucherListResponse> getMyVouchers(@Header("Authorization") String token);
    @POST("api/carts/apply-user-voucher")
    Call<ApplyVoucherResponse> applyUserVoucher(@Header("Authorization") String token, @Body Map<String, Integer> body);
}
