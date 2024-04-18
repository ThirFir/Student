package com.example.ggwave;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ServerApi {
    @POST("")
    Call<AttendanceDTO> postDecodedKey(@Body KeyReq req);


}
