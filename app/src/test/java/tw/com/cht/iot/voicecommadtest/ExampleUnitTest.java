package tw.com.cht.iot.voicecommadtest;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
//    static final Logger LOG = LoggerFactory.getLogger(ExampleUnitTest.class);

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testYouTube() throws Exception {
        MyRichClient client = new MyRichClient();

        MyRichClient.Trailer t = client.searchTrailer("五月天");

//        LOG.info("{} {}", t.title, t.url);

        System.out.printf("%s %s", t.title, t.url);
    }
}