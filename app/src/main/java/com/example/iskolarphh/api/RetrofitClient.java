package com.example.iskolarphh.api;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private final ApiClientConfig config;
    private Retrofit retrofit;
    private LongcatApiService apiService;

    public RetrofitClient(ApiClientConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("ApiClientConfig cannot be null");
        }
        this.config = config;
    }

    public RetrofitClient(String baseUrl, String apiKey) {
        this(new ApiClientConfig(baseUrl, apiKey));
    }

    public LongcatApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.Request.Builder builder = original.newBuilder()
                                .header("Authorization", "Bearer " + config.getApiKey())
                                .header("Content-Type", "application/json");
                        okhttp3.Request request = builder.build();
                        return chain.proceed(request);
                    })
                    .connectTimeout(config.getConnectTimeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS)
                    .readTimeout(config.getReadTimeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS);

            if (config.isLoggingEnabled()) {
                clientBuilder.addInterceptor(logging);
            }

            OkHttpClient client = clientBuilder.build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(config.getBaseUrl())
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(LongcatApiService.class);
        }
        return apiService;
    }
}
