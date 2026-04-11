package com.example.iskolarphh.api;

import com.example.iskolarphh.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.longcat.chat/openai/";
    private static Retrofit retrofit = null;
    private static LongcatApiService apiService = null;

    public static LongcatApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.Request.Builder builder = original.newBuilder()
                                .header("Authorization", "Bearer " + BuildConfig.LONGCAT_API_KEY)
                                .header("Content-Type", "application/json");
                        okhttp3.Request request = builder.build();
                        return chain.proceed(request);
                    })
                    .addInterceptor(logging)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(LongcatApiService.class);
        }
        return apiService;
    }
}
