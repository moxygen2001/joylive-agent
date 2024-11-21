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
package com.jd.live.agent.implement.service.policy.apollo.client;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.jd.live.agent.governance.service.sync.SyncResponse;
import com.jd.live.agent.governance.service.sync.SyncStatus;
import com.jd.live.agent.governance.service.sync.Syncer;
import com.jd.live.agent.implement.service.policy.apollo.ApolloSyncKey;
import com.jd.live.agent.implement.service.policy.apollo.config.ApolloSyncConfig;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A client for interacting with the Nacos configuration service.
 */
public class ApolloClient {

    private final ApolloSyncConfig syncConfig;

    private final String namespace;

    private final Config config;

    public ApolloClient(ApolloSyncConfig syncConfig) {
        this.namespace = syncConfig.getApollo().getNamespace();
        this.syncConfig = syncConfig;
        this.config = ConfigService.getConfig(namespace);
    }

    public void subscribe(String key, ConfigChangeListener listener) {
        String value = config.getProperty(key, null);
        Map<String, ConfigChange> changes = new HashMap<>();
        changes.put(key, new ConfigChange(namespace, key, null, value, PropertyChangeType.MODIFIED));
        listener.onChange(new ConfigChangeEvent(namespace, changes));
        config.addChangeListener(listener, Collections.singleton(key));
    }

    public void unsubscribe(ConfigChangeListener listener) {
        config.removeChangeListener(listener);
    }

    public <K extends ApolloSyncKey, T> Syncer<K, T> createSyncer(Function<String, SyncResponse<T>> parser) {
        return subscription -> {
            try {
                subscribe(subscription.getKey().getKey(), changeEvent -> changeEvent.changedKeys().forEach(key -> {
                    ConfigChange change = changeEvent.getChange(key);
                    switch (change.getChangeType()) {
                        case DELETED:
                            subscription.onUpdate(new SyncResponse<>(SyncStatus.NOT_FOUND, null));
                            break;
                        case ADDED:
                        case MODIFIED:
                            subscription.onUpdate(parser.apply(change.getNewValue()));
                            break;
                    }

                }));
            } catch (Throwable e) {
                subscription.onUpdate(new SyncResponse<>(e));
            }
        };
    }
}
