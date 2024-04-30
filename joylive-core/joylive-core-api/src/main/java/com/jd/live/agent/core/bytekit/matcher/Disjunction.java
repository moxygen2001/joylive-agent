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
package com.jd.live.agent.core.bytekit.matcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a disjunction of element matchers, where at least one of the provided matchers
 * must match for the condition to be considered satisfied.
 *
 * @param <T> The type of elements that this disjunction will attempt to match.
 * @since 1.0.0
 */
public class Disjunction<T> extends AbstractJunction<T> {

    /**
     * A list of matchers that are combined in this disjunction.
     */
    private final List<ElementMatcher<? super T>> matchers;

    @SuppressWarnings("unchecked")
    public Disjunction(ElementMatcher<? super T>... matcher) {
        this(matcher == null ? null : Arrays.asList(matcher));
    }

    @SuppressWarnings("unchecked")
    public Disjunction(List<ElementMatcher<? super T>> matchers) {
        this.matchers = new ArrayList<>(matchers == null ? 0 : matchers.size());
        if (matchers != null) {
            for (ElementMatcher<? super T> matcher : matchers) {
                if (matcher instanceof Disjunction<?>) {
                    this.matchers.addAll(((Disjunction<Object>) matcher).matchers);
                } else {
                    this.matchers.add(matcher);
                }
            }
        }
    }

    @Override
    public boolean match(T target) {
        for (ElementMatcher<? super T> matcher : matchers) {
            if (matcher.match(target)) {
                return true;
            }
        }
        return false;
    }
}
