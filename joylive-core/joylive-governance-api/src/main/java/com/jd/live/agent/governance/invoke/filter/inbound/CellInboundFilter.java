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
package com.jd.live.agent.governance.invoke.filter.inbound;

import com.jd.live.agent.core.extension.annotation.ConditionalOnProperty;
import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.governance.config.GovernanceConfig;
import com.jd.live.agent.governance.context.RequestContext;
import com.jd.live.agent.governance.context.bag.Carrier;
import com.jd.live.agent.governance.invoke.CellAction;
import com.jd.live.agent.governance.invoke.CellAction.CellActionType;
import com.jd.live.agent.governance.invoke.InboundInvocation;
import com.jd.live.agent.governance.invoke.UnitAction;
import com.jd.live.agent.governance.invoke.filter.InboundFilter;
import com.jd.live.agent.governance.invoke.filter.InboundFilterChain;
import com.jd.live.agent.governance.invoke.metadata.LiveMetadata;
import com.jd.live.agent.governance.request.ServiceRequest.InboundRequest;

import static com.jd.live.agent.governance.invoke.Invocation.FAILOVER_CELL_NOT_ACCESSIBLE;

/**
 * CellInboundFilter
 *
 * @author Zhiguo.Chen
 * @since 1.0.0
 */
@Extension(value = "CellInboundFilter", order = InboundFilter.ORDER_INBOUND_LIVE_CELL)
@ConditionalOnProperty(value = GovernanceConfig.CONFIG_LIVE_ENABLED, matchIfMissing = true)
public class CellInboundFilter implements InboundFilter {

    @Override
    public <T extends InboundRequest> void filter(InboundInvocation<T> invocation, InboundFilterChain chain) {
        UnitAction unitAction = invocation.getUnitAction();
        if (unitAction.getType() == UnitAction.UnitActionType.FORWARD) {
            CellAction cellAction = cellAction(invocation);
            invocation.setCellAction(cellAction);
            switch (cellAction.getType()) {
                case FORWARD:
                    chain.filter(invocation);
                    break;
                case FAILOVER:
                    Carrier carrier = RequestContext.getOrCreate();
                    carrier.setAttribute(Carrier.ATTRIBUTE_FAILOVER_CELL, cellAction);
                    chain.filter(invocation);
            }
        } else {
            chain.filter(invocation);
        }
    }

    protected <T extends InboundRequest> CellAction cellAction(InboundInvocation<T> invocation) {
        LiveMetadata metadata = invocation.getLiveMetadata();
        if (metadata.getUnitRule() != null && !invocation.isAccessible(metadata.getCurrentCell())) {
            // TODO add cell route access mode
            return new CellAction(CellActionType.FAILOVER, invocation.getError(FAILOVER_CELL_NOT_ACCESSIBLE));
        } else {
            return new CellAction(CellActionType.FORWARD, null);
        }
    }

}
