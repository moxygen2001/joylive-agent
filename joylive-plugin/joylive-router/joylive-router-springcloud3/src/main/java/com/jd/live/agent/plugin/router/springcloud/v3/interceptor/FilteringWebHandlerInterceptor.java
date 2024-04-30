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
package com.jd.live.agent.plugin.router.springcloud.v3.interceptor;

import com.jd.live.agent.bootstrap.bytekit.context.ExecutableContext;
import com.jd.live.agent.bootstrap.bytekit.context.MethodContext;
import com.jd.live.agent.bootstrap.exception.RejectException;
import com.jd.live.agent.core.instance.Application;
import com.jd.live.agent.governance.interceptor.AbstractInterceptor.AbstractHttpInboundInterceptor;
import com.jd.live.agent.governance.invoke.InboundInvocation.GatewayInboundInvocation;
import com.jd.live.agent.governance.invoke.InboundInvocation.HttpInboundInvocation;
import com.jd.live.agent.governance.invoke.InvocationContext;
import com.jd.live.agent.governance.invoke.filter.InboundFilter;
import com.jd.live.agent.plugin.router.springcloud.v3.request.ReactiveInboundRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.handler.FilteringWebHandler;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * FilteringWebHandlerInterceptor
 *
 * @author Zhiguo.Chen
 * @since 1.0.0
 */
public class FilteringWebHandlerInterceptor extends AbstractHttpInboundInterceptor<ReactiveInboundRequest> {

    private final Application application;

    public FilteringWebHandlerInterceptor(InvocationContext context, List<InboundFilter> filters, Application application) {
        super(context, filters);
        this.application = application;
    }

    /**
     * Enhanced logic before method execution
     * <p>
     *
     * @param ctx ExecutableContext
     * @see FilteringWebHandler#handle(ServerWebExchange)
     */
    @Override
    public void onEnter(ExecutableContext ctx) {
        MethodContext mc = (MethodContext) ctx;
        ServerWebExchange exchange = (ServerWebExchange) mc.getArguments()[0];
        try {
            process(new ReactiveInboundRequest(exchange.getRequest()));
        } catch (RejectException e) {
            mc.setResult(Mono.error(new ResponseStatusException(
                    HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, e.getMessage(), e)));
            mc.setSkip(true);
        }
    }

    @Override
    protected HttpInboundInvocation<ReactiveInboundRequest> createInlet(ReactiveInboundRequest request) {
        return application.getService().isGateway() ? new GatewayInboundInvocation<>(request, context) : super.createInlet(request);
    }
}
