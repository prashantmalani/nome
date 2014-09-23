package com.apps.pmalani.nome;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;
import android.view.View;
import android.widget.EditText;


public class WearMainActivity extends Activity {

    private TextView mTextView;
    private int mCurrentTempo;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize tempo to 60 bpm
        mCurrentTempo = 60;
        mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        setContentView(R.layout.activity_wear_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
            }
        });
    }

    public void handleClick(View v) {
        switch(v.getId()) {
            case R.id.temp_decrease_button:
                mCurrentTempo--;
                break;
            case R.id.tempo_increase_button:
                mCurrentTempo++;
            default:
                break;
        }
        EditText tempo_text = (EditText)findViewById(R.id.cur_tempo_text);
        tempo_text.setText("" + mCurrentTempo);

        // Sample to show how the vibration works
        mVibrator.vibrate(20);

        // TODO : Recalculate time between beats here!
    }
}
