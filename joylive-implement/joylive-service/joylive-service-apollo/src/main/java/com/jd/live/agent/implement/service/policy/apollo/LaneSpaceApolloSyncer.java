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
import com.jd.live.agent.governance.policy.lane.LaneSpace;
import com.jd.live.agent.governance.service.sync.AbstractLaneSpaceSyncer;
import com.jd.live.agent.governance.service.sync.SyncKey.LaneSpaceKey;
import com.jd.live.agent.governance.service.sync.Syncer;
import com.jd.live.agent.governance.service.sync.api.ApiSpace;
import com.jd.live.agent.implement.service.policy.apollo.client.ApolloClient;
import com.jd.live.agent.implement.service.policy.apollo.config.ApolloSyncConfig;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.jd.live.agent.implement.service.policy.apollo.LaneSpaceApolloSyncer.ApolloLaneSpaceKey;

/**
 * LaneSpaceNacosSyncer is responsible for synchronizing lane spaces policies from apollo.
 */
@Injectable
@Extension("LaneSpaceApolloSyncer")
@ConditionalOnProperty(name = SyncConfig.SYNC_LANE_SPACE_TYPE, value = "apollo")
@ConditionalOnProperty(name = GovernanceConfig.CONFIG_LANE_ENABLED, matchIfMissing = true)
public class LaneSpaceApolloSyncer extends AbstractLaneSpaceSyncer<ApolloLaneSpaceKey> {

    @Config(SyncConfig.SYNC_LANE_SPACE)
    private ApolloSyncConfig syncConfig = new ApolloSyncConfig();

    private ApolloClient client;

    public LaneSpaceApolloSyncer() {
        name = "lane-space-apollo-syncer";
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
        return new Template(syncConfig.getApollo().getLaneSpaceKeyTemplate());
    }

    @Override
    protected ApolloLaneSpaceKey createSpaceListKey() {
        return new ApolloLaneSpaceKey(null, syncConfig.getApollo().getLaneSpacesKey());
    }

    @Override
    protected ApolloLaneSpaceKey createSpaceKey(String spaceId) {
        Map<String, Object> context = new HashMap<>();
        context.put("id", spaceId);
        String key = template.evaluate(context);
        return new ApolloLaneSpaceKey(spaceId, key);
    }

    @Override
    protected Syncer<ApolloLaneSpaceKey, List<ApiSpace>> createSpaceListSyncer() {
        return client.createSyncer(this::parseSpaceList);
    }

    @Override
    protected Syncer<ApolloLaneSpaceKey, LaneSpace> createSyncer() {
        return client.createSyncer(this::parseSpace);
    }

    @Getter
    protected static class ApolloLaneSpaceKey extends LaneSpaceKey implements ApolloSyncKey {

        private final String key;

        public ApolloLaneSpaceKey(String id, String key) {
            super(id);
            this.key = key;
        }
    }

}
