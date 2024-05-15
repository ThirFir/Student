package com.example.ggwave;

import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AttendanceResultAdapter extends RecyclerView.Adapter<AttendanceResultAdapter.AttendanceResultViewHolder> {

    public ArrayList<AttendanceDTO> resultData = new ArrayList();
    @NonNull
    @Override
    public AttendanceResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_result, parent, false);
        return new AttendanceResultViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceResultViewHolder holder, int position) {
        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class AttendanceResultViewHolder extends RecyclerView.ViewHolder {
        TextView lecture_title;
        TextView week;
        TextView attendance;
        TextView timeline;
        public AttendanceResultViewHolder(@NonNull View itemView) {
            super(itemView);

            lecture_title = (TextView) itemView.findViewById(R.id.tv_lecture);
            week = (TextView) itemView.findViewById(R.id.tv_date);
            attendance = (TextView) itemView.findViewById(R.id.tv_is_checked);
            timeline = (TextView) itemView.findViewById(R.id.tv_checked_time);
        }

        public void onBind(int pos) {
            lecture_title.setText(resultData.get(pos).getLecture());
            timeline.setText(resultData.get(pos).getAttendedAt());
            week.setText("1주차");
            attendance.setText("출석");
        }
    }
}
