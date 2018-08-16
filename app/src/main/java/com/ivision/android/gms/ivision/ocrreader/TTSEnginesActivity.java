package com.ivision.android.gms.ivision.ocrreader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.app.Activity;

import com.google.android.gms.common.api.CommonStatusCodes;

@TargetApi(14)
public class TTSEnginesActivity extends AppCompatActivity implements OnInitListener, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "TTSEngines";

    private TextView tvDefaultTTS;

    private Spinner spInstalledEngines;
    private TextToSpeech tts;
    private List<TextToSpeech.EngineInfo> listInstalledEngines;
    private List<String> listInstalledEnginesName;
    private String defaultTTS;
    private int ttsSelected;
    private String selectedTTS;
    private String selectedTTSLabel;
    private String ttsEngineLabel;

    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;

    public static final String TTSSelectedEngineName = "String";
    public static final String TTSSelectedEngineLabel = "Strings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tts_engines);

        ttsEngineLabel = getIntent().getStringExtra(MainActivity.TTSEngineLabel);

        tts = new TextToSpeech(this, this);
        listInstalledEngines = tts.getEngines();
        listInstalledEnginesName = new ArrayList<String>();

        for(int i = 0; i < listInstalledEngines.size(); i++){
            listInstalledEnginesName.add(listInstalledEngines.get(i).label);
        }

        spInstalledEngines = (Spinner)findViewById(R.id.installedengines);
        spInstalledEngines.setOnItemSelectedListener(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, listInstalledEnginesName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spInstalledEngines.setAdapter(adapter);

        if(ttsEngineLabel != null) {
            spInstalledEngines.setSelection(adapter.getPosition(ttsEngineLabel));
            Log.d(TAG, "here " + ttsEngineLabel);
        }

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        Button selectTTSButton = (Button) findViewById(R.id.selectTTSButton);

        selectTTSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicking: " + selectedTTS);

                Intent data = new Intent();
                data.putExtra(TTSSelectedEngineName, selectedTTS);
                data.putExtra(TTSSelectedEngineLabel, selectedTTSLabel);
                setResult(CommonStatusCodes.SUCCESS, data);
                finish();
            }
        });
    }

    @Override
    public void onInit(int status) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tts.shutdown();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        tts.stop();

        Log.d(TAG, "tts selected: " + listInstalledEngines.get(position).name);
        selectedTTS = listInstalledEngines.get(position).name;
        selectedTTSLabel = listInstalledEngines.get(position).label;

        tts = new TextToSpeech(this.getApplicationContext(), new TextToSpeech.OnInitListener() {
            public void onInit(int status) {
                tts.setLanguage(Locale.US);
                tts.speak("Currently " + selectedTTSLabel + " selected.", TextToSpeech.QUEUE_ADD, null);
                tts.speak("Click drop-down menu to change Text to Speech Engine.", TextToSpeech.QUEUE_ADD, null);
                tts.speak("Press OK to confirm your selection or go back to main page.", TextToSpeech.QUEUE_ADD, null);
            }
        }, selectedTTS);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    public boolean onFling(MotionEvent event1, MotionEvent event2, float v, float v1) {
//        if(event1.getX() < event2.getX()) {
//            Log.d(DEBUG_TAG, "swipe right");
//            Intent intent = new Intent(this, MainActivity.class);
//            startActivity(intent);
//        }
        return true;
    }


    public boolean onSingleTapConfirmed(MotionEvent event) {
        return true;
    }

    public boolean onDoubleTap(MotionEvent event) {
        return true;
    }

    public boolean onDoubleTapEvent(MotionEvent event) {
        return true;
    }

    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    public void onShowPress(MotionEvent motionEvent) { return; }

    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) { return false; }

    public void onLongPress(MotionEvent motionEvent) { return; }
}
