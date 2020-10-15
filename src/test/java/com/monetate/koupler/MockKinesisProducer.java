package com.monetate.koupler;

import com.amazonaws.services.kinesis.producer.KinesisProducer;
import com.amazonaws.services.kinesis.producer.UserRecord;
import com.amazonaws.services.kinesis.producer.UserRecordResult;
import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class MockKinesisProducer extends KinesisProducer {
    public AtomicInteger COUNT = new AtomicInteger();

    public int getCOUNT() {
        return COUNT.get();
    }

    @Override
    public ListenableFuture<UserRecordResult> addUserRecord(String stream, String partitionKey, ByteBuffer data) {
        COUNT.getAndIncrement();
        return null;
    }

    @Override
    public ListenableFuture<UserRecordResult> addUserRecord(UserRecord userRecord) {
        COUNT.getAndIncrement();
        return null;
    }
}
