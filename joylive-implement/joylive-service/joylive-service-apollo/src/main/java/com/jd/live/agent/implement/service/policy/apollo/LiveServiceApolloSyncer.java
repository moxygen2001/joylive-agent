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

import com.jd.live.agent.core.config.SyncConfig;
import com.jd.live.agent.core.extension.annotation.ConditionalOnProperty;
import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.core.inject.annotation.Config;
import com.jd.live.agent.core.inject.annotation.Injectable;
import com.jd.live.agent.core.util.template.Template;
import com.jd.live.agent.governance.config.GovernanceConfig;
import com.jd.live.agent.governance.policy.PolicySubscriber;
import com.jd.live.agent.governance.policy.listener.ServiceEvent;
import com.jd.live.agent.governance.policy.service.MergePolicy;
import com.jd.live.agent.governance.policy.service.Service;
import com.jd.live.agent.governance.service.sync.AbstractServiceSyncer;
import com.jd.live.agent.governance.service.sync.Syncer;
import com.jd.live.agent.implement.service.policy.apollo.client.ApolloClient;
import com.jd.live.agent.implement.service.policy.apollo.config.ApolloSyncConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * LiveServiceNacosSyncer is responsible for synchronizing live service policies from apollo.
 */
@Injectable
@Extension("LiveServiceApolloSyncer")
@ConditionalOnProperty(name = SyncConfig.SYNC_LIVE_SPACE_TYPE, value = "apollo")
@ConditionalOnProperty(name = SyncConfig.SYNC_LIVE_SPACE_SERVICE, matchIfMissing = true)
@ConditionalOnProperty(name = GovernanceConfig.CONFIG_LIVE_ENABLED, matchIfMissing = true)
public class LiveServiceApolloSyncer extends AbstractServiceSyncer<ApolloServiceKey> {

    @Config(SyncConfig.SYNC_LIVE_SPACE)
    private ApolloSyncConfig syncConfig = new ApolloSyncConfig();

    private ApolloClient client;

    public LiveServiceApolloSyncer() {
        name = "service-apollo-syncer";
    }

    @Override
    protected CompletableFuture<Void> doStart() {
        client = new ApolloClient(syncConfig);
        return super.doStart();
    }

    @Override
    protected void stopSync() {
        client = null;
        super.stopSync();
    }

    @Override
    protected ApolloSyncConfig getSyncConfig() {
        return syncConfig;
    }

    @Override
    protected Template createTemplate() {
        return new Template(syncConfig.getApollo().getLiveServiceTemplate());
    }

    @Override
    protected ApolloServiceKey createServiceKey(PolicySubscriber subscriber) {
        Map<String, Object> context = new HashMap<>();
        context.put("name", subscriber.getName());
        context.put("space", application.getService().getNamespace());
        String key = template.evaluate(context);
        return new ApolloServiceKey(subscriber, key);
    }

    @Override
    protected Syncer<ApolloServiceKey, Service> createSyncer() {
        return client.createSyncer(this::parse);
    }

    @Override
    protected void configure(ServiceEvent event) {
        event.setMergePolicy(MergePolicy.LIVE);
    }

}
