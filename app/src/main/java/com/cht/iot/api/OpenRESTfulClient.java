package com.cht.iot.api;

import com.cht.iot.utils.JsonUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class OpenRESTfulClient {
    static final Logger LOG = LoggerFactory.getLogger(OpenRESTfulClient.class);

    public static final DateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    static {
        DF.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    final String url; // http://iot.cht.com.tw:80/iot/v1
    final String apiKey;

    public OpenRESTfulClient(String host, int port, String apiKey) {
        url = String.format("http://%s:%d/iot/v1", host, port);

        this.apiKey = apiKey;
    }

    InputStream request(HttpURLConnection con) throws IOException {
        con.connect();

        int sc = con.getResponseCode();
        if (sc != 200) {
            throw new IOException(String.format("%d: %s", sc, con.getResponseMessage()));
        }

        return con.getInputStream();
    }

    <T> T fromJson(InputStream is, Class<T> clazz) throws IOException {
        return JsonUtils.fromJson(new InputStreamReader(is, "UTF-8"), clazz);
    }

    <T> T httpGet(URL u, Class<T> clazz) throws IOException {
        HttpURLConnection con = (HttpURLConnection) u.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("CK", apiKey);

        return fromJson(request(con), clazz);
    }
    
    InputStream httpPost(URL u, Object payload) throws IOException {
    	HttpURLConnection con = (HttpURLConnection) u.openConnection();
    	con.setRequestMethod("POST");    	
    	con.setRequestProperty("CK", apiKey);
    	con.setDoOutput(true);
    	
    	OutputStreamWriter os = new OutputStreamWriter(con.getOutputStream(), "UTF-8");
    	os.write(JsonUtils.toJson(payload));
    	os.flush();
    	
    	return request(con);
    }

    String encode(String s) throws IOException {
        return URLEncoder.encode(s, "UTF-8");
    }
    
    public Device[] getDevices() throws IOException {
        URL u = new URL(String.format("%s/device", url));

        return httpGet(u, Device[].class);
    }
    
    public Device saveDevice(Device device) throws IOException {
    	URL u = new URL(String.format("%s/device", url));
    	
    	Reply reply = fromJson(httpPost(u, device), Reply.class);
    	device.id = reply.id;
    	
    	return device;
    }
    
    public Sensor[] getSensors(String deviceId) throws IOException {
    	URL u = new URL(String.format("%s/device/%s/sensor", url, deviceId));
    	
    	return httpGet(u, Sensor[].class);
    }
    
    public Sensor saveSensor(String deviceId, Sensor sensor) throws IOException {
    	URL u = new URL(String.format("%s/device/%s/sensor", url, deviceId));
    	
    	httpPost(u, sensor);
    	
    	return sensor;
    }

    public Rawdata getRawdata(String deviceId, String sensorId) throws IOException {
        URL u = new URL(String.format("%s/device/%s/sensor/%s/rawdata", url, deviceId, sensorId));

        return httpGet(u, Rawdata.class);
    }

    public Rawdata[] getRawdatas(String deviceId, String sensorId, String start, String end) throws IOException {
        if (start == null) {
            throw new IOException("You must specify the start timestamp");
        }
        start = encode(start);

        StringBuilder sb = new StringBuilder(String.format("%s/device/%s/sensor/%s/rawdata?start=%s&", url, deviceId, sensorId, start));
        if (end != null) {
            end = encode(end);
            sb.append("end=");
            sb.append(end);
            sb.append('&');
        }

        URL u = new URL(sb.substring(0, sb.length() - 1));

        return httpGet(u, Rawdata[].class);
    }
    
    public void saveRawdata(String deviceId, String sensorId, String timestamp, String value) throws IOException {
    	URL u = new URL(String.format("%s/device/%s/rawdata", url, deviceId));
    	
    	Rawdata rawdata = new Rawdata();
    	rawdata.id = sensorId;
    	rawdata.time = timestamp; // "2017-01-31T12:36:28.038Z"
    	rawdata.save = true;
    	rawdata.value = new String[] { value };    	
    	
    	httpPost(u, new Rawdata[] { rawdata });
    }
}
