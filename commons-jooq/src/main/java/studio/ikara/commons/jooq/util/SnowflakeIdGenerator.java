package studio.ikara.commons.jooq.util;

import java.util.concurrent.atomic.AtomicLong;

public final class SnowflakeIdGenerator {

    private static final long EPOCH = 1672531200000L; // 2023-01-01 in ms
    private static final int SHARD_ID = 1;
    private static final long SEQ_MASK = 1023L; // 10 bits

    private static final AtomicLong sequence = new AtomicLong(0);

    private SnowflakeIdGenerator() {}

    public static long nextId() {
        long nowMillis = System.currentTimeMillis();
        long seq = sequence.incrementAndGet() & SEQ_MASK;
        return ((nowMillis - EPOCH) << 23) | ((long) SHARD_ID << 10) | seq;
    }
}
