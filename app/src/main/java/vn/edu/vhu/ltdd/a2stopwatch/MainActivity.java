package vn.edu.vhu.ltdd.a2stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "A2_221A010861";

    private static final String KEY_RUNNING = "running";
    private static final String KEY_ACCUMULATED = "accumulated";
    private static final String KEY_START = "start";
    private static final String KEY_RECREATE = "recreate";
    private static final String KEY_LAPS = "laps";
    private static final String KEY_STOP_BACKGROUND = "stop_background";

    private TextView tvTime, tvStatus, tvRecreate, tvLaps;
    private Button btnStartPause, btnReset, btnLap;
    private CheckBox cbStopWhenBackground;

    private boolean running = false;
    private long accumulated = 0L;
    private long startTime = 0L;
    private int recreateCount = 0;

    private boolean stopWhenBackground = false;

    private ArrayList<String> laps = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final Runnable ticker = new Runnable() {
        @Override
        public void run() {
            updateTimeText();
            handler.postDelayed(this, 100);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvRecreate = findViewById(R.id.tvRecreate);
        tvLaps = findViewById(R.id.tvLaps);

        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);
        btnLap = findViewById(R.id.btnLap);

        cbStopWhenBackground = findViewById(R.id.cbStopWhenBackground);

        if (savedInstanceState != null) {
            running = savedInstanceState.getBoolean(KEY_RUNNING);
            accumulated = savedInstanceState.getLong(KEY_ACCUMULATED);
            startTime = savedInstanceState.getLong(KEY_START);
            recreateCount = savedInstanceState.getInt(KEY_RECREATE) + 1;

            stopWhenBackground =
                    savedInstanceState.getBoolean(KEY_STOP_BACKGROUND);

            ArrayList<String> savedLaps =
                    savedInstanceState.getStringArrayList(KEY_LAPS);

            if (savedLaps != null) {
                laps = savedLaps;
            }

            Log.d(TAG, "onCreate: KHÔI PHỤC trạng thái");
        } else {
            Log.d(TAG, "onCreate: khởi tạo mới");
        }

        cbStopWhenBackground.setChecked(stopWhenBackground);

        btnStartPause.setOnClickListener(v -> {
            if (running) {
                pauseStopwatch();
            } else {
                startStopwatch();
            }
        });

        btnReset.setOnClickListener(v -> resetStopwatch());

        btnLap.setOnClickListener(v -> addLap());

        cbStopWhenBackground.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    stopWhenBackground = isChecked;
                }
        );

        updateUi();
    }

    private long elapsed() {
        // Tính thời gian bằng SystemClock
        if (!running) {
            return accumulated;
        }

        return accumulated +
                (SystemClock.elapsedRealtime() - startTime);
    }

    private void startStopwatch() {
        running = true;
        startTime = SystemClock.elapsedRealtime();

        startTicking();
        updateUi();

        Log.i(TAG, "BẮT ĐẦU đếm giờ");
    }

    private void pauseStopwatch() {
        accumulated +=
                SystemClock.elapsedRealtime() - startTime;

        running = false;

        stopTicking();
        updateUi();

        Log.i(TAG, "TẠM DỪNG tại " + accumulated + "ms");
    }

    private void resetStopwatch() {
        running = false;
        accumulated = 0L;
        startTime = 0L;

        laps.clear();

        stopTicking();
        updateUi();

        Log.i(TAG, "ĐẶT LẠI về 00:00.0");
    }

    private void addLap() {
        String lapTime = formatTime(elapsed());

        laps.add("Vòng " + laps.size() + ": " + lapTime);

        updateLaps();

        Log.i(TAG, "LAP: " + lapTime);
    }

    private String formatTime(long ms) {
        long phut = ms / 60000;
        long giay = (ms % 60000) / 1000;
        long phanMuoi = (ms % 1000) / 100;

        return String.format(
                Locale.getDefault(),
                "%02d:%02d.%d",
                phut,
                giay,
                phanMuoi
        );
    }

    private void startTicking() {
        handler.removeCallbacks(ticker);
        handler.post(ticker);
    }

    private void stopTicking() {
        handler.removeCallbacks(ticker);
    }

    private void updateTimeText() {
        tvTime.setText(formatTime(elapsed()));
    }

    private void updateLaps() {
        StringBuilder text = new StringBuilder();

        for (String lap : laps) {
            text.append(lap).append("\n");
        }

        tvLaps.setText(text.toString());
    }

    private void updateUi() {
        updateTimeText();

        btnStartPause.setText(
                running ? R.string.pause : R.string.start
        );

        tvStatus.setText(
                running
                        ? R.string.status_running
                        : R.string.status_paused
        );

        tvRecreate.setText(
                getString(
                        R.string.recreate_count,
                        recreateCount
                )
        );

        updateLaps();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");

        if (running) {
            startTicking();
        }

        updateUi();
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopTicking();

        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (stopWhenBackground && running) {
            pauseStopwatch();
            Log.d(TAG, "onStop: đã dừng do ra nền");
        }

        Log.d(TAG, "onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onDestroy() {
        stopTicking();

        Log.d(TAG, "onDestroy");

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_RUNNING, running);
        outState.putLong(KEY_ACCUMULATED, accumulated);
        outState.putLong(KEY_START, startTime);
        outState.putInt(KEY_RECREATE, recreateCount);

        outState.putStringArrayList(KEY_LAPS, laps);
        outState.putBoolean(
                KEY_STOP_BACKGROUND,
                stopWhenBackground
        );

        Log.d(
                TAG,
                "onSaveInstanceState – đã lưu "
                        + elapsed() + "ms vào Bundle"
        );
    }

    @Override
    protected void onRestoreInstanceState(
            Bundle savedInstanceState
    ) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(
                TAG,
                "onRestoreInstanceState"
        );
    }
}