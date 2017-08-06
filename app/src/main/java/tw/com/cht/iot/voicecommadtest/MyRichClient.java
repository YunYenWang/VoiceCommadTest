package tw.com.cht.iot.voicecommadtest;

import com.cht.iot.api.OpenRESTfulClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by rickwang on 2017/8/6.
 */

public class MyRichClient {
    static final Logger LOG = LoggerFactory.getLogger(MyRichClient.class);

    String host = "iot.cht.com.tw";
    int port = 80;
    String apiKey = "DKK2SSMMR3B3M1Y7RC";
    String deviceId = "4409568060";

    OpenRESTfulClient client;

    ExecutorService executor;

    public MyRichClient() {
        client = new OpenRESTfulClient(host, port, apiKey);

        executor = Executors.newSingleThreadExecutor();
    }

    public void close() {
        executor.shutdown();
    }

    public void controlLamp(final boolean on) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    client.saveRawdata(deviceId, "Lamp", null, on? "1" : "0");

                } catch (Exception e) {
                    LOG.error("I/O error", e);
                }
            }
        });
    }
}
