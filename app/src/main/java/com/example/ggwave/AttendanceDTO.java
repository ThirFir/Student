package com.example.ggwave;

import com.google.gson.annotations.SerializedName;

public class AttendanceDTO {
    @SerializedName("attend")
    private boolean isAttend;
    public AttendanceDTO(boolean isAttend) {
        this.isAttend = isAttend;
    }

    public boolean getIsAttend() {
        return isAttend;
    }
}
