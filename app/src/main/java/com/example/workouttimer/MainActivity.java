package com.example.workouttimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.icu.util.ULocale;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    long startTime = 0;
    long timeRunning = 0;

    TextView timerTextView;
    EditText workOutTypeEditText;
    LinearLayout controlsContainer;
    ImageButton startImageButton;
    ImageButton pauseImageButton;
    ImageButton stopImageButton;
    TextView lastWorkoutMessage;

    SharedPreferences sharedPreferences;

    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime + timeRunning;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int intMillis = (int)millis % 1000 / 10;
            timerTextView.setText(String.format(Locale.getDefault(),"%d:%02d:%02d", minutes, seconds, intMillis));
            timerHandler.postDelayed(this, 10);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        timerTextView = findViewById(R.id.timer);
        workOutTypeEditText = findViewById(R.id.workoutType);
        controlsContainer = findViewById(R.id.controlsContainer);
        startImageButton = findViewById(R.id.startButton);
        pauseImageButton = findViewById(R.id.pauseButton);
        stopImageButton = findViewById(R.id.stopButton);
        lastWorkoutMessage = findViewById(R.id.lastWorkoutMessage);
        sharedPreferences = getSharedPreferences("com.example.workouttimer", MODE_PRIVATE);
        if(savedInstanceState != null){
            startTime = savedInstanceState.getLong("START_TIME");
            timeRunning = savedInstanceState.getLong("TIME_RUNNING");

            //if startTime equals 0 we basically just want to take timeRunning into account. so this equation becomes: 0 - 0 + timeRunning;
            long millis = (startTime != 0 ? System.currentTimeMillis() : 0) - startTime + timeRunning;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int intMillis = (int)millis % 1000 / 10;
            timerTextView.setText(String.format(Locale.getDefault(),"%d:%02d:%02d", minutes, seconds, intMillis));
        }

        if(startTime != 0){
            startImageButton.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.success), android.graphics.PorterDuff.Mode.SRC_IN);
        }else if(timeRunning != 0){
            pauseImageButton.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.warning), android.graphics.PorterDuff.Mode.SRC_IN);
        }else{
            stopImageButton.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.danger), android.graphics.PorterDuff.Mode.SRC_IN);;
        }

        checkSharedPreferences();

    }
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putLong("START_TIME", startTime);
        savedInstanceState.putLong("TIME_RUNNING", timeRunning);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart(){
        super.onStart();
        if(startTime != 0){
            timerHandler.postDelayed(timerRunnable, 0);
        }

    }

    @Override
    protected void onStop(){
        timerHandler.removeCallbacks(timerRunnable);
        super.onStop();
    }

    @Override
    protected void onDestroy(){
        timerHandler.removeCallbacks(timerRunnable);
        super.onDestroy();
    }


    public void start(View view) {
        if(startTime == 0) {
            startTime = System.currentTimeMillis();
            timerHandler.postDelayed(timerRunnable, 0);
            for(int i = 0; i < controlsContainer.getChildCount(); ++i){
                ImageButton imgBtn = (ImageButton)controlsContainer.getChildAt(i);
                if(view.equals(imgBtn)) {
                    imgBtn.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.success), android.graphics.PorterDuff.Mode.SRC_IN);
                }else {
                    imgBtn.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }

        }
    }
    public void pause(View view) {
        if (startTime != 0) {
            timeRunning = System.currentTimeMillis() - startTime + timeRunning;
            startTime = 0;
            timerHandler.removeCallbacks(timerRunnable);
            for (int i = 0; i < controlsContainer.getChildCount(); ++i) {
                ImageButton imgBtn = (ImageButton) controlsContainer.getChildAt(i);
                if (view.equals(imgBtn)) {
                    imgBtn.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.warning), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    imgBtn.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
        }
    }
    public void stop(View view) {
        String workoutType = workOutTypeEditText.getText().toString();
        long timerInMillis = (startTime != 0 ? System.currentTimeMillis() : 0) - startTime + timeRunning;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("WORKOUT_TYPE", workoutType);
        editor.putLong("TIMER_MILLIS",timerInMillis);
        editor.apply();

        setLastWorkoutMessage(workoutType, timerInMillis);

        timeRunning = 0;
        startTime = 0;
        timerHandler.removeCallbacks(timerRunnable);
        timerTextView.setText(getString(R.string.default_timer_text));
        for(int i = 0; i < controlsContainer.getChildCount(); ++i){
            ImageButton imgBtn = (ImageButton)controlsContainer.getChildAt(i);
            if(view.equals(imgBtn)) {
                imgBtn.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.danger), android.graphics.PorterDuff.Mode.SRC_IN);
            }else {
                imgBtn.setColorFilter(ContextCompat.getColor(this.getApplicationContext(), R.color.black), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }


    }

    private void checkSharedPreferences(){
        String workoutType = sharedPreferences.getString("WORKOUT_TYPE", "");
        long timerInMillis = sharedPreferences.getLong("TIMER_MILLIS", 0);
        if(workoutType != "" && timerInMillis != 0) {
            setLastWorkoutMessage(workoutType, timerInMillis);
        }
    }

    private void setLastWorkoutMessage(String workoutType, long timerInMillis){
           int seconds = (int) (timerInMillis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            String timerFormatted = String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
            lastWorkoutMessage.setText(getString(R.string.last_workout_message, timerFormatted, workoutType));
    }

}
