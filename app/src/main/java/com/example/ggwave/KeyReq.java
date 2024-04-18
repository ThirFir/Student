package com.example.ggwave;

import com.google.gson.annotations.SerializedName;

public class KeyReq {
    @SerializedName("key")
    private String key;
    @SerializedName("id")
    private String id;

    public KeyReq(String key, String id) {
        this.key = key;
        this.id = id;
    }

    public String getKey() {
        return key;
    }
    public String getId() {
        return id;
    }
}
