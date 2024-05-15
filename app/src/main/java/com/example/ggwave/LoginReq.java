package com.example.ggwave;

import com.google.gson.annotations.SerializedName;

public class LoginReq {
    @SerializedName("id")
    private String id;

    public LoginReq(String id) {
        this.id = id;
    }
    public String getId() {
        return id;
    }
}
