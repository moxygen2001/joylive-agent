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
package com.jd.live.agent.governance.invoke.matcher.system;

import com.jd.live.agent.core.Constants;
import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.governance.context.RequestContext;
import com.jd.live.agent.governance.context.bag.Cargo;
import com.jd.live.agent.governance.request.Request;

import java.util.List;

@Extension(value = "consumer")
public class ConsumerTagProvider implements SystemTagProvider {

    @Override
    public List<String> getValues(Request request, String key) {
        Cargo cargo = RequestContext.getCargo(Constants.LABEL_SERVICE_CONSUMER);
        return cargo == null ? null : cargo.getValues();
    }
}
