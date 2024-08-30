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
package com.jd.live.agent.plugin.router.springcloud.v3.cluster;

import com.jd.live.agent.core.util.Futures;
import com.jd.live.agent.core.util.type.ClassDesc;
import com.jd.live.agent.core.util.type.ClassUtils;
import com.jd.live.agent.core.util.type.FieldDesc;
import com.jd.live.agent.core.util.type.FieldList;
import com.jd.live.agent.governance.policy.service.cluster.RetryPolicy;
import com.jd.live.agent.governance.response.Response;
import com.jd.live.agent.plugin.router.springcloud.v3.instance.SpringEndpoint;
import com.jd.live.agent.plugin.router.springcloud.v3.request.ReactiveClusterRequest;
import com.jd.live.agent.plugin.router.springcloud.v3.response.ReactiveClusterResponse;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancedExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerClientRequestTransformer;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.client.loadbalancer.reactive.RetryableLoadBalancerExchangeFilterFunction;
import org.springframework.core.NestedRuntimeException;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;

/**
 * Represents a client cluster that handles outbound requests and responses for services
 * within a microservices architecture, utilizing a reactive load balancer. This class
 * integrates with Spring's WebClient and load balancing infrastructure to dynamically
 * select service instances based on load balancing strategies and policies.
 *
 * <p>This class is designed to work with {@link ReactiveClusterRequest} and
 * {@link ReactiveClusterResponse}, facilitating the routing and invocation of requests
 * to services identified by {@link SpringEndpoint}s and handling exceptions with
 * {@link NestedRuntimeException}.
 *
 * <p>It supports retry mechanisms based on configurable exceptions and integrates
 * service instance selection with load balancing and retry policies.
 */
public class ReactiveCluster extends AbstractClientCluster<ReactiveClusterRequest, ReactiveClusterResponse> {

    private static final Set<String> RETRY_EXCEPTIONS = new HashSet<>(Arrays.asList(
            "java.io.IOException",
            "java.util.concurrent.TimeoutException",
            "org.springframework.cloud.client.loadbalancer.reactive.RetryableStatusCodeException"
    ));

    private static final String FIELD_LOAD_BALANCER_FACTORY = "loadBalancerFactory";

    private static final String FIELD_TRANSFORMERS = "transformers";

    private final LoadBalancedExchangeFilterFunction filterFunction;

    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

    private final List<LoadBalancerClientRequestTransformer> transformers;

    /**
     * Constructs a new ClientCluster with the specified {@link LoadBalancedExchangeFilterFunction}.
     * This constructor initializes the load balancer factory and transformers by reflecting
     * on the provided filterFunction's class fields.
     *
     * @param filterFunction The {@link LoadBalancedExchangeFilterFunction} used to filter exchange functions.
     */
    @SuppressWarnings("unchecked")
    public ReactiveCluster(LoadBalancedExchangeFilterFunction filterFunction) {
        this.filterFunction = filterFunction;
        ClassDesc describe = ClassUtils.describe(filterFunction.getClass());
        FieldList fieldList = describe.getFieldList();
        FieldDesc field = fieldList.getField(FIELD_LOAD_BALANCER_FACTORY);
        this.loadBalancerFactory = (ReactiveLoadBalancer.Factory<ServiceInstance>) (field == null ? null : field.get(filterFunction));
        field = fieldList.getField(FIELD_TRANSFORMERS);
        this.transformers = (List<LoadBalancerClientRequestTransformer>) (field == null ? null : field.get(filterFunction));
    }

    public ReactiveLoadBalancer.Factory<ServiceInstance> getLoadBalancerFactory() {
        return loadBalancerFactory;
    }

    @Override
    protected boolean isRetryable() {
        return filterFunction instanceof RetryableLoadBalancerExchangeFilterFunction;
    }

    @Override
    public CompletionStage<ReactiveClusterResponse> invoke(ReactiveClusterRequest request, SpringEndpoint endpoint) {
        try {
            ClientRequest newRequest = buildRequest(request, endpoint.getInstance());
            return request.getNext().exchange(newRequest).map(ReactiveClusterResponse::new).toFuture();
        } catch (Throwable e) {
            return Futures.future(e);
        }
    }

    @Override
    public ReactiveClusterResponse createResponse(Throwable throwable, ReactiveClusterRequest request, SpringEndpoint endpoint) {
        return new ReactiveClusterResponse(createException(throwable, request, endpoint));
    }

    @Override
    public boolean isRetryable(Response response) {
        return RetryPolicy.isRetry(RETRY_EXCEPTIONS, response.getThrowable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(ReactiveClusterResponse response, ReactiveClusterRequest request, SpringEndpoint endpoint) {
        LoadBalancerProperties properties = request.getProperties();
        boolean useRawStatusCodeInResponseData = properties != null && properties.isUseRawStatusCodeInResponseData();
        request.lifecycles(l -> l.onComplete(new CompletionContext<>(
                CompletionContext.Status.SUCCESS,
                request.getLbRequest(),
                endpoint.getResponse(),
                useRawStatusCodeInResponseData
                        ? new ResponseData(new RequestData(request.getRequest()), response.getResponse())
                        : new ResponseData(response.getResponse(), new RequestData(request.getRequest())))));
    }

    /**
     * Builds a new {@link ClientRequest} tailored for a specific {@link ServiceInstance}, incorporating sticky session
     * configurations and potential transformations.
     *
     * @param request         The original {@link ReactiveClusterRequest} containing the request to be sent and its associated
     *                        load balancer properties.
     * @param serviceInstance The {@link ServiceInstance} to which the request should be directed.
     * @return A new {@link ClientRequest} instance, modified to target the specified {@link ServiceInstance} and
     * potentially transformed by any configured {@link LoadBalancerClientRequestTransformer}s.
     */
    private ClientRequest buildRequest(ReactiveClusterRequest request, ServiceInstance serviceInstance) {
        LoadBalancerProperties properties = request.getProperties();
        LoadBalancerProperties.StickySession stickySession = properties == null ? null : properties.getStickySession();
        String instanceIdCookieName = stickySession == null ? null : stickySession.getInstanceIdCookieName();
        boolean addServiceInstanceCookie = stickySession != null && stickySession.isAddServiceInstanceCookie();
        ClientRequest clientRequest = request.getRequest();
        URI originalUrl = clientRequest.url();
        ClientRequest result = ClientRequest
                .create(clientRequest.method(), LoadBalancerUriTools.reconstructURI(serviceInstance, originalUrl))
                .headers(headers -> headers.addAll(clientRequest.headers()))
                .cookies(cookies -> {
                    cookies.addAll(clientRequest.cookies());
                    // todo how to use this sticky session
                    if (!(instanceIdCookieName == null || instanceIdCookieName.isEmpty()) && addServiceInstanceCookie) {
                        cookies.add(instanceIdCookieName, serviceInstance.getInstanceId());
                    }
                })
                .attributes(attributes -> attributes.putAll(clientRequest.attributes()))
                .body(clientRequest.body())
                .build();
        if (transformers != null) {
            for (LoadBalancerClientRequestTransformer transformer : transformers) {
                result = transformer.transformRequest(result, serviceInstance);
            }
        }
        return result;
    }
}
