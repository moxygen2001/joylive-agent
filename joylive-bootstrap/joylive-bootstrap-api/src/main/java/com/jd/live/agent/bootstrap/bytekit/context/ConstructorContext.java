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
package com.jd.live.agent.bootstrap.bytekit.context;

import lombok.Getter;

import java.lang.reflect.Constructor;

/**
 * ConstructorContext class extends the ExecutableContext class to provide a context
 * specific to a constructor invocation.
 *
 * @param <T> The type of the object being constructed.
 */
@Getter
public class ConstructorContext<T> extends ExecutableContext {

    /**
     * The Constructor object being invoked within this context.
     */
    private final Constructor<T> constructor;

    /**
     * Constructor for the ConstructorContext class.
     * Initializes the context with the given type, arguments, constructor, and description.
     *
     * @param type        The Class object representing the type of the object being constructed.
     * @param arguments   An array of Objects representing the arguments to be passed to the constructor.
     * @param constructor The Constructor object representing the constructor being invoked.
     * @param description A String providing a description of the constructor invocation context.
     */
    public ConstructorContext(Class<T> type, Object[] arguments, Constructor<T> constructor, String description) {
        super(type, arguments, description);
        this.constructor = constructor;
    }
}