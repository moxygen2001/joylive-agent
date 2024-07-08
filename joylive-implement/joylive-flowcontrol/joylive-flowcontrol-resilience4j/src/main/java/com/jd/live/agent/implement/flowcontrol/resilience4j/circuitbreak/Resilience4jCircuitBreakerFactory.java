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
package com.jd.live.agent.implement.flowcontrol.resilience4j.circuitbreak;

import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.core.inject.annotation.Injectable;
import com.jd.live.agent.core.util.URI;
import com.jd.live.agent.governance.exception.CircuitBreakException;
import com.jd.live.agent.governance.invoke.circuitbreak.AbstractCircuitBreakerFactory;
import com.jd.live.agent.governance.invoke.circuitbreak.CircuitBreaker;
import com.jd.live.agent.governance.policy.PolicyId;
import com.jd.live.agent.governance.policy.service.circuitbreaker.CircuitBreakerPolicy;
import com.jd.live.agent.governance.policy.service.circuitbreaker.CircuitLevel;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;

/**
 * Resilience4jCircuitBreakerFactory
 *
 * @since 1.1.0
 */
@Injectable
@Extension(value = "Resilience4j")
public class Resilience4jCircuitBreakerFactory extends AbstractCircuitBreakerFactory {

    /**
     * {@inheritDoc}
     */
    @Override
    public CircuitBreaker create(CircuitBreakerPolicy policy, URI uri) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowType(policy.getSlidingWindowType().equalsIgnoreCase("time") ?
                        CircuitBreakerConfig.SlidingWindowType.TIME_BASED : CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(policy.getSlidingWindowSize())
                .minimumNumberOfCalls(policy.getMinCallsThreshold())
                .failureRateThreshold(policy.getFailureRateThreshold())
                .slowCallRateThreshold(policy.getSlowCallRateThreshold())
                .slowCallDurationThreshold(Duration.ofMillis(policy.getSlowCallDurationThreshold()))
                .waitDurationInOpenState(Duration.ofMillis(policy.getWaitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(policy.getAllowedCallsInHalfOpenState())
                .recordException(exception -> exception instanceof CircuitBreakException)
                .build();
        io.github.resilience4j.circuitbreaker.CircuitBreaker cb = CircuitBreakerRegistry.of(circuitBreakerConfig).circuitBreaker(uri.toString());
        CircuitBreaker circuitBreaker = new Resilience4jCircuitBreaker(policy, uri, cb);
        if (policy.getLevel() == CircuitLevel.INSTANCE) {
            circuitBreaker.registerListener(new InstanceCircuitBreakerStateListener(policy, uri.getParameter(PolicyId.KEY_SERVICE_ENDPOINT)));
        }
        return circuitBreaker;
    }

}
