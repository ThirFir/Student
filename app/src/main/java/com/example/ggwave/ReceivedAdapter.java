package com.example.ggwave;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ggwave.databinding.ItemNBinding;

public class ReceivedAdapter extends ListAdapter<String, RecyclerView.ViewHolder> {

    public ReceivedAdapter() {
        super(diffCallback);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ReceivedViewHolder(ItemNBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ReceivedViewHolder) holder).bind(getItem(position));
    }

    static class ReceivedViewHolder extends RecyclerView.ViewHolder {
        ItemNBinding mBinding;

        public ReceivedViewHolder(ItemNBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(String received) {
            mBinding.tvNValue.setText(received);
        }
    }

    static DiffUtil.ItemCallback<String> diffCallback = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(@NonNull String oldItem, @NonNull String newItem) {
            return oldItem.equals(newItem);
        }
    };
}
