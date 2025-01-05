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
package com.jd.live.agent.plugin.transmission.dubbo.v2_7.interceptor;

import com.jd.live.agent.bootstrap.bytekit.context.ExecutableContext;
import com.jd.live.agent.core.plugin.definition.InterceptorAdaptor;
import com.jd.live.agent.governance.context.RequestContext;
import com.jd.live.agent.governance.context.bag.Carrier;
import com.jd.live.agent.governance.context.bag.Propagation;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcInvocation;

import static com.jd.live.agent.governance.context.bag.live.LivePropagation.LIVE_PROPAGATION;
import static com.jd.live.agent.governance.request.header.HeaderParser.StringHeaderParser.reader;
import static com.jd.live.agent.governance.request.header.HeaderParser.StringHeaderParser.writer;
import static org.apache.dubbo.common.constants.RegistryConstants.REGISTRY_TYPE_KEY;
import static org.apache.dubbo.common.constants.RegistryConstants.SERVICE_REGISTRY_TYPE;

public class DubboConsumerInterceptor extends InterceptorAdaptor {

    private final Propagation propagation;

    public DubboConsumerInterceptor(Propagation propagation) {
        this.propagation = propagation;
    }

    @Override
    public void onEnter(ExecutableContext ctx) {
        RpcInvocation invocation = (RpcInvocation) ctx.getArguments()[0];
        Carrier carrier = RequestContext.getOrCreate();
        RpcContext context = RpcContext.getContext();
        // read from rpc context by live propagation
        LIVE_PROPAGATION.read(carrier, reader(context.getAttachments()));
        // write to invocation with live attachments in rpc context
        propagation.write(carrier, writer(invocation.getAttachments(), invocation::setAttachment));
        Invoker<?> invoker = invocation.getInvoker();
        if (invoker != null) {
            URL url = invoker.getUrl();
            if (SERVICE_REGISTRY_TYPE.equals(url.getParameter(REGISTRY_TYPE_KEY))) {
                invocation.setAttachmentIfAbsent(REGISTRY_TYPE_KEY, SERVICE_REGISTRY_TYPE);
            }
        }
    }
}
