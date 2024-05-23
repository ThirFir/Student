package com.example.ggwave;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ggwave.databinding.ItemAttendanceResultBinding;

import java.util.ArrayList;

public class AttendanceResultAdapter extends RecyclerView.Adapter<AttendanceResultAdapter.AttendanceResultViewHolder> {

    public ArrayList<AttendanceDTO> resultData = new ArrayList();
    private final Context mContext;

    public AttendanceResultAdapter(Context context) {
        super();
        mContext = context;
    }

    @NonNull
    @Override
    public AttendanceResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance_result, parent, false);
        ItemAttendanceResultBinding binding = ItemAttendanceResultBinding.inflate(LayoutInflater.from(mContext), parent, false);
        return new AttendanceResultViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceResultViewHolder holder, int position) {

        holder.onBind(position);
    }

    @Override
    public int getItemCount() {
        return resultData.size();
    }

    class AttendanceResultViewHolder extends RecyclerView.ViewHolder {
        ItemAttendanceResultBinding mBinding;

        public AttendanceResultViewHolder(ItemAttendanceResultBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void onBind(int pos) {
            mBinding.tvLecture.setText(resultData.get(pos).getLecture());
            mBinding.tvWeek.setText("1주차");
            mBinding.tvIsAttendance.setText("출석");
            mBinding.tvAttendanceTime.setText(resultData.get(pos).getAttendedAt());
        }
    }
}
