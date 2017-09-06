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

import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;

import java.util.HashSet;
import java.util.Set;


public class KubernetesRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final String ALEXA_SKILL_ID_ENV_VAR = "ALEXA_SKILL_ID";

    private static final KubernetesClient KUBERNETES_CLIENT = new DefaultKubernetesClient();
    private static final Set<String> SUPPORTED_APPLICATION_IDS;

    static {
        /*
         * This Id can be found on https://developer.amazon.com/edw/home.html#/ "Edit" the relevant
         * Alexa Skill and put the relevant Application Ids in this Set.
         */
        SUPPORTED_APPLICATION_IDS = new HashSet<>();
        SUPPORTED_APPLICATION_IDS.add(System.getenv(ALEXA_SKILL_ID_ENV_VAR));
    }

    public KubernetesRequestStreamHandler() {
        super(new RequestDispatcher(KUBERNETES_CLIENT), SUPPORTED_APPLICATION_IDS);
    }
}
