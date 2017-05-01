package jp.co.skyweaver.speechtotext;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.speech.SpeechRecognizer.CONFIDENCE_SCORES;
import static android.speech.SpeechRecognizer.ERROR_NO_MATCH;
import static android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT;
import static android.speech.SpeechRecognizer.RESULTS_RECOGNITION;

public class MainActivity extends AppCompatActivity implements OnRequestPermissionsResultCallback {

    private static final boolean DEBUG = false;
    private static final int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private Button mButton;
    private SpeechRecognizer mRecognizer;
    private RecognitionListener mListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle bundle) {

        }

        @Override
        public void onBeginningOfSpeech() {
            debugPrintLn("onBeginningOfSpeech");

        }

        @Override
        public void onRmsChanged(float v) {
//            debugPrintLn("onRmsChanged");

        }

        @Override
        public void onBufferReceived(byte[] bytes) {
            debugPrintLn("onBufferReceived");
        }

        @Override
        public void onEndOfSpeech() {
            debugPrintLn("onEndOfSpeech");
        }

        @Override
        public void onError(int errorCode) {
            debugPrintLn("onError:" + errorCode);

            switch (errorCode) {
                case ERROR_NO_MATCH: // go through.
                case ERROR_SPEECH_TIMEOUT:
                    restart();
            }
        }

        @Override
        public void onResults(Bundle bundle) {
            debugPrintLn("onResults:");
            ArrayList<String> texts = bundle.getStringArrayList(RESULTS_RECOGNITION);
            float[] confidents = bundle.getFloatArray(CONFIDENCE_SCORES);

            int maxConfidenceIndex = -1;
            float maxConfidence = 0;

            for (int i = 0; i < confidents.length; i++) {
                if (confidents[i] >= maxConfidence) {
                    maxConfidence = confidents[i];
                    maxConfidenceIndex = i;
                }
            }

            for (int i = 0; i < texts.size(); i++) {
                debugPrintLn("" + i + ":" + confidents[i] + ":" + texts.get(i));
            }

            debugPrintLn("" + maxConfidenceIndex + ":" + confidents[maxConfidenceIndex] + ":" + texts.get(maxConfidenceIndex));
            printLn(texts.get(maxConfidenceIndex));
            mHistoryText.append(texts.get(maxConfidenceIndex));
            restart();
        }

        @Override
        public void onPartialResults(Bundle bundle) {
            debugPrintLn("onPartialResults:");
            ArrayList<String> texts = bundle.getStringArrayList(RESULTS_RECOGNITION);

            if(TextUtils.isEmpty(texts.get(0)) ) {
                return;
            }

            for (int i = 0; i < texts.size(); i++) {
                debugPrintLn("" + i + ":" + texts.get(i));
            }
            printLn(texts.get(0));
        }

        @Override
        public void onEvent(int i, Bundle bundle) {
            debugPrintLn("onEvent");
        }
    };
    private TextView mHistoryText;


    private void restart() {
        debugPrintLn("restart");
        mRecognizer.stopListening();
        mRecognizer.startListening(mIntent);
    }

    private ScrollView mScrollView;

    private void printLn(String text) {
        if (!DEBUG) {
            mText.setText(text);
            scrollToBottom();
        } else {
            mText.append("\n" + text);
            scrollToBottom();
        }
    }

    private void debugPrintLn(String text) {
        if (!DEBUG) return;

        mText.append("\n" + text);
        scrollToBottom();
    }

    private Intent mIntent;
    private TextView mText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mText = (TextView) findViewById(R.id.text_output);
        mHistoryText = (TextView) findViewById(R.id.text_history);
        mScrollView = (ScrollView) findViewById(R.id.scroll_view);
        mScrollView.fullScroll(View.FOCUS_DOWN);

    }

    @Override
    protected void onResume() {
        super.onResume();

        requestPermissionsToUser();


        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(mListener);
        String langMode = "ja-JP";
        langMode = RecognizerIntent.LANGUAGE_MODEL_FREE_FORM;
        mIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, langMode);
        mIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getClass().getPackage().getName());
        mIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);


        mRecognizer.startListening(mIntent);




    }

    private void requestPermissionsToUser() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_RECORD_AUDIO);

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRecognizer.stopListening();

        mRecognizer.destroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length != 1 ||
                    grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                debugPrintLn("permission denied.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void scrollToBottom() {
        mScrollView.post(new Runnable() {
            public void run() {
                mScrollView.smoothScrollTo(0, mText.getBottom());
            }
        });
    }
}
