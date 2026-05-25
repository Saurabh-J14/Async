package com.async.app.network;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.Path;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.DELETE;
import java.util.List;

import com.async.app.model.Project;
import com.async.app.model.Task;
import com.async.app.model.User;
import com.async.app.network.model.LoginRequest;
import com.async.app.network.model.RegisterRequest;
import com.async.app.network.model.CreateTaskRequest;
import com.async.app.network.model.CreateProjectRequest;
import com.async.app.network.model.UpdateProjectRequest;

public interface ApiService {

    @POST("user/signup")
    Call<ResponseBody> registerUser(@Body RegisterRequest request);

    @POST("user/login")
    Call<ResponseBody> loginUser(@Body LoginRequest request);

    @POST("tasks/create_task")
    Call<ResponseBody> createTask(@Body CreateTaskRequest request);

    @GET("tasks/all_tasks")
    Call<List<Task>> getTasks();

    @GET("user/by_department")
    Call<List<User>> getEmployees();

    @GET("user/projects")
    Call<List<Project>> getProjects();

    @GET("user/is_auth")
    Call<ResponseBody> isAuth(@Header("Authorization") String authHeader);

    @Multipart
    @PUT("user/profile")
    Call<ResponseBody> updateProfile(
        @Header("Authorization") String authHeader,
        @Part("name") okhttp3.RequestBody name,
        @Part("username") okhttp3.RequestBody username,
        @Part("password") okhttp3.RequestBody password,
        @Part okhttp3.MultipartBody.Part avatar
    );

    @Multipart
    @PUT("user/users/{user_id}")
    Call<ResponseBody> updateEmployeeProfile(
        @Header("Authorization") String authHeader,
        @Path("user_id") String userId,
        @Part("name") okhttp3.RequestBody name,
        @Part("username") okhttp3.RequestBody username,
        @Part("password") okhttp3.RequestBody password,
        @Part("role") okhttp3.RequestBody role,
        @Part("department") okhttp3.RequestBody department,
        @Part okhttp3.MultipartBody.Part avatar
    );

    @POST("user/create_projects")
    Call<ResponseBody> createProject(@Body CreateProjectRequest request);

    @GET("user/projects")
    Call<List<Project>> getUserProjects();

    @GET("user/by_department")
    Call<List<User>> getDepartmentColleagues(@Header("Authorization") String authHeader);

    @GET("user/leaderboard")
    Call<okhttp3.ResponseBody> getLeaderboard(@Header("Authorization") String authHeader);

    @DELETE("user/project_delete/{project_id}")
    Call<ResponseBody> deleteProject(
        @Header("Authorization") String authHeader,
        @Path("project_id") int projectId
    );

    @DELETE("user/delete_user/{user_id}")
    Call<ResponseBody> deleteUser(
        @Header("Authorization") String authHeader,
        @Path("user_id") int userId
    );

    @PUT("user/update_project/{project_id}")
    Call<ResponseBody> updateProject(
        @Header("Authorization") String authHeader,
        @Path("project_id") int projectId,
        @Body UpdateProjectRequest request
    );
}
