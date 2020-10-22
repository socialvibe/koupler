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

    String delimiter = ",";

    public HttpKoupler(int port, KinesisProducer producer) {
        this.producer = producer;
        LOGGER.info("Firing up HTTP listener on [{}]", port);
    }

    public HttpKoupler(int port, String propertiesFile) {
        KinesisProducerConfiguration config = KinesisProducerConfiguration.fromPropertiesFile(propertiesFile);
        this.producer = new KinesisProducer(config);
        LOGGER.info("Firing up HTTP listener on [{}]", port);
    }

    /** Returns partition key from an event
     *
     * Our convention is that the event string should not be null and should
     * contain the delimiter. If it's not the case, we always return null.
     *
     * TODO: we can probably remove the try catch block.
     */
    public String getPartitionKey(String event) {
        try {
            if (event.contains(delimiter)) {
                return event.split(this.delimiter, 2)[0];
            } else {
                LOGGER.warn("Received event from which we could NOT extract partition key.");
                return null;
            }
        } catch (Exception e) {
            LOGGER.warn("Received event from which we could NOT extract partition key.", e);
            return null;
        }
    }

    /** Returns data from an event
     *
     * Our convention is that the event string should not be null and should
     * contain the delimiter. If it's not the case, we always return null.
     *
     * TODO: we can probably remove the try catch block.
     */
    public String getData(String event) {
        try {
            if (event.contains(delimiter)) {
                return event.split(this.delimiter, 2)[1];
            } else {
                LOGGER.warn("Received event from which we could NOT extract data.");
                return null;
            }
        } catch (Exception e) {
            LOGGER.warn("Received event from which we could NOT extract data.", e);
            return null;
        }
    }
    
    @Override
    public void run() {
        post("/:stream", (request, response) -> {
            String streamName = request.params(":stream");
            String event = request.body();
            String partitionKey = getPartitionKey(event);
            String data = getData(event);

            if (partitionKey != null) {
                byte[] bytes = data.getBytes("UTF-8");
                ByteBuffer buffer = ByteBuffer.wrap(bytes);

                UserRecord record = new UserRecord(streamName, partitionKey, buffer.asReadOnlyBuffer());

                LOGGER.debug("request body: " + data);
                LOGGER.debug("request partition key: " + partitionKey);
                producer.addUserRecord(record);
            }

            return "ACK\n";
        });
    }
}
