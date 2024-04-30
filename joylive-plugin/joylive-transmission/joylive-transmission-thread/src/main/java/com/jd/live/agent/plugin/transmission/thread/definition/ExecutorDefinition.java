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
package com.jd.live.agent.plugin.transmission.thread.definition;

import com.jd.live.agent.bootstrap.classloader.ResourcerType;
import com.jd.live.agent.core.bytekit.matcher.MatcherBuilder;
import com.jd.live.agent.core.extension.annotation.ConditionalOnProperty;
import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.core.inject.annotation.Config;
import com.jd.live.agent.core.inject.annotation.Inject;
import com.jd.live.agent.core.inject.annotation.InjectLoader;
import com.jd.live.agent.core.inject.annotation.Injectable;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinition;
import com.jd.live.agent.core.plugin.definition.InterceptorDefinitionAdapter;
import com.jd.live.agent.core.plugin.definition.PluginDefinition;
import com.jd.live.agent.core.plugin.definition.PluginDefinitionAdapter;
import com.jd.live.agent.core.thread.Camera;
import com.jd.live.agent.governance.config.GovernanceConfig;
import com.jd.live.agent.plugin.transmission.thread.config.ThreadConfig;
import com.jd.live.agent.plugin.transmission.thread.interceptor.ExecutorInterceptor;

import java.util.List;

import static com.jd.live.agent.plugin.transmission.thread.config.ThreadConfig.CONFIG_THREAD_PREFIX;

/**
 * ExecutorDefinition
 */
@Injectable
@Extension(value = "ExecutorDefinition", order = PluginDefinition.ORDER_TRANSMISSION)
@ConditionalOnProperty(value = GovernanceConfig.CONFIG_TRANSMISSION_ENABLED, matchIfMissing = true)
@ConditionalOnProperty(value = GovernanceConfig.CONFIG_TRANSMISSION_THREADPOOL_ENABLED)
public class ExecutorDefinition extends PluginDefinitionAdapter {
    private static final String TYPE_EXECUTOR = "java.util.concurrent.Executor";

    private static final String METHOD_EXECUTE = "execute";

    private static final String METHOD_SUBMIT = "submit";
    private static final String[] METHODS = {METHOD_EXECUTE, METHOD_SUBMIT};

    @Inject
    @InjectLoader(ResourcerType.CORE_IMPL)
    private List<Camera> handlers;

    @Config(CONFIG_THREAD_PREFIX)
    private ThreadConfig threadConfig;

    public ExecutorDefinition() {
        this.matcher = () -> MatcherBuilder.isImplement(TYPE_EXECUTOR).
                and(MatcherBuilder.not(MatcherBuilder.in(threadConfig.getExcludes())));
        this.interceptors = new InterceptorDefinition[]{
                new InterceptorDefinitionAdapter(MatcherBuilder.in(METHODS).and(MatcherBuilder.isPublic()),
                        () -> new ExecutorInterceptor(handlers))};
    }
}