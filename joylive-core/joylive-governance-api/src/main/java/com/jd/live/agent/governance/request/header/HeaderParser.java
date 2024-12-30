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
package com.jd.live.agent.governance.request.header;

import com.jd.live.agent.core.util.tag.Label;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * A class that implements the {@link HeaderReader} and {@link HeaderWriter} interfaces.
 * It is designed to parse and manage headers using a generic type.
 *
 * @param <T> The type of the values used in the header parsing and management.
 */
public class HeaderParser<T> implements HeaderReader, HeaderWriter {

    protected final Map<String, T> map;

    protected final Function<T, List<String>> converter;

    protected final BiConsumer<String, String> consumer;

    public HeaderParser(Map<String, T> map, Function<T, List<String>> converter, BiConsumer<String, String> consumer) {
        this.map = map;
        this.converter = converter;
        this.consumer = consumer;
    }

    @Override
    public Iterator<String> getHeaderNames() {
        return map == null ? null : map.keySet().iterator();
    }

    @Override
    public List<String> getHeaders(String key) {
        T value = map == null ? null : map.get(key);
        return value == null || converter == null ? null : converter.apply(value);
    }

    @Override
    public String getHeader(String key) {
        return HeaderReader.super.getHeader(key);
    }

    @Override
    public void setHeader(String key, String value) {
        if (consumer != null) {
            consumer.accept(key, value);
        }
    }

    /**
     * A class that implements both {@link HeaderReader} and {@link HeaderWriter} interfaces.
     * This class is designed to handle the reading and writing of multi-map headers.
     */
    public static class MultiHeaderParser extends HeaderParser<List<String>> {

        private static final Function<List<String>, List<String>> LIST_LIST_FUNCTION = value -> value;

        public MultiHeaderParser(Map<String, List<String>> map) {
            super(map, LIST_LIST_FUNCTION, null);
        }

        public MultiHeaderParser(Map<String, List<String>> map, BiConsumer<String, String> consumer) {
            super(map, LIST_LIST_FUNCTION, consumer);
        }

        public static HeaderReader reader(Map<String, List<String>> map) {
            return new MultiHeaderParser(map);
        }

        public static HeaderWriter writer(Map<String, List<String>> map, BiConsumer<String, String> consumer) {
            return new MultiHeaderParser(map, consumer);
        }
    }

    /**
     * Represents a header that uses a map of objects to store and retrieve header values.
     * It implements both {@link HeaderReader} and {@link HeaderWriter} interfaces.
     */
    public static class ObjectHeaderParser extends HeaderParser<Object> {

        private static final Function<Object, List<String>> OBJECT_LIST_FUNCTION = value -> Label.parseValue(value.toString());

        public ObjectHeaderParser(Map<String, Object> map) {
            super(map, OBJECT_LIST_FUNCTION, null);
        }

        public ObjectHeaderParser(Map<String, Object> map, BiConsumer<String, String> consumer) {
            super(map, OBJECT_LIST_FUNCTION, consumer);
        }

        public static HeaderReader reader(Map<String, Object> map) {
            return new ObjectHeaderParser(map);
        }

        public static HeaderWriter writer(Map<String, Object> map, BiConsumer<String, String> consumer) {
            return new ObjectHeaderParser(map, consumer);
        }
    }

    /**
     * A class that implements both {@link HeaderReader} and {@link HeaderWriter} interfaces.
     * This class is designed to handle the reading and writing of headers using a map of strings.
     */
    public static class StringHeaderParser extends HeaderParser<String> {

        private static final Function<String, List<String>> STRING_LIST_FUNCTION = Label::parseValue;

        public StringHeaderParser(Map<String, String> map) {
            super(map, STRING_LIST_FUNCTION, null);
        }

        public StringHeaderParser(Map<String, String> map, BiConsumer<String, String> consumer) {
            super(map, STRING_LIST_FUNCTION, consumer);
        }

        public static HeaderReader reader(Map<String, String> map) {
            return new StringHeaderParser(map);
        }

        public static HeaderWriter writer(Map<String, String> map, BiConsumer<String, String> consumer) {
            return new StringHeaderParser(map, consumer);
        }
    }
}
