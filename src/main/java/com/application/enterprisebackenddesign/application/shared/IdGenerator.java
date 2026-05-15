package com.application.enterprisebackenddesign.application.shared;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * Generates collision-resistant positive Long IDs using SecureRandom.
 *
 * Interview context: This was introduced in ticket 28 to replace
 * UUID.randomUUID().getMostSignificantBits() which was scattered across
 * controllers and use cases. Centralizing ID generation gives us:
 *
 * 1. Consistency — every entity uses the same strategy.
 * 2. Testability — we can mock IdGenerator in tests.
 * 3. Auditability — we can add metrics/logging in one place.
 *
 * Why SecureRandom over AtomicLong?
 * - AtomicLong would produce sequential IDs that leak business info
 *   (order count, growth rate) — a security concern for customer-facing IDs.
 * - SecureRandom gives 63 bits of randomness (2^63 ≈ 9 quintillion values)
 *   with astronomically low collision probability.
 *
 * The & Long.MAX_VALUE ensures positive IDs only. Negative IDs cause issues
 * with URL paths, JSON serialization, and human readability.
 */
@Component
public class IdGenerator {
    private final SecureRandom random = new SecureRandom();

    public long generateId() {
        return random.nextLong() & Long.MAX_VALUE;
    }
}
