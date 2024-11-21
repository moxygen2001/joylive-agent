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
package com.jd.live.agent.implement.service.policy.apollo;

import com.jd.live.agent.governance.policy.PolicySubscriber;
import com.jd.live.agent.governance.service.sync.SyncKey.ServiceKey;
import lombok.Getter;

@Getter
public class ApolloServiceKey extends ServiceKey implements ApolloSyncKey {

    private final String key;

    public ApolloServiceKey(PolicySubscriber subscriber, String key) {
        super(subscriber);
        this.key = key;
    }
}
