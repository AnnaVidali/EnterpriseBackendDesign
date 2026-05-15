package com.application.enterprisebackenddesign.application.shared;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class IdGenerator {
    private final SecureRandom random = new SecureRandom();

    public long generateId() {
        return random.nextLong() & Long.MAX_VALUE;
    }
}
