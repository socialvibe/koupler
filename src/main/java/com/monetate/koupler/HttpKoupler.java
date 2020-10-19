package com.monetate.koupler;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.KinesisProducerConfiguration;
import com.amazonaws.services.kinesis.producer.UserRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

import static spark.Spark.post;

public class HttpKoupler implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpKoupler.class);

    KinesisProducer producer;

    public HttpKoupler(int port, KinesisProducer producer) {
        this.producer = producer;
        LOGGER.info("Firing up HTTP listener on [{}]", port);
    }

    public HttpKoupler(int port, String propertiesFile) {
        KinesisProducerConfiguration config = KinesisProducerConfiguration.fromPropertiesFile(propertiesFile);
        this.producer = new KinesisProducer(config);
        LOGGER.info("Firing up HTTP listener on [{}]", port);
    }
    
    @Override
    public void run() {
        post("/:stream", (request, response) -> {
            String streamName = request.params(":stream");
            String msg = request.body();
            String partitionKey = msg.split(",", 2)[0];
            String data = msg.split(",", 2)[1];

            byte[] bytes = data.getBytes("UTF-8");
            ByteBuffer buffer = ByteBuffer.wrap(bytes);

            UserRecord record = new UserRecord(streamName, partitionKey, buffer.asReadOnlyBuffer());

            LOGGER.debug("request body: " + data);
            LOGGER.debug("request partition key: " + partitionKey);
            producer.addUserRecord(record);
            return "ACK\n";
        });
    }
}
