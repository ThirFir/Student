package com.example.ggwave;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ggwave.databinding.ActivityMainBinding;

import java.io.IOException;
import java.nio.ShortBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;
    private String studentId = "20191111";

    private String kMessageToSend = "Hello Android!";
    private CapturingThread mCapturingThread;
    private RtNoiseReducer rtNoiseReducer;
    private PlaybackThread mPlaybackThread;
    private static final int REQUEST_RECORD_AUDIO = 13;

    // TODO : BASE URL
    private static final String BASE_URL = "http://15.165.236.170:5000/student/";
    private OkHttpClient client;
    private Retrofit retrofit;
    private ServerApi api;

    private AttendanceResultAdapter mAdapter;
    private ReceivedAdapter mReceivedAdapter;
    private NAdapter mNAdapter;
    private List<Integer> nList;
    private List<String> receivedList;

    // Native interface:
    private native void initNative();

    private native void processCaptureData(short[] data);

    private native void sendMessage(String message);

    private boolean isChecked = false;

    private void onNativeReceivedMessage(byte c_message[]) {
        String message = new String(c_message);
        Log.d("dhk", "Received Message: " + message);

        if (isChecked)
            return;
        if (api != null) {
            api.postDecodedKey(new KeyReq(message, studentId)).enqueue(new Callback<AttendanceDTO>() {
                @Override
                public void onResponse(@NonNull Call<AttendanceDTO> call, @NonNull Response<AttendanceDTO> response) {
                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            if (response.body().getLecture() != null) {
                                AttendanceDTO attendance = response.body();
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "출석 처리되었습니다.", Toast.LENGTH_SHORT).show();
                                    nList.add(nList.size() + 1);
                                    receivedList.add(message);
                                    mNAdapter.submitList(nList);
                                    mReceivedAdapter.submitList(receivedList);
                                    
                                    binding.lottieReceiving.setVisibility(View.INVISIBLE);
                                    binding.lottieChecked.setVisibility(View.VISIBLE);
                                    binding.lottieChecked.playAnimation();

                                    isChecked = true;
                                });
                            } else
                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "출석 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                });
                        }
                    } else {
                        Log.d("ggwave", "Response: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<AttendanceDTO> call, Throwable t) {
                    Log.d("ggwave", "Failure: " + t.getMessage());
                    Toast.makeText(MainActivity.this, "출석 실패하였습니다.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            runOnUiThread(() -> {
                Log.d("ggwave", "api is null");
                binding.lottieReceiving.setVisibility(View.INVISIBLE);
                binding.lottieChecked.setVisibility(View.VISIBLE);
                binding.lottieChecked.playAnimation();

            });
        }

        runOnUiThread(() -> {
            mCapturingThread.stopCapturing();
            binding.textViewStatusInp.setText("Status: Idle");
            //binding.textViewReceived.setText(stringToBits(message));
        });
    }

    private void onNativeMessageEncoded(short c_message[]) {
        Log.v("ggwave", "Playing encoded message ..");

        mPlaybackThread = new PlaybackThread(c_message, new PlaybackListener() {
            @Override
            public void onProgress(int progress) {
                // todo : progress updates
            }

            @Override
            public void onCompletion() {
                mPlaybackThread.stopPlayback();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAdapter = new AttendanceResultAdapter(getBaseContext());
        mReceivedAdapter = new ReceivedAdapter();
        mNAdapter = new NAdapter();
        binding.rvN.setAdapter(mNAdapter);
        binding.rvString.setAdapter(mReceivedAdapter);
        binding.rvN.setLayoutManager(new LinearLayoutManager(this));
        binding.rvString.setLayoutManager(new LinearLayoutManager(this));

        System.loadLibrary("test-cpp");
        initNative();
        initRTNR();
        initCheckResult();
        //initRetrofitApi();

        mCapturingThread = new CapturingThread(new AudioDataReceivedListener() {
            @Override
            public void onAudioDataReceived(short[] data) {
                //Log.v("ggwave", "java: 0 = " + data[0]);
                processCaptureData(data);
            }
        });

        binding.lottieReceiving.setOnClickListener(v -> {
            if (!mCapturingThread.capturing()) {
                startAudioCapturingSafe();
                binding.lottieReceiving.playAnimation();
                binding.lottieReceiving.setProgress(0.33f);
            } else {
                mCapturingThread.stopCapturing();
                binding.lottieReceiving.cancelAnimation();
                binding.lottieReceiving.setProgress(0.33f);
            }
        });
    }

    private void startAudioCapturingSafe() {
        Log.i("ggwave", "startAudioCapturingSafe");

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            Log.i("ggwave", " - record permission granted");
            mCapturingThread.startCapturing();
        } else {
            Log.i("ggwave", " - record permission NOT granted");
            requestMicrophonePermission();
        }
    }

    private void requestMicrophonePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.RECORD_AUDIO)) {
            new AlertDialog.Builder(this)
                    .setTitle("Microphone Access Requested")
                    .setMessage("Microphone access is required in order to receive audio messages")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                                    android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
                        }
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mCapturingThread.stopCapturing();
        }
    }

    private void initRetrofitApi() {
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

    private double[] convertByteArrayToDouble(byte[] byteArray) {
        // 바이트 배열의 길이가 짝수가 아니거나 바이트 배열이 null이면 변환할 수 없음
        if (byteArray == null || byteArray.length % 2 != 0) {
            return null;
        }

        int shortArrayLength = byteArray.length / 2;
        short[] shortArray = new short[shortArrayLength];

        // 바이트 배열을 short 배열로 변환
        for (int i = 0; i < shortArrayLength; i++) {
            shortArray[i] = (short) (((byteArray[i * 2 + 1] & 0xFF) << 8) | (byteArray[i * 2] & 0xFF));
        }

        // short 배열을 double 배열로 변환
        double[] doubleArray = new double[shortArrayLength];
        for (int i = 0; i < shortArrayLength; i++) {
            doubleArray[i] = shortArray[i];
        }

        return doubleArray;
    }

    public String stringToBits(String input) {
        StringBuilder binary = new StringBuilder();
        for (char c : input.toCharArray()) {
            String binaryString = Integer.toBinaryString(c);
            binary.append(String.format("%8s", binaryString).replace(' ', '0'));
            binary.append("\n");
        }
        return binary.toString();
    }

    void initRTNR() {
        try {
            rtNoiseReducer = new RtNoiseReducer(this);
        } catch (IOException e) {
            Log.d("class", "Failed to create noise reduction");
        }
    }

    public static byte[] convertDoubleArrayToByteArray(double[] doubleArray) {
        if (doubleArray == null) {
            return null;
        }

        int shortArrayLength = doubleArray.length;
        short[] shortArray = new short[shortArrayLength];

        // double 배열을 short 배열로 변환
        for (int i = 0; i < shortArrayLength; i++) {
            shortArray[i] = (short) doubleArray[i];
        }

        // short 배열을 바이트 배열로 변환
        byte[] byteArray = new byte[shortArrayLength * 2];
        for (int i = 0; i < shortArrayLength; i++) {
            // little-endian 형태로 short 값을 바이트 배열로 변환
            byteArray[i * 2] = (byte) (shortArray[i] & 0xFF);
            byteArray[i * 2 + 1] = (byte) ((shortArray[i] >> 8) & 0xFF);
        }

        return byteArray;
    }

    public void initCheckResult() {
        binding.btnCheckResult.setOnClickListener(v -> {

                }
        );
    }
}

interface AudioDataReceivedListener {
    void onAudioDataReceived(short[] data);
}

class CapturingThread {
    private static final String LOG_TAG = CapturingThread.class.getSimpleName();
    private static final int SAMPLE_RATE = 48000;

    public CapturingThread(AudioDataReceivedListener listener) {
        mListener = listener;
    }

    private boolean mShouldContinue;
    private AudioDataReceivedListener mListener;
    private Thread mThread;

    public boolean capturing() {
        return mThread != null;
    }

    public void startCapturing() {
        if (mThread != null)
            return;

        mShouldContinue = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                capture();
            }
        });
        mThread.start();
    }

    public void stopCapturing() {
        if (mThread == null)
            return;

        mShouldContinue = false;
        mThread = null;
    }

    private void capture() {
        Log.v(LOG_TAG, "Start");
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // buffer size in bytes
        int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        bufferSize = 4 * 1024;

        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize);

        Log.d("ggwave", "buffer size = " + bufferSize);
        Log.d("ggwave", "Sample rate = " + record.getSampleRate());

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }
        record.startRecording();

        Log.v(LOG_TAG, "Start capturing");

        long shortsRead = 0;
        while (mShouldContinue) {
            int numberOfShort = record.read(audioBuffer, 0, audioBuffer.length);
            shortsRead += numberOfShort;

            mListener.onAudioDataReceived(audioBuffer);
        }

        record.stop();
        record.release();

        Log.v(LOG_TAG, String.format("Capturing stopped. Samples read: %d", shortsRead));
    }
}

interface PlaybackListener {
    void onProgress(int progress);

    void onCompletion();
}

class PlaybackThread {
    static final int SAMPLE_RATE = 48000;
    private static final String LOG_TAG = PlaybackThread.class.getSimpleName();

    public PlaybackThread(short[] samples, PlaybackListener listener) {
        mSamples = ShortBuffer.wrap(samples);
        mNumSamples = samples.length;
        mListener = listener;
    }

    private Thread mThread;
    private boolean mShouldContinue;
    private ShortBuffer mSamples;
    private int mNumSamples;
    private PlaybackListener mListener;

    public boolean playing() {
        return mThread != null;
    }

    public void startPlayback() {
        if (mThread != null)
            return;

        // Start streaming in a thread
        mShouldContinue = true;
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                play();
            }
        });
        mThread.start();
    }

    public void stopPlayback() {
        if (mThread == null)
            return;

        mShouldContinue = false;
        mThread = null;
    }

    private void play() {
        int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioTrack.ERROR || bufferSize == AudioTrack.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }

        bufferSize = 16 * 1024;

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);

        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            public void onPeriodicNotification(AudioTrack track) {
                if (mListener != null && track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    mListener.onProgress((track.getPlaybackHeadPosition() * 1000) / SAMPLE_RATE);
                }
            }

            @Override
            public void onMarkerReached(AudioTrack track) {
                Log.v(LOG_TAG, "Audio file end reached");
                track.release();
                if (mListener != null) {
                    mListener.onCompletion();
                }
            }
        });
        audioTrack.setPositionNotificationPeriod(SAMPLE_RATE / 30); // 30 times per second
        audioTrack.setNotificationMarkerPosition(mNumSamples);

        audioTrack.play();

        Log.v(LOG_TAG, "Audio streaming started");

        short[] buffer = new short[bufferSize];
        mSamples.rewind();
        int limit = mNumSamples;
        int totalWritten = 0;
        while (mSamples.position() < limit && mShouldContinue) {
            int numSamplesLeft = limit - mSamples.position();
            int samplesToWrite;
            if (numSamplesLeft >= buffer.length) {
                mSamples.get(buffer);
                samplesToWrite = buffer.length;
            } else {
                for (int i = numSamplesLeft; i < buffer.length; i++) {
                    buffer[i] = 0;
                }
                mSamples.get(buffer, 0, numSamplesLeft);
                samplesToWrite = numSamplesLeft;
            }
            totalWritten += samplesToWrite;
            audioTrack.write(buffer, 0, samplesToWrite);
        }

        if (!mShouldContinue) {
            audioTrack.release();
        }

        Log.v(LOG_TAG, "Audio streaming finished. Samples written: " + totalWritten);
    }
}
