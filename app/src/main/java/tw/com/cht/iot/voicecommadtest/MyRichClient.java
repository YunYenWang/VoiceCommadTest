package tw.com.cht.iot.voicecommadtest;

import com.cht.iot.api.OpenRESTfulClient;
import com.cht.iot.api.Rawdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    String getRawdata(OpenRESTfulClient c, String deviceId, String sensorId) throws IOException {
        Rawdata r = c.getRawdata(deviceId, sensorId);
        if ((r.value != null) && (r.value.length > 0)) {
            return r.value[0];
        }

        return "";
    }

    public String pir() throws InterruptedException {
        final StringBuilder sb = new StringBuilder();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    OpenRESTfulClient c = new OpenRESTfulClient(host, port, "DKP2YBSKPSSTKFH55C");

                    sb.append(String.format("溫度 %s 濕度 %s PM 二點五 %s CO兔 %s PPM",
                            getRawdata(c, "830323652", "CH1"),
                            getRawdata(c, "830323652", "CH2"),
                            getRawdata(c, "830323652", "CH3"),
                            getRawdata(c, "830323652", "CH4")));

                    synchronized (sb) {
                        sb.notify();
                    }

                } catch (Exception e) {
                    LOG.error("Network error", e);
                }
            }
        });

        synchronized (sb) {
            sb.wait(2000L);
        }

        return sb.toString();
    }

    public List<String> weathers() throws InterruptedException {
        final List<String> ws = new ArrayList<>();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://play-yunyenwang.rhcloud.com/weather?text");
                    InputStreamReader isr = new InputStreamReader(url.openStream(), "UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    String ln;
                    while ((ln = br.readLine()) != null) {
                        ws.add(ln);
                    }

                    synchronized (ws) {
                        ws.notify();
                    }

                } catch (Exception e) {
                    LOG.error("Network error", e);
                }
            }
        });

        synchronized (ws) {
            ws.wait(5000L);
        }

        return ws;
    }
}
