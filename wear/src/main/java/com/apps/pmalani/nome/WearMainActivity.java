package com.apps.pmalani.nome;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;
import android.widget.Button;


public class WearMainActivity extends Activity {

    private static final String TAG = "NomeWearMainActivity";
    private TextView mTextView;
    private RelativeLayout mRelLayout;
    private Button mStartButton;
    private TextView mTimeSigNumView;
    private TextView mTimeSigDenView;

    private static int MILLISECONDS_PER_MIN = 60 * 1000;

    private static int VIBRATE_LENGTH_NORMAL = 50;
    private static int VIBRATE_LENGTH_COMPLETE = 200;

    // Length of the flash, in milliseconds
    private static int FLASH_LENGTH = 50;

    //Max possible notes per measure
    private static int MAX_NOTES = 16;

    // Current beats per minute value
    private int mCurrentTempo;

    // Time interval in milliseconds
    private int mTimeInterval;

    // NOTE: Currently we only support quarter notes per measure
    private int mNotesPerMeasure = 4;
    private int mCurrentNote = 1;

    private Vibrator mVibrator;

    private boolean mCurrentlyRunning = false;

    private Handler mPeriodicHandler;

    private Object mParamLock = new Object();

    private Animation mFlashAnimation;

    private boolean mIsSquare;

    private void setIsSquare(boolean isSquare) {
        mIsSquare = isSquare;
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

        stub.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override
            public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                Log.e(TAG, "HEY IM GETTING CALLED HERE!");
                if (insets.isRound()) {
                    setIsSquare(false);
                    setContentView(R.layout.round_activity_wear_main);
                } else {
                    setIsSquare(true);
                    setContentView(R.layout.rect_activity_wear_main);
                }

                // Here is a more appropriate place to get pointers to the buttons (maybe)
                mRelLayout = (RelativeLayout)findViewById(R.id.relLayout);
                if (mRelLayout == null) {
                    Log.e(TAG, "mRelLayout returned NULL!");
                }
                mStartButton = (Button)findViewById(R.id.startButton);
                if (mStartButton == null) {
                    Log.e(TAG, "mStartButton returned NULL");
                }
                mTimeSigNumView = (TextView)findViewById(R.id.timeSigNum);
                if (mTimeSigNumView == null) {
                    Log.e(TAG, "mTimeSigNumView returned NULL");
                }
                mTimeSigDenView = (TextView)findViewById(R.id.timeSigDen);
                if (mTimeSigDenView == null) {
                    Log.e(TAG, "mTimeSigDenView returned NULL");
                }
                return insets;
            }
        });

        // Perform Animation setup
        mFlashAnimation = new AlphaAnimation(0, 1);
        mFlashAnimation.setDuration(FLASH_LENGTH * 5);
        mFlashAnimation.setInterpolator(new LinearInterpolator());
        mFlashAnimation.setRepeatMode(Animation.REVERSE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "onPause()");
        synchronized (mParamLock) {
            // Stop any pending timers
            if (mCurrentlyRunning == true) {
                mStartButton.setText("Stop");
                mPeriodicHandler.removeCallbacks(mBeatsTask);
                mCurrentlyRunning = false;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    public void handleTempoClick(View v) {
        synchronized (mParamLock) {
            switch (v.getId()) {
                case R.id.temp_decrease_button:
                    if (mCurrentTempo > 20)
                        mCurrentTempo--;
                    break;
                case R.id.tempo_increase_button:
                    if (mCurrentTempo < 190)
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
            synchronized (mParamLock) {
                mCurrentNote = (mCurrentNote + 1) % mNotesPerMeasure;
                if (mCurrentNote == 0) {
                    //End of Bar, so vibrate longer
                    mVibrator.vibrate(VIBRATE_LENGTH_COMPLETE);
                    mRelLayout.startAnimation(mFlashAnimation);
                } else {
                    mVibrator.vibrate(VIBRATE_LENGTH_NORMAL);
                }
                mPeriodicHandler.postDelayed(this, mTimeInterval);
            }
        }
    };

    public void startStopClick(View v) {
        Button startButton = (Button)findViewById(R.id.startButton);
        synchronized (mParamLock) {
            if (mCurrentlyRunning == false) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                mCurrentlyRunning = true;
                mBeatsTask.run();
                startButton.setText("Stop");
                // Reset to one
                mCurrentNote = 1;
            } else {
                startButton.setText("Start");
                mPeriodicHandler.removeCallbacks(mBeatsTask);
                mCurrentlyRunning = false;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    public void timeSigNumClick(View v) {
        synchronized (mParamLock) {
            mNotesPerMeasure = mNotesPerMeasure % 16;
            mNotesPerMeasure++;
        }
        mTimeSigNumView.setText("" + mNotesPerMeasure);
    }
}
