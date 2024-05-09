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
package com.jd.live.agent.governance.invoke;

import com.jd.live.agent.governance.event.TrafficEvent.ComponentType;
import com.jd.live.agent.governance.event.TrafficEvent.Direction;
import com.jd.live.agent.governance.event.TrafficEvent.TrafficEventBuilder;
import com.jd.live.agent.governance.instance.Endpoint;
import com.jd.live.agent.governance.invoke.metadata.parser.LiveMetadataParser.OutboundLiveMetadataParser;
import com.jd.live.agent.governance.invoke.metadata.parser.LiveMetadataParser.RpcOutboundLiveMetadataParser;
import com.jd.live.agent.governance.invoke.metadata.parser.MetadataParser.LiveParser;
import com.jd.live.agent.governance.invoke.metadata.parser.MetadataParser.ServiceParser;
import com.jd.live.agent.governance.invoke.metadata.parser.ServiceMetadataParser.GatewayOutboundServiceMetadataParser;
import com.jd.live.agent.governance.invoke.metadata.parser.ServiceMetadataParser.OutboundServiceMetadataParser;
import com.jd.live.agent.governance.policy.live.Cell;
import com.jd.live.agent.governance.policy.live.Unit;
import com.jd.live.agent.governance.request.HttpRequest.HttpOutboundRequest;
import com.jd.live.agent.governance.request.RpcRequest.RpcOutboundRequest;
import com.jd.live.agent.governance.request.ServiceRequest.OutboundRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an abstract outbound invocation, extending the Invocation class
 * with outbound-specific properties and methods.
 *
 * @param <T> the type of outbound request associated with this invocation
 */
@Setter
@Getter
public abstract class OutboundInvocation<T extends OutboundRequest> extends Invocation<T> {

    /**
     * A list of endpoints that this outbound invocation targets.
     */
    private List<? extends Endpoint> instances;

    /**
     * The target route for this outbound invocation.
     */
    private RouteTarget routeTarget;

    /**
     * Constructs an OutboundInvocation with a request and invocation context.
     *
     * @param request the request associated with this invocation
     * @param context the invocation context
     */
    public OutboundInvocation(T request, InvocationContext context) {
        super(request, context);
    }

    /**
     * Constructs an OutboundInvocation with a request and a base invocation.
     *
     * @param request    the request associated with this invocation
     * @param invocation the base invocation from which to derive properties
     */
    public OutboundInvocation(T request, Invocation<?> invocation) {
        this.request = request;
        this.context = invocation.getContext();
        this.governancePolicy = invocation.governancePolicy;
        this.liveMetadata = invocation.getLiveMetadata();
        this.laneMetadata = invocation.getLaneMetadata();
        ServiceParser serviceParser = createServiceParser();
        this.serviceMetadata = serviceParser.configure(serviceParser.parse(), liveMetadata.getUnitRule());
    }

    @Override
    protected ServiceParser createServiceParser() {
        return new OutboundServiceMetadataParser(request, context.getGovernanceConfig().getServiceConfig(),
                context.getApplication(), governancePolicy);
    }

    @Override
    protected LiveParser createLiveParser() {
        return new OutboundLiveMetadataParser(request, context.getGovernanceConfig().getLiveConfig(),
                context.getApplication(), governancePolicy);
    }

    /**
     * Retrieves the endpoints targeted by this outbound invocation.
     *
     * @return a list of endpoints, or an empty list if no route target is set.
     */
    public List<? extends Endpoint> getEndpoints() {
        return routeTarget == null ? new ArrayList<>(0) : routeTarget.getEndpoints();
    }

    /**
     * Get the routing target. When the routing target is empty, the default routing target is returned.
     *
     * @return RouteTarget
     */
    public RouteTarget getRouteTarget() {
        if (null == routeTarget) {
            routeTarget = RouteTarget.forward(getInstances());
        }
        return routeTarget;
    }

    @Override
    protected TrafficEventBuilder configure(TrafficEventBuilder builder) {
        Unit targetUnit = routeTarget == null ? null : routeTarget.getUnit();
        Cell targetCell = routeTarget == null ? null : routeTarget.getCell();
        return super.configure(builder).componentType(ComponentType.SERVICE).direction(Direction.OUTBOUND).
                targetUnit(targetUnit == null ? null : targetUnit.getCode()).
                targetCell(targetCell == null ? null : targetCell.getCode());
    }

    /**
     * A specialized static inner class representing an RPC outbound invocation. This class
     * extends the OutboundInvocation to provide RPC-specific logic for handling outbound requests.
     *
     * @param <T> the type parameter of the RPC outbound request, which must extend RpcOutboundRequest
     */
    public static class RpcOutboundInvocation<T extends RpcOutboundRequest> extends OutboundInvocation<T> {

        public RpcOutboundInvocation(T request, InvocationContext context) {
            super(request, context);
        }

        public RpcOutboundInvocation(T request, Invocation<?> invocation) {
            super(request, invocation);
        }

        @Override
        protected LiveParser createLiveParser() {
            return new RpcOutboundLiveMetadataParser(request, context.getGovernanceConfig().getLiveConfig(),
                    context.getApplication(), governancePolicy, context.getVariableParsers());
        }
    }

    /**
     * A specialized static inner class representing an HTTP outbound invocation. This class
     * extends the OutboundInvocation to provide HTTP-specific handling for outbound requests.
     *
     * @param <T> the type parameter of the HTTP outbound request, which must extend HttpOutboundRequest
     */
    public static class HttpOutboundInvocation<T extends HttpOutboundRequest> extends OutboundInvocation<T> {

        public HttpOutboundInvocation(T request, InvocationContext context) {
            super(request, context);
        }

        public HttpOutboundInvocation(T request, Invocation<?> invocation) {
            super(request, invocation);
        }

    }

    /**
     * A specialized static inner class representing an HTTP outbound invocation that is specifically
     * designed for use in a gateway scenario. This class extends the HttpOutboundInvocation to provide
     * additional gateway-specific logic for handling outbound requests.
     *
     * @param <T> the type parameter of the HTTP outbound request, which must extend HttpOutboundRequest
     */
    public static class GatewayHttpOutboundInvocation<T extends HttpOutboundRequest> extends HttpOutboundInvocation<T> {

        public GatewayHttpOutboundInvocation(T request, InvocationContext context) {
            super(request, context);
        }

        public GatewayHttpOutboundInvocation(T request, Invocation<?> invocation) {
            super(request, invocation);
        }

        @Override
        protected ServiceParser createServiceParser() {
            return new GatewayOutboundServiceMetadataParser(request, context.getGovernanceConfig().getServiceConfig(),
                    context.getApplication(), governancePolicy);
        }

        @Override
        protected TrafficEventBuilder configure(TrafficEventBuilder builder) {
            return super.configure(builder).componentType(ComponentType.GATEWAY);
        }

    }

    /**
     * A specialized static inner class representing an RPC outbound invocation designed for use in a gateway scenario.
     * This class extends the RpcOutboundInvocation to provide gateway-specific logic for handling outbound RPC requests.
     *
     * @param <T> the type parameter of the RPC outbound request, which must extend RpcOutboundRequest
     */
    public static class GatewayRpcOutboundInvocation<T extends RpcOutboundRequest> extends RpcOutboundInvocation<T> {

        public GatewayRpcOutboundInvocation(T request, InvocationContext context) {
            super(request, context);
        }

        public GatewayRpcOutboundInvocation(T request, Invocation<?> invocation) {
            super(request, invocation);
        }

        @Override
        protected ServiceParser createServiceParser() {
            return new GatewayOutboundServiceMetadataParser(request, context.getGovernanceConfig().getServiceConfig(),
                    context.getApplication(), governancePolicy);
        }

        @Override
        protected TrafficEventBuilder configure(TrafficEventBuilder builder) {
            return super.configure(builder).componentType(ComponentType.GATEWAY);
        }

    }
}
