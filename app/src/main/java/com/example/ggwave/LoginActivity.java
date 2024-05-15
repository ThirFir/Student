package com.example.ggwave;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ggwave.databinding.ActivityLoginBinding;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    private OkHttpClient client;
    private Retrofit retrofit;
    private ServerApi api;
    private static final String BASE_URL = "http://15.165.236.170:5000/student/";
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initRetrofit();

        binding.btnLogin.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            api.getIsLoginSuccess("2019136002").enqueue(
                    new Callback<Boolean>() {
                        @Override
                        public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                            if (response.isSuccessful()) {
                                if (Boolean.TRUE.equals(response.body())) {
                                    startActivity(intent);
                                } else {
                                    binding.etStudentId.setError("학번을 다시 확인해주세요.");
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Boolean> call, Throwable t) {
                            Toast.makeText(LoginActivity.this, "서버 통신 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
            );
            intent.putExtra("id", Objects.requireNonNull(binding.etStudentId.getText()).toString());
            startActivity(intent);
        });
    }

    private void initRetrofit() {
        client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
        retrofit = new Retrofit.Builder()
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(BASE_URL)
                .build();
        api = retrofit.create(ServerApi.class);
    }
}