package com.example.ggwave;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ggwave.databinding.ItemNBinding;

public class NAdapter extends ListAdapter<Integer, RecyclerView.ViewHolder> {

    public NAdapter() {
        super(diffCallback);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NViewHolder(ItemNBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((NViewHolder) holder).bind(getItem(position));
    }

    static class NViewHolder extends RecyclerView.ViewHolder {
        ItemNBinding mBinding;
        public NViewHolder(ItemNBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(Integer n) {
            mBinding.tvNValue.setText(String.valueOf(n));
        }
    }

    static DiffUtil.ItemCallback<Integer> diffCallback = new DiffUtil.ItemCallback<Integer>() {
        @Override
        public boolean areItemsTheSame(@NonNull Integer oldItem, @NonNull Integer newItem) {
            return oldItem.intValue() == newItem.intValue();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Integer oldItem, @NonNull Integer newItem) {
            return oldItem.equals(newItem);
        }
    };
}
