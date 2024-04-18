package com.example.ggwave;

import com.google.gson.annotations.SerializedName;

public class AttendanceDTO {
    @SerializedName("lecture")
    private String lecture;
    @SerializedName("attendedAt")
    private String attendedAt;
    public AttendanceDTO(String attendedAt, String lecture) {
        this.attendedAt = attendedAt;
        this.lecture = lecture;
    }

    public String getAttendedAt() {
        return attendedAt;
    }
    public String getLecture() {
        return lecture;
    }
}
