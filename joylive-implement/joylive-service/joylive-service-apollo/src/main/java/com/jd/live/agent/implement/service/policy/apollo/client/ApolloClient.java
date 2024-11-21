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

import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.enums.PropertyChangeType;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.jd.live.agent.governance.service.sync.SyncResponse;
import com.jd.live.agent.governance.service.sync.SyncStatus;
import com.jd.live.agent.governance.service.sync.Syncer;
import com.jd.live.agent.implement.service.policy.apollo.ApolloSyncKey;
import com.jd.live.agent.implement.service.policy.apollo.config.ApolloSyncConfig;

import java.util.function.Function;

/**
 * A client for interacting with the Nacos configuration service.
 */
public class ApolloClient {

    private final ApolloSyncConfig syncConfig;

    public ApolloClient(ApolloSyncConfig syncConfig) {
        this.syncConfig = syncConfig;
    }

    public void subscribe(String namespace, ConfigFileChangeListener listener) {
        ConfigFile config = ConfigService.getConfigFile(namespace, ConfigFileFormat.TXT);
        listener.onChange(new ConfigFileChangeEvent(namespace, null, config.getContent(), PropertyChangeType.ADDED));
        config.addChangeListener(listener);
    }

    public void unsubscribe(String namespace, ConfigFileChangeListener listener) {
        ConfigFile config = ConfigService.getConfigFile(namespace, ConfigFileFormat.TXT);
        config.removeChangeListener(listener);
    }

    public <K extends ApolloSyncKey, T> Syncer<K, T> createSyncer(Function<String, SyncResponse<T>> parser) {
        return subscription -> {
            try {
                subscribe(subscription.getKey().getNamespace(), event -> {
                    switch (event.getChangeType()) {
                        case DELETED:
                            subscription.onUpdate(new SyncResponse<>(SyncStatus.NOT_FOUND, null));
                            break;
                        case MODIFIED:
                        case ADDED:
                            subscription.onUpdate(parser.apply(event.getNewValue()));
                            break;
                    }
                });
            } catch (Throwable e) {
                subscription.onUpdate(new SyncResponse<>(e));
            }
        };
    }
}
