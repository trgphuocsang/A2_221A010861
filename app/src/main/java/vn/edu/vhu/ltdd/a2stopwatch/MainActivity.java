package vn.edu.vhu.ltdd.a2stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvTime;
    private TextView tvStatus;
    private TextView tvRecreate;
    private Button btnStartPause;
    private Button btnReset;

    private Handler handler = new Handler();

    private long startTime = 0;
    private long elapsedTime = 0;

    private boolean isRunning = false;

    private int recreateCount = 0;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {

            if (isRunning) {

                elapsedTime =
                        SystemClock.elapsedRealtime() - startTime;

                updateTime();

                handler.postDelayed(this, 100);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tvTime = findViewById(R.id.tvTime);
        tvStatus = findViewById(R.id.tvStatus);
        tvRecreate = findViewById(R.id.tvRecreate);

        btnStartPause = findViewById(R.id.btnStartPause);
        btnReset = findViewById(R.id.btnReset);

        recreateCount++;

        if (savedInstanceState != null) {

            elapsedTime =
                    savedInstanceState.getLong(
                            "elapsedTime", 0
                    );

            isRunning =
                    savedInstanceState.getBoolean(
                            "isRunning", false
                    );
        }

        updateTime();
        updateStatus();
        updateRecreateCount();

        btnStartPause.setOnClickListener(v -> {

            if (isRunning) {
                pauseTimer();
            } else {
                startTimer();
            }

        });

        btnReset.setOnClickListener(v -> resetTimer());

        if (isRunning) {

            startTime =
                    SystemClock.elapsedRealtime()
                            - elapsedTime;

            handler.post(timerRunnable);
        }
    }

    private void startTimer() {

        isRunning = true;

        startTime =
                SystemClock.elapsedRealtime()
                        - elapsedTime;

        btnStartPause.setText(R.string.pause);

        tvStatus.setText(
                R.string.status_running
        );

        handler.post(timerRunnable);
    }

    private void pauseTimer() {

        elapsedTime =
                SystemClock.elapsedRealtime()
                        - startTime;

        isRunning = false;

        handler.removeCallbacks(timerRunnable);

        btnStartPause.setText(R.string.start);

        tvStatus.setText(
                R.string.status_paused
        );

        updateTime();
    }

    private void resetTimer() {

        isRunning = false;

        elapsedTime = 0;

        handler.removeCallbacks(timerRunnable);

        btnStartPause.setText(R.string.start);

        tvStatus.setText(
                R.string.status_paused
        );

        updateTime();
    }

    private void updateTime() {

        long totalTenths =
                elapsedTime / 100;

        long minutes =
                totalTenths / 600;

        long seconds =
                (totalTenths / 10) % 60;

        long tenths =
                totalTenths % 10;

        String time =
                String.format(
                        "%02d:%02d.%d",
                        minutes,
                        seconds,
                        tenths
                );

        tvTime.setText(time);
    }

    private void updateStatus() {

        if (isRunning) {

            tvStatus.setText(
                    R.string.status_running
            );

            btnStartPause.setText(
                    R.string.pause
            );

        } else {

            tvStatus.setText(
                    R.string.status_paused
            );

            btnStartPause.setText(
                    R.string.start
            );
        }
    }

    private void updateRecreateCount() {

        tvRecreate.setText(
                getString(
                        R.string.recreate_count,
                        recreateCount
                )
        );
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        if (isRunning) {

            elapsedTime =
                    SystemClock.elapsedRealtime()
                            - startTime;
        }

        outState.putLong(
                "elapsedTime",
                elapsedTime
        );

        outState.putBoolean(
                "isRunning",
                isRunning
        );

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {

        handler.removeCallbacks(timerRunnable);

        super.onDestroy();
    }
}