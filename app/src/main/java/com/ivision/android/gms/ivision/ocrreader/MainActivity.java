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
import android.content.SharedPreferences;
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
import java.util.HashMap;
import java.util.Locale;

import static android.R.attr.data;

@TargetApi(14)
public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, AdapterView.OnItemSelectedListener {
    private static final String API_KEY = "";

    // Use a compound button so either checkbox or switch widgets work.
    private CompoundButton autoFocus;
    private CompoundButton useFlash;
    private TextView statusMessage;
    private TextView textValue;

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
    private HashMap<Integer, String> languages;
    Spinner spLanguage;

    public static final int RC_TTSENGINES = 1003;
    private String ttsEngineName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView)findViewById(R.id.status_message);
        textValue = (TextView)findViewById(R.id.text_value);

        autoFocus = (CompoundButton) findViewById(R.id.auto_focus);
        useFlash = (CompoundButton) findViewById(R.id.use_flash);

        spLanguage = (Spinner) findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_arrays, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLanguage.setAdapter(adapter);
        spLanguage.setOnItemSelectedListener(this);

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


        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                            tts.speak("Swipe right to detect text, double tap to stop speech, tap to restart speech. Swipe left to change Text to Speech Engine.", TextToSpeech.QUEUE_ADD, null);
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
//        tts = new TextToSpeech(this.getApplicationContext(), listener, ttsEngineName);
        tts = new TextToSpeech(this.getApplicationContext(), listener);

        //findViewById(R.id.read_text).setOnClickListener(this);

        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);



        languages = new HashMap<Integer, String>();
        languages = createHash(languages);


    }

    public HashMap<Integer, String> createHash(HashMap<Integer,String> languages) {
        languages.put(0, "af");
        languages.put(1, "sq");
        languages.put(2, "am");
        languages.put(3, "ar");
        languages.put(4, "hy");
        languages.put(5, "az");
        languages.put(6, "eu");
        languages.put(7, "bn");
        languages.put(8, "bs");
        languages.put(9, "bg");
        languages.put(10, "ca");
        languages.put(11, "ceb");
        languages.put(12, "zh-CN");
        languages.put(13, "zh-TW");
        languages.put(14, "co");
        languages.put(15, "hr");
        languages.put(16, "cs");
        languages.put(17, "da");
        languages.put(18, "nl");
        languages.put(19, "et");
        languages.put(20, "fi");
        languages.put(21, "fr");
        languages.put(22, "gl");
        languages.put(23, "ka");
        languages.put(24, "de");
        languages.put(25, "el");
        languages.put(26, "gu");
        languages.put(27, "iw");
        languages.put(28, "hi");
        languages.put(29, "hu");
        languages.put(30, "is");
        languages.put(31, "id");
        languages.put(32, "it");
        languages.put(33, "ja");
        languages.put(34, "jw");
        languages.put(35, "kn");
        languages.put(36, "kk");
        languages.put(37, "km");
        languages.put(38, "ko");
        languages.put(39, "ku");
        languages.put(40, "lv");
        languages.put(41, "lt");
        languages.put(42, "mk");
        languages.put(43, "ms");
        languages.put(44, "ml");
        languages.put(45, "mr");
        languages.put(46, "mn");
        languages.put(47, "no");
        languages.put(48, "ny");
        languages.put(49, "ps");
        languages.put(50, "fa");
        languages.put(51, "pl");
        languages.put(52, "pt");
        languages.put(53, "ma");
        languages.put(54, "ro");
        languages.put(55, "ru");
        languages.put(56, "sr");
        languages.put(57, "st");
        languages.put(58, "sn");
        languages.put(59, "sd");
        languages.put(60, "si");
        languages.put(61, "sk");
        languages.put(62, "sl");
        languages.put(63, "es");
        languages.put(64, "sw");
        languages.put(65, "sv");
        languages.put(66, "tl");
        languages.put(67, "ta");
        languages.put(68, "te");
        languages.put(69, "th");
        languages.put(70, "tr");
        languages.put(71, "uk");
        languages.put(72, "ur");
        languages.put(73, "uz");
        languages.put(74, "vi");
        languages.put(75, "xh");
        languages.put(76, "zu");

        return languages;
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
                    tts.speak("Click drop-down menu to translate text into other languages.", TextToSpeech.QUEUE_ADD, null);
                    tts.speak("Swipe right to capture new text.", TextToSpeech.QUEUE_ADD, null);
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

        if (requestCode == RC_TTSENGINES) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    ttsEngineName = data.getStringExtra(TTSEnginesActivity.TTSSelectedString);
                    Log.d("TTSListener", "EngineName: " + ttsEngineName.toString());

                    tts = new TextToSpeech(this.getApplicationContext(), new TextToSpeech.OnInitListener() {
                        public void onInit(int status) {
                            tts.setLanguage(Locale.US);
                            tts.speak("Swipe right to detect text, double tap to stop speech, tap to restart speech. Swipe left to change Text to Speech Engine.", TextToSpeech.QUEUE_ADD, null);
                        }
                    }, ttsEngineName);

                    Log.d("TTSListener", "tts: " + tts);
                }
            }
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        languageSelected = languages.get(position);

        tts.setLanguage(Locale.US);
        tts.speak("Currently " + spLanguage.getItemAtPosition(position) + " selected.", TextToSpeech.QUEUE_ADD, null);
        tts.speak("Click drop-down menu to change language.", TextToSpeech.QUEUE_ADD, null);
        tts.speak("Press TRANSLATE to confirm your language selection.", TextToSpeech.QUEUE_ADD, null);
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
        tts.setLanguage(Locale.US);
        tts.speak("Swipe right to capture new text.", TextToSpeech.QUEUE_ADD, null);
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
            intent.putExtra(ttsEngineName, ttsEngineName);

            text = "";
            outputText.clearComposingText();
            tts.setLanguage(Locale.US);

            tts.stop();

            startActivityForResult(intent, RC_OCR_CAPTURE);
        } else if(event1.getX() > event2.getX()) {
            Log.d(DEBUG_TAG, "swipe left");

            Intent intent = new Intent(this, TTSEnginesActivity.class);

            ttsEngineName = "";

            tts.stop();

            startActivityForResult(intent, RC_TTSENGINES);
//            startActivity(intent);
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
