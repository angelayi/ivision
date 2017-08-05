/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.samples.vision.ocrreader;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.text.TextBlock;

//import com.google.api.translate.Language;
//import com.google.api.translate.Translate;

import java.io.PrintStream;
import java.util.Locale;

import static android.R.attr.data;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, AdapterView.OnItemSelectedListener {
    private static final String API_KEY = "";

    // Use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView textValue;

    private Spinner spinner;

    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";
    private static final int SETTING_REQUEST = 1;

    private static final String DEBUG_TAG = "Gestures";
    private GestureDetectorCompat mDetector;

    private String text;
    private TextToSpeech tts;

    private GoogleTranslate translator;
    //private EditText inputText;
    private TextView outputText;
    private String languageSelected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        textValue = (TextView)findViewById(R.id.text_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_arrays, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //inputText = (EditText) findViewById(R.id.inputText);
        outputText = (TextView) findViewById(R.id.outputText);
        Button translatebutton = (Button) findViewById(R.id.translatebutton);
        
        translatebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EnglishToLanguage().execute();
            }
        });


        //findViewById(R.id.read_text).setOnClickListener(this);

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);

    }

    /*
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.read_text) {
            // launch Ocr capture activity.
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            intent.putExtra(OcrCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_OCR_CAPTURE);
        }
    }
    */

    /*
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     * @see #startActivityForResult
     * @see #createPendingResult
     * @see #setResult(int)
     */

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    statusMessage.setText(R.string.ocr_success);

                    Log.d(TAG, "Text read: " + text);

                    textValue.setText(text);
                    tts.speak(text, TextToSpeech.QUEUE_ADD, null);
                } else {
                    statusMessage.setText(R.string.ocr_failure);
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                statusMessage.setText(String.format(getString(R.string.ocr_error), CommonStatusCodes.getStatusCodeString(resultCode)));
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        languageSelected = "";
        switch(position) {
            case 1: languageSelected = "af";
            case 2: languageSelected = "sq";
            case 3: languageSelected = "am";
            case 4: languageSelected = "ar";
            case 5: languageSelected = "hy";
            case 6: languageSelected = "az";
            case 7: languageSelected = "eu";
            case 8: languageSelected = "bn";
            case 9: languageSelected = "bs";
            case 10: languageSelected = "bg";
            case 11: languageSelected = "ca";
            case 12: languageSelected = "ceb";
            case 13: languageSelected = "zh-CN";
            case 14: languageSelected = "zh-TW";
            case 15: languageSelected = "co";
            case 16: languageSelected = "hr";
            case 17: languageSelected = "cs";
            case 18: languageSelected = "da";
            case 19: languageSelected = "nl";
            case 20: languageSelected = "et";
            case 21: languageSelected = "fi";
            case 22: languageSelected = "fr";
            case 23: languageSelected = "gl";
            case 24: languageSelected = "ka";
            case 25: languageSelected = "de";
            case 26: languageSelected = "el";
            case 27: languageSelected = "gu";
            case 28: languageSelected = "iw";
            case 29: languageSelected = "hi";
            case 30: languageSelected = "hu";
            case 31: languageSelected = "is";
            case 32: languageSelected = "id";
            case 33: languageSelected = "it";
            case 34: languageSelected = "ja";
            case 35: languageSelected = "jw";
            case 36: languageSelected = "kn";
            case 37: languageSelected = "kk";
            case 38: languageSelected = "km";
            case 39: languageSelected = "ko";
            case 40: languageSelected = "ku";
            case 41: languageSelected = "lv";
            case 42: languageSelected = "lt";
            case 43: languageSelected = "mk";
            case 44: languageSelected = "ms";
            case 45: languageSelected = "ml";
            case 46: languageSelected = "mr";
            case 47: languageSelected = "mn";
            case 48: languageSelected = "no";
            case 49: languageSelected = "ny";
            case 50: languageSelected = "ps";
            case 51: languageSelected = "fa";
            case 52: languageSelected = "pl";
            case 53: languageSelected = "pt";
            case 54: languageSelected = "ma";
            case 55: languageSelected = "ro";
            case 56: languageSelected = "ru";
            case 57: languageSelected = "sr";
            case 58: languageSelected = "st";
            case 59: languageSelected = "sn";
            case 60: languageSelected = "sd";
            case 61: languageSelected = "si";
            case 62: languageSelected = "sk";
            case 63: languageSelected = "sl";
            case 64: languageSelected = "es";
            case 65: languageSelected = "sw";
            case 66: languageSelected = "sv";
            case 67: languageSelected = "tl";
            case 68: languageSelected = "ta";
            case 69: languageSelected = "te";
            case 70: languageSelected = "th";
            case 71: languageSelected = "tr";
            case 72: languageSelected = "uk";
            case 73: languageSelected = "ur";
            case 74: languageSelected = "uz";
            case 75: languageSelected = "vi";
            case 76: languageSelected = "xh";
            case 77: languageSelected = "zu";
                break;
            default: break;
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {}

    private class EnglishToLanguage extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        protected void onError(Exception ex) {}

        @Override
        protected Void doInBackground(Void... params) {
            try {
                translator = new GoogleTranslate(API_KEY);

                Thread.sleep(1000);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(MainActivity.this, null, "Translating...");
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setIndeterminate(true);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
            super.onPostExecute(result);
            translated();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    public void translated(){
        String output = translator.translte(text, "en", languageSelected);
        outputText.setText(output);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        return true;
    }

    public boolean onDoubleTap(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        tts.stop();
        return true;
    }

    public boolean onDoubleTapEvent(MotionEvent event) {
        Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    public boolean onFling(MotionEvent event1, MotionEvent event2, float v, float v1) {
        if(event1.getX() < event2.getX()) {
            Log.d(DEBUG_TAG, "swipe right");
            Intent intent = new Intent(this, OcrCaptureActivity.class);
            intent.putExtra(OcrCaptureActivity.AutoFocus, autoFocus.isChecked());
            intent.putExtra(OcrCaptureActivity.UseFlash, useFlash.isChecked());

            startActivityForResult(intent, RC_OCR_CAPTURE);
        } else if(event1.getX() > event2.getX()) {
            Log.d(DEBUG_TAG, "swipe left");
        }
        return true;
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) { return; }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) { return false; }

    @Override
    public void onLongPress(MotionEvent motionEvent) { return; }
}
