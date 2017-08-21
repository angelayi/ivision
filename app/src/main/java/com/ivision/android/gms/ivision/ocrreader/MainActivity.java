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

package com.ivision.android.gms.ivision.ocrreader;

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
                            tts.speak("Swipe right to detect text, double tap to stop speech, tap to restart speech", TextToSpeech.QUEUE_ADD, null);
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
        if (requestCode == RC_OCR_CAPTURE) {
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
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        languageSelected = "";
        switch(position) {
            case 0: languageSelected = "af";
                break;
            case 1: languageSelected = "sq";
                break;
            case 2: languageSelected = "am";
                break;
            case 3: languageSelected = "ar";
                break;
            case 4: languageSelected = "hy";
                break;
            case 5: languageSelected = "az";
                break;
            case 6: languageSelected = "eu";
                break;
            case 7: languageSelected = "bn";
                break;
            case 8: languageSelected = "bs";
                break;
            case 9: languageSelected = "bg";
                break;
            case 10: languageSelected = "ca";
                break;
            case 11: languageSelected = "ceb";
                break;
            case 12: languageSelected = "zh-CN";
                break;
            case 13: languageSelected = "zh-TW";
                break;
            case 14: languageSelected = "co";
                break;
            case 15: languageSelected = "hr";
                break;
            case 16: languageSelected = "cs";
                break;
            case 17: languageSelected = "da";
                break;
            case 18: languageSelected = "nl";
                break;
            case 19: languageSelected = "et";
                break;
            case 20: languageSelected = "fi";
                break;
            case 21: languageSelected = "fr";
                break;
            case 22: languageSelected = "gl";
                break;
            case 23: languageSelected = "ka";
                break;
            case 24: languageSelected = "de";
                break;
            case 25: languageSelected = "el";
                break;
            case 26: languageSelected = "gu";
                break;
            case 27: languageSelected = "iw";
                break;
            case 28: languageSelected = "hi";
                break;
            case 29: languageSelected = "hu";
                break;
            case 30: languageSelected = "is";
                break;
            case 31: languageSelected = "id";
                break;
            case 32: languageSelected = "it";
                break;
            case 33: languageSelected = "ja";
                break;
            case 34: languageSelected = "jw";
                break;
            case 35: languageSelected = "kn";
                break;
            case 36: languageSelected = "kk";
                break;
            case 37: languageSelected = "km";
                break;
            case 38: languageSelected = "ko";
                break;
            case 39: languageSelected = "ku";
                break;
            case 40: languageSelected = "lv";
                break;
            case 41: languageSelected = "lt";
                break;
            case 42: languageSelected = "mk";
                break;
            case 43: languageSelected = "ms";
                break;
            case 44: languageSelected = "ml";
                break;
            case 45: languageSelected = "mr";
                break;
            case 46: languageSelected = "mn";
                break;
            case 47: languageSelected = "no";
                break;
            case 48: languageSelected = "ny";
                break;
            case 49: languageSelected = "ps";
                break;
            case 50: languageSelected = "fa";
                break;
            case 51: languageSelected = "pl";
                break;
            case 52: languageSelected = "pt";
                break;
            case 53: languageSelected = "ma";
                break;
            case 54: languageSelected = "ro";
                break;
            case 55: languageSelected = "ru";
                break;
            case 56: languageSelected = "sr";
                break;
            case 57: languageSelected = "st";
                break;
            case 58: languageSelected = "sn";
                break;
            case 59: languageSelected = "sd";
                break;
            case 60: languageSelected = "si";
                break;
            case 61: languageSelected = "sk";
                break;
            case 62: languageSelected = "sl";
                break;
            case 63: languageSelected = "es";
                break;
            case 64: languageSelected = "sw";
                break;
            case 65: languageSelected = "sv";
                break;
            case 66: languageSelected = "tl";
                break;
            case 67: languageSelected = "ta";
                break;
            case 68: languageSelected = "te";
                break;
            case 69: languageSelected = "th";
                break;
            case 70: languageSelected = "tr";
                break;
            case 71: languageSelected = "uk";
                break;
            case 72: languageSelected = "ur";
                break;
            case 73: languageSelected = "uz";
                break;
            case 74: languageSelected = "vi";
                break;
            case 75: languageSelected = "xh";
                break;
            case 76: languageSelected = "zu";
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
        Log.d(TAG, "Translated Text: " + output);
        outputText.setText(output);

        tts.setLanguage(new Locale(languageSelected));
        tts.speak(output, TextToSpeech.QUEUE_ADD, null);
        outputText.clearComposingText();
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

            text = "";
            outputText.clearComposingText();
            tts.setLanguage(Locale.US);

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
