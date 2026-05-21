package com.async.app.network;

import com.async.app.network.model.CreateTaskRequest;
import com.async.app.network.model.RegisterRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("user/register")
    Call<ResponseBody> registerUser(@Body RegisterRequest request);

    @POST("task/create_task")
    Call<ResponseBody> createTask(@Body CreateTaskRequest request);
}
