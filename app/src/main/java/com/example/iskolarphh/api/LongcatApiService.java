package com.example.iskolarphh.api;

import com.example.iskolarphh.model.LongcatRequest;
import com.example.iskolarphh.model.LongcatResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LongcatApiService {
    @POST("v1/chat/completions")
    Call<LongcatResponse> sendMessage(@Body LongcatRequest request);
}
