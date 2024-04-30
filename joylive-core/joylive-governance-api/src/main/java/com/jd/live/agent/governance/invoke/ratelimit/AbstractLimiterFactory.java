/*
 * Copyright © ${year} ${owner} (${email})
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jd.live.agent.governance.invoke.ratelimit;

import com.jd.live.agent.governance.policy.service.limit.RateLimitPolicy;
import com.jd.live.agent.governance.policy.service.limit.SlidingWindow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * AbstractLimiterFactory provides a base implementation for factories that create and manage rate limiters.
 * It uses a thread-safe map to store and retrieve rate limiters associated with specific rate limit policies.
 * This class is designed to be extended by concrete factory implementations that provide the actual
 * rate limiter creation logic.
 *
 * @since 1.0.0
 */
public abstract class AbstractLimiterFactory implements RateLimiterFactory {

    /**
     * A thread-safe map to store rate limiters associated with their respective policies.
     * The keys are the policy IDs, and the values are atomic references to the rate limiters.
     */
    protected final Map<Long, AtomicReference<RateLimiter>> rateLimiters = new ConcurrentHashMap<>();

    /**
     * Retrieves a rate limiter for the given rate limit policy. If a rate limiter for the policy
     * already exists and its version is greater than or equal to the policy version, it is returned.
     * Otherwise, a new rate limiter is created using the {@link #create(RateLimitPolicy)} method.
     *
     * @param policy The rate limit policy for which to retrieve or create a rate limiter.
     * @return A rate limiter that corresponds to the given policy, or null if the policy is null.
     */
    @Override
    public RateLimiter get(RateLimitPolicy policy) {
        if (policy == null) {
            return null;
        }
        List<SlidingWindow> windows = policy.getSlidingWindows();
        if (windows == null || windows.isEmpty()) {
            return null;
        }
        AtomicReference<RateLimiter> reference = rateLimiters.computeIfAbsent(policy.getId(), n -> new AtomicReference<>());
        RateLimiter rateLimiter = reference.get();
        if (rateLimiter != null && rateLimiter.getPolicy().getVersion() >= policy.getVersion()) {
            return rateLimiter;
        }
        RateLimiter newLimiter = create(policy);
        while (true) {
            rateLimiter = reference.get();
            if (rateLimiter == null || rateLimiter.getPolicy().getVersion() < policy.getVersion()) {
                if (reference.compareAndSet(rateLimiter, newLimiter)) {
                    rateLimiter = newLimiter;
                    break;
                }
            }
        }
        return rateLimiter;
    }

    /**
     * Creates a new rate limiter instance based on the provided rate limit policy.
     * This method is abstract and must be implemented by subclasses to provide the specific
     * rate limiter creation logic.
     *
     * @param policy The rate limit policy to be used for creating the rate limiter.
     * @return A new rate limiter instance that enforces the given policy.
     */
    protected abstract RateLimiter create(RateLimitPolicy policy);
}

