/*
 * Copyright (C) 2017 original authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.fabric8.kubernetes.alexa;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class IntentContextTest {


    @Test
    public void shouldSelectEqualNames() {
        String name = IntentContext.selectName(Arrays.stream(new String[]{"iocanel"}), "iocanel");
        Assert.assertEquals("iocanel", name);
    }

    @Test
    public void shouldAcceptSlightlyDifferentNames() {
        String name = IntentContext.selectName(Arrays.stream(new String[]{"iocanel"}), "iokanel");
        Assert.assertEquals("iocanel", name);
    }

    @Test
    public void shouldNotAcceptALittleMoreDifferentNames() {
        String name = IntentContext.selectName(Arrays.stream(new String[]{"iocanel"}), "iokanet");
        Assert.assertNull(name);
    }


    @Test
    public void shouldSelectTheClosestAlternative() {
        String name = IntentContext.selectName(Arrays.stream(new String[]{"xbcdefghijk", "xxcdefghijk" }), "abcdefghijk");
        Assert.assertEquals("xbcdefghijk", name);
    }
}
