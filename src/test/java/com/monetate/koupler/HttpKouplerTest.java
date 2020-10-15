package com.monetate.koupler;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class HttpKouplerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpKouplerTest.class);

    @Test
    public void testRest() throws Exception {
        MockKinesisProducer producer = new MockKinesisProducer();
        Thread server = new Thread(new HttpKoupler(4567, producer));
        server.start();
        Thread.sleep(1000);

        URIBuilder builder = new URIBuilder();
        CloseableHttpClient client = HttpClients.createDefault();
        builder.setScheme("http").setHost("localhost").setPort(4567).setPath("/test_stream");
        HttpPost post = new HttpPost(builder.toString());
        post.setEntity(new ByteArrayEntity("key,data".getBytes()));

        CloseableHttpResponse response = client.execute(post);
        String responseBody = EntityUtils.toString(response.getEntity());
        LOGGER.info("Received [{}] as response from HTTP server.", responseBody);
        assertEquals("Request should succeed", 200, response.getStatusLine().getStatusCode());
        assertEquals("one event received", 1, producer.getCOUNT());
    }
}
