package tw.com.cht.iot.voicecommadtest;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    static final Logger LOG = LoggerFactory.getLogger(MainActivity.class);

    static final String LISTEN = "listen";
    static final String DONE = "done";

    TextToSpeech tts;

    SpeechRecognizer recognizer;

    MyRichClient client;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            public void onInit(int i) {
                if (i == TextToSpeech.SUCCESS) {
                    if (tts.isLanguageAvailable(Locale.TAIWAN) == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                        tts.setLanguage(Locale.TAIWAN);

                        doIntroduce();

                    } else { // otherwise, Locale is not yet supported
                        Toast.makeText(MainActivity.this, "Taiwan language is not supported.", Toast.LENGTH_LONG).show();

                    }
                }
            }
        });

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {
            }

            @Override
            public void onBufferReceived(byte[] bytes) {
            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {
                onVoiceResults("......");

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (!data.isEmpty()) {
                    String first = data.get(0);

                    onVoiceResults(first);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {
            }

            @Override
            public void onEvent(int i, Bundle bundle) {
            }
        });

        client = new MyRichClient();
    }

    protected void onDestroy() {
        recognizer.destroy();

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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            doListen();
                        }
                    });

                } else if (DONE.equals(s)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    });
                }
            }
        });

        String[] hellos = new String[] { "你好 有什麼需要我幫忙的地方", "同志你好 有何貴幹", "我是阿拉丁神燈 你現在想許什麼願望", "現在是有什麼事情嗎", "老大 現在是怎樣呢", "阿西屋瞎咪歹集", "今罵西衝瞎毀" };
        int i = (int)(Math.random() * hellos.length);
        speak(hellos[i], LISTEN); // just for fun
    }

    void doListen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "請說出指令"); // TODO - NG
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000000);
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000000);

        recognizer.startListening(intent);
    }

    void onVoiceResults(String first) {
        Toast.makeText(this, first, Toast.LENGTH_LONG).show();

        if (first.contains("開")) { // TODO - NG
            speak("燈打開了 很不賴吧", DONE);
            client.controlLamp(true);

        } else if (first.contains("關")) { // TODO - NG
            speak("燈關掉了 有意思喔", DONE);
            client.controlLamp(false);

        } else if (first.contains("訊息")) { // TODO - NG
            String message = first.substring(first.indexOf("訊息") + 2);
            speak("訊息送出去了 用 Line 收收看喔", DONE);
            client.sendMessage(message);

        } else if (first.contains("空氣")) { // TODO - NG
            try {
                speak(client.pir(), DONE);

            } catch (Exception e) {
                LOG.error("error", e);
            }

        } else if (first.contains("歌")) {
            speak("喔唉呀呀呀喔喔喔 愛的抱抱 然後你 K K 罷克斯 要記得繳錢才能聽啊", DONE);

        } else if (first.contains("新聞")) {
            speak("懶得查 就一堆三寶行車紀錄器 很無聊 沒什麼營養 多讀點書吧", DONE);

        } else if (first.contains("笑")) { // TODO - NG
            speak("跟你講一個笑話 從前有三隻毛毛蟲排成一列 然後就麻煩請您自己上網找這則笑話 謝謝你的惠顧", DONE);

        } else if (first.contains("氣")) { // TODO - NG
            try {
                List<String> ws = client.weathers();
                for (String w : ws) {
                    String h = w.substring(0, 2);

                    if (first.contains(h)) {
                        speak(w, DONE);

                        return;
                    }
                }

            } catch (Exception e) {
            }

            speak("那裡天氣應該不錯 太陽從東邊升起 西邊落下 如果下雨 請記得帶傘", DONE);

        } else if (first.contains("安排")) {   // TODO - NG
            speak("已經排好了 時候到了會通知您 會安排在你老婆不在家的時候 天知地知你知我知 你老婆絕對不知 9 4 8 7 9 4 狂", DONE);

        } else if (first.contains("喜歡")) {   // TODO - NG
            speak("別這樣 我會害臊的 糾咪", DONE);

        } else if (first.contains("電話")) {
            speak("你電話費還沒有繳 打不出去啊", DONE);

        } else if (first.contains("笨")) {
            speak("我不笨 是你自己沒有慧根 好嗎", DONE);

        } else if (first.contains("煮")) {
            speak("你老婆沒有教我 你岳母也沒有教你老婆 乾脆我幫你訂餐廳外食好了", DONE);

        } else if (first.contains("美女")) {
            speak("謝謝你的讚美 我是永遠的二十歲啊", DONE);

        } else {
            String[] errors = new String[] { "不好意思 沒聽懂 再講一次好嗎", "你口音怪怪的", "這個問題很難回答", "糟糕我幫不了你", "啥 人家聽不懂啦", "瞎咪挖溝 可以再說一次嗎", "我的老天鵝阿 你在說什麼" };
            int i = (int)(Math.random() * errors.length);
            speak(errors[i], LISTEN);
        }
    }
}
