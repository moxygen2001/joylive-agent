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
package com.jd.live.agent.governance.policy.service.limit;

import com.jd.live.agent.governance.policy.PolicyInherit.PolicyInheritWithIdGen;
import com.jd.live.agent.governance.policy.service.annotation.Provider;
import com.jd.live.agent.governance.rule.tag.TagCondition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Implements a concurrency-based rate limiting policy. This policy limits the number of concurrent
 * requests or actions that can be performed within a system or component. It extends {@link AbstractLimitPolicy}
 * to utilize common rate limiting attributes while introducing specific properties related to concurrency control,
 * such as maximum concurrency level and maximum wait time.
 * <p>
 * This policy is useful in scenarios where it's critical to control the load on resources by limiting how many
 * concurrent operations can occur. This helps in maintaining system stability and ensuring that resources are
 * not overwhelmed by too many simultaneous requests.
 * </p>
 *
 * @since 1.0.0
 */
@Getter
@Setter
@Provider
public class ConcurrencyLimitPolicy extends AbstractLimitPolicy implements PolicyInheritWithIdGen<ConcurrencyLimitPolicy> {

    /**
     * The query parameter name for concurrency limit.
     */
    public static final String QUERY_CONCURRENCY_LIMIT = "concurrencyLimit";

    /**
     * The maximum number of concurrent requests allowed.
     */
    private int maxConcurrency;

    /**
     * The maximum time, in milliseconds, a request can wait to be executed before it is rejected, when the maximum
     * concurrency limit has been reached.
     */
    private long maxWaitMs;

    /**
     * Constructs a new {@code ConcurrencyLimitPolicy} with default settings.
     */
    public ConcurrencyLimitPolicy() {
    }

    /**
     * Constructs a new {@code ConcurrencyLimitPolicy} with the specified policy name.
     *
     * @param name the name of the concurrency limit policy
     */
    public ConcurrencyLimitPolicy(String name) {
        super(name);
    }

    /**
     * Constructs a new {@code ConcurrencyLimitPolicy} with detailed specifications.
     *
     * @param name           the name of the concurrency limit policy
     * @param strategyType   the strategy type of the rate limiting policy
     * @param matchSource    a list of conditions (tags) for the rate limiting policy
     * @param maxConcurrency the maximum number of concurrent requests allowed
     * @param version        the version of the rate limiting policy
     */
    public ConcurrencyLimitPolicy(String name, String strategyType, List<TagCondition> matchSource,
                                  int maxConcurrency, long version) {
        super(name, strategyType, matchSource, version);
        this.maxConcurrency = maxConcurrency;
    }

    /**
     * Supplements the current concurrency limit policy with another policy's details. This method is used
     * to inherit or override attributes from another policy. If the current policy lacks specific attributes,
     * they are filled in with the values from the source policy, specifically focusing on concurrency settings.
     *
     * @param source the source concurrency limit policy to supplement from
     */
    @Override
    public void supplement(ConcurrencyLimitPolicy source) {
        if (source == null) {
            return;
        }
        super.supplement(source);
        if (maxConcurrency <= 0) {
            maxConcurrency = source.maxConcurrency;
        }
    }
}

