package tw.com.cht.iot.voicecommadtest;

/**
 * Created by rickwang on 2017/8/8.
 */

public class Future<T> {

    T object;

    public synchronized void set(T object) {
        this.object = object;

        notifyAll();
    }

    public synchronized T get(long timeout) throws InterruptedException {
        if (object == null) {
            wait(timeout);
        }

        if (object == null) {
            throw new InterruptedException("timeout");
        }

        return object;
    }
}
