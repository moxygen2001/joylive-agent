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
package com.jd.live.agent.plugin.registry.springcloud.v3.interceptor;

import com.jd.live.agent.bootstrap.bytekit.context.ExecutableContext;
import com.jd.live.agent.bootstrap.bytekit.context.MethodContext;
import com.jd.live.agent.core.plugin.definition.InterceptorAdaptor;
import com.jd.live.agent.governance.policy.PolicySupplier;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;

/**
 * DiscoveryClientGetInterceptor
 */
public class DiscoveryClientGetInterceptor extends InterceptorAdaptor {

    private final PolicySupplier policySupplier;

    public DiscoveryClientGetInterceptor(PolicySupplier policySupplier) {
        this.policySupplier = policySupplier;
    }

    @Override
    public void onEnter(ExecutableContext ctx) {
        MethodContext mc = (MethodContext) ctx;
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) mc.getTarget();
        if (!policySupplier.isDone(supplier.getServiceId())) {
            mc.setResult(Flux.error(HttpClientErrorException.create(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Governance policy is not synchronized.",
                    new HttpHeaders(),
                    new byte[0],
                    StandardCharsets.UTF_8)));
            mc.setSkip(true);
        }
    }
}
