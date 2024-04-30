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
package com.jd.live.agent.core.inject.jbind.converter.supplier;

import com.jd.live.agent.core.extension.annotation.Extension;
import com.jd.live.agent.core.inject.jbind.Conversion;
import com.jd.live.agent.core.inject.jbind.ConversionType;
import com.jd.live.agent.core.inject.jbind.Converter;
import com.jd.live.agent.core.inject.jbind.ConverterSupplier;

@Extension(value = "ToStringSupplier", order = ConverterSupplier.TO_STRING_ORDER)
public class ToStringSupplier implements ConverterSupplier {
    @Override
    public Converter getConverter(ConversionType type) {
        return String.class == type.getTargetType().getRawType() ? new ToStringConverter() : null;
    }

    public static class ToStringConverter implements Converter {
        @Override
        public Object convert(Conversion conversion) {
            return conversion.getSource().toString();
        }
    }
}
