package tw.com.cht.iot.voicecommadtest;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    static final String LISTEN = "listen";
    static final String DONE = "done";
    static final int RQS_VOICE_RECOGNITION = 0;

    TextToSpeech tts;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    if (tts.isLanguageAvailable(Locale.TAIWAN) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                        tts.setLanguage(Locale.TAIWAN);

                        doIntroduce();
                    } // otherwise, Locale is not yet supported
                }
            }
        });
    }

    protected void onDestroy() {
        tts.shutdown();

        super.onDestroy();
    }

    // ======

    void speak(String message, String utteranceId) {
        HashMap<String, String> params = new HashMap<>();
        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId);
        tts.speak(message, TextToSpeech.QUEUE_FLUSH, params);
    }

    void doIntroduce() {
        tts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
            public void onUtteranceCompleted(String s) {
                if (LISTEN.equals(s)) {
                    doListen();
                } else if (DONE.equals(s)) {
                    finish();
                }
            }
        });

        String[] hellos = new String[] { "你好 有什麼需要我幫忙的地方", "同志你好 有何貴幹", "我是神燈 你現在想許什麼願望", "有什麼事情嗎", "現在是怎樣呢", "瞎咪歹集", "衝瞎毀喇" };
        int i = (int)(Math.random() * hellos.length);
        speak(hellos[i], "listen"); // just for fun
    }

    void doListen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說出指令"); // TODO - NG
        startActivityForResult(intent, RQS_VOICE_RECOGNITION);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQS_VOICE_RECOGNITION) {
            if (resultCode == RESULT_OK) {
                String first = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0); // TODO - NG
                Toast.makeText(this, first, Toast.LENGTH_LONG).show();

                if (first.contains("開")) { // TODO - NG
                    speak("燈打開了 很不賴吧", DONE);
                } else if (first.contains("關")) { // TODO - NG
                    speak("燈關掉了 應該吧", DONE);
                } else if (first.contains("笑")) { // TODO - NG
                    speak("跟你講一個笑話 從前有三隻毛毛蟲排成一列 然後就麻煩你自己上網找這則笑話 報告完畢 謝謝各位", DONE);
                } else if (first.contains("天")) { // TODO - NG
                    speak("今天天氣應該不錯 太陽從東邊升起 西邊落下", DONE);
                } else if (first.contains("排")) {   // TODO - NG
                    speak("已經排好了 時候到了會通知您 會安排在你老婆不在家的時候", DONE);
                } else if (first.contains("喜歡")) {   // TODO - NG
                    speak("別這樣 我會害臊的 糾咪", DONE);
                } else {
                    speak("不好意思 沒聽懂 再講一次吧", LISTEN);
                }
            }
        }
    }
}
