package com.example.ggwave;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ggwave.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {
    private ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());


    }
}

