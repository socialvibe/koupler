package com.monetate.koupler;

import static spark.Spark.post;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpKoupler extends Koupler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpKoupler.class);

    private Map<String, KinesisEventProducer> producers = new HashMap();

    private KinesisEventProducer getOrCreateProducer(String streamName) {
        KinesisEventProducer producer = producers.get(streamName);
        if (producer == null) {
            producer = new KinesisEventProducer(format, cmd, propertiesFile, streamName, queueSize, appName);
            new Thread(producer).start();
        }
        return producer;
    }

    public HttpKoupler(int port) {
        super(20);
        LOGGER.info("Firing up HTTP listener on [{}]", port);
    }
    
    @Override
    public void run() {
        post("/:stream", (request, response) -> {
            String event = request.body();
            getOrCreateProducer(request.params(":stream")).queueEvent(event);
            return "ACK\n";
        });
    }
}
