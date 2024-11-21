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
package com.jd.live.agent.implement.service.policy.apollo.config;

import lombok.Getter;
import lombok.Setter;

import static com.jd.live.agent.core.config.SyncConfig.CONFIG_PREFIX;

/**
 * ApolloConfig is responsible for Apollo settings.
 */

@Getter
@Setter
public class ApolloConfig {

    /**
     * apollo app.id
     */
    private String appId;

    /**
     * apollo address type
     */
    private AddressType addressType = AddressType.META_SERVICE;

    /**
     * apollo environment
     */
    private String environment;

    private String serviceNamespaceTemplate = CONFIG_PREFIX + "-service-${name}";

    private String laneSpacesNamespace = CONFIG_PREFIX + "-laneSpaces";

    private String laneSpaceNamespaceTemplate = CONFIG_PREFIX + "-laneSpace-${id}";

    private String liveSpacesNamespace = CONFIG_PREFIX + "-liveSpaces";

    private String liveSpaceNamespaceTemplate = CONFIG_PREFIX + "-liveSpace-${id}";

    private String liveServiceNamespaceTemplate = CONFIG_PREFIX + "-liveService-${name}";

}
