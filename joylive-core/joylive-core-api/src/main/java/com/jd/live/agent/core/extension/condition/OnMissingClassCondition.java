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
package com.jd.live.agent.core.extension.condition;

import com.jd.live.agent.core.extension.annotation.ConditionalOnMissingClass;

import java.net.URL;

/**
 * on missing class condition
 */
public class OnMissingClassCondition extends OnCondition {

    public static final OnMissingClassCondition CONDITION = new OnMissingClassCondition();

    @Override
    public boolean match(final ConditionContext context) {
        ConditionalOnMissingClass onClass = (ConditionalOnMissingClass) context.getAnnotation();
        String name = onClass.value();
        if (!isEmpty(name)) {
            name = name.replace(".", "/") + ".class";
            URL url = context.getClassLoader().getResource(name);
            return url == null;
        }
        return true;
    }
}
