package com.application.enterprisebackenddesign.application.shared;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {

    private final IdGenerator idGenerator = new IdGenerator();

    @Test
    void shouldGeneratePositiveId() {
        long id = idGenerator.generateId();
        assertThat(id).isPositive();
    }

    @RepeatedTest(100)
    void shouldGenerateUniqueIdsSequentially() {
        Set<Long> ids = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ids.add(idGenerator.generateId());
        }
        assertThat(ids).hasSize(1000);
    }

    @Test
    void shouldGenerateUniqueIdsUnderConcurrentLoad() throws Exception {
        int threadCount = 10;
        int idsPerThread = 1000;
        Set<Long> ids = Collections.newSetFromMap(new ConcurrentHashMap<>());
        var executor = Executors.newFixedThreadPool(threadCount);
        var latch = new CountDownLatch(threadCount);

        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < idsPerThread; i++) {
                        ids.add(idGenerator.generateId());
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertThat(ids).hasSize(threadCount * idsPerThread);
    }
}
