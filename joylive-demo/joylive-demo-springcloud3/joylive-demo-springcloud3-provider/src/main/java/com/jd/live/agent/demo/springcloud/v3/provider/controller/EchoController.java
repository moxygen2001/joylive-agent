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
package com.jd.live.agent.demo.springcloud.v3.provider.controller;

import com.jd.live.agent.demo.util.EchoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
public class EchoController {

    private final CountDownLatch latch = new CountDownLatch(1);

    @GetMapping("/echo/{str}")
    public String echo(@PathVariable String str, HttpServletRequest request) {
        try {
            latch.await(2000 + ThreadLocalRandom.current().nextInt(1000), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignore) {
        }
        return new EchoResponse("spring-provider", "header", request::getHeader, str).toString();
    }
}
