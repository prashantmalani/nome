package com.apps.pmalani.nome;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;


public class WearMainActivity extends Activity {

    private TextView mTextView;

    private static int MILLISECONDS_PER_MIN = 60 * 1000;

    private static int VIBRATE_LENGTH_NORMAL = 5;
    private static int VIBRATE_LENGTH_COMPLETE = 20;

    // Current beats per minute value
    private int mCurrentTempo;

    // Time interval in milliseconds
    private int mTimeInterval;

    // NOTE: Currently we only support quarter notes per measure
    private int mNotesPerMeasure = 4;

    private int mCurrentNote = 0;

    private Vibrator mVibrator;

    private boolean mCurrentlyRunning = false;

    private Handler mPeriodicHandler;

    private void updateParams() {
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize tempo to 60 bpm
        mCurrentTempo = 60;
        mTimeInterval = MILLISECONDS_PER_MIN / mCurrentTempo;
        mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        mPeriodicHandler = new Handler();

        setContentView(R.layout.activity_wear_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }

    public void handleTempoClick(View v) {
        synchronized (this) {
            switch (v.getId()) {
                case R.id.temp_decrease_button:
                    mCurrentTempo--;
                    break;
                case R.id.tempo_increase_button:
                    mCurrentTempo++;
                default:
                    break;
            }
            TextView tempo_text = (TextView) findViewById(R.id.cur_tempo_text);
            tempo_text.setText("" + mCurrentTempo);

            mTimeInterval = MILLISECONDS_PER_MIN / mCurrentTempo;

        }
    }

    Runnable mBeatsTask = new Runnable() {
        @Override
        public void run() {
            // TODO: Need to worry about synchronization of the member variables here
            mCurrentNote = (mCurrentNote + 1) % mNotesPerMeasure;
            if (mCurrentNote == 0) {
                //End of Bar, so vibrate longer
                mVibrator.vibrate(VIBRATE_LENGTH_COMPLETE);
            } else {
                mVibrator.vibrate(VIBRATE_LENGTH_NORMAL);
            }
            mVibrator.vibrate(VIBRATE_LENGTH_NORMAL);
            mPeriodicHandler.postDelayed(this, mTimeInterval);
        }
    };

    public void startStopClick(View v) {
        Button startButton = (Button)findViewById(R.id.startButton);

        if (mCurrentlyRunning == false) {
            mCurrentlyRunning = true;
            mBeatsTask.run();
            startButton.setText("Stop");
        } else {
            startButton.setText("Start");
            mPeriodicHandler.removeCallbacks(mBeatsTask);
            mCurrentlyRunning = false;
        }
    }
}
