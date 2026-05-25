package com.async.app.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String BASE_URL = "http://192.168.1.76:8000/";
    private static ApiService apiService = null;

    public static synchronized ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.Request.Builder requestBuilder = original.newBuilder();
                        
                        String path = original.url().encodedPath();
                        if (!path.contains("/user/login") && !path.contains("/user/signup") && !path.contains("/user/register")) {
                            com.async.app.AsyncApplication app = com.async.app.AsyncApplication.getInstance();
                            if (app != null) {
                                String token = com.async.app.util.SessionManager.getInstance(app).getToken();
                                if (token != null && !token.trim().isEmpty()) {
                                    requestBuilder.header("Authorization", "Bearer " + token);
                                }
                            }
                        }
                        
                        return chain.proceed(requestBuilder.build());
                    })
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
