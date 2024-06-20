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
package com.jd.live.agent.plugin.router.rocketmq.v4.definition;

import com.jd.live.agent.core.bytekit.matcher.MatcherBuilder;
import com.jd.live.agent.core.extension.annotation.ConditionalOnClass;
import com.jd.live.agent.core.extension.annotation.ConditionalOnProperty;
import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.core.inject.annotation.Config;
import com.jd.live.agent.core.inject.annotation.Inject;
import com.jd.live.agent.core.inject.annotation.Injectable;
import com.jd.live.agent.core.instance.Application;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinition;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinitionAdapter;
import com.jd.live.agent.core.plugin.definition.PluginDefinitionAdapter;
import com.jd.live.agent.governance.config.GovernanceConfig;
import com.jd.live.agent.plugin.router.rocketmq.v4.interceptor.SetConsumerGroupInterceptor;

@Injectable
@Extension(value = "DefaultLitePullConsumerDefinition_v4")
@ConditionalOnProperty(name = {
        GovernanceConfig.CONFIG_LIVE_ENABLED,
        GovernanceConfig.CONFIG_LANE_ENABLED
}, matchIfMissing = true)
@ConditionalOnProperty(value = GovernanceConfig.CONFIG_LIVE_ROCKETMQ_ENABLED, matchIfMissing = true)
@ConditionalOnClass(DefaultLitePullConsumerDefinition.TYPE_DEFAULT_LITE_PULL_CONSUMER)
@ConditionalOnClass(PullAPIWrapperDefinition.TYPE_CLIENT_LOGGER)
public class DefaultLitePullConsumerDefinition extends PluginDefinitionAdapter {

    protected static final String TYPE_DEFAULT_LITE_PULL_CONSUMER = "org.apache.rocketmq.client.consumer.DefaultLitePullConsumer";

    private static final String METHOD_SET_CONSUMER_GROUP = "setConsumerGroup";

    private static final String[] ARGUMENT_SET_CONSUMER_GROUP = new String[]{
            "java.lang.String"
    };

    @Inject(Application.COMPONENT_APPLICATION)
    private Application application;

    @Config(GovernanceConfig.CONFIG_LIVE_ENABLED)
    private boolean liveEnabled = true;

    @Config(GovernanceConfig.CONFIG_LANE_ENABLED)
    private boolean laneEnabled = true;

    public DefaultLitePullConsumerDefinition() {
        this.matcher = () -> MatcherBuilder.named(TYPE_DEFAULT_LITE_PULL_CONSUMER);
        this.interceptors = new InterceptorDefinition[]{
                new InterceptorDefinitionAdapter(
                        MatcherBuilder.named(METHOD_SET_CONSUMER_GROUP).
                                and(MatcherBuilder.arguments(ARGUMENT_SET_CONSUMER_GROUP)),
                        () -> new SetConsumerGroupInterceptor(application, liveEnabled, laneEnabled)
                )
        };
    }
}