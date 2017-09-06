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

package io.fabric8.kubernetes.alexa.request.impl;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import io.fabric8.kubernetes.alexa.IntentContext;
import io.fabric8.kubernetes.alexa.request.BaseRequestHandler;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static io.fabric8.kubernetes.alexa.IntentContext.LABEL_FILTER;
import static io.fabric8.kubernetes.alexa.IntentContext.NAMESPACE_FILTER;
import static io.fabric8.kubernetes.alexa.IntentContext.NAME_FILTER;


public abstract class BaseKubernetesIntentRequestHandler<T extends KubernetesResource, L extends KubernetesResourceList> extends BaseRequestHandler<IntentRequest> {

    private final KubernetesClient kubernetesClient;

    protected BaseKubernetesIntentRequestHandler(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
    }

    public abstract BaseOperation<T, L, ?, ?> newOperation();

    public KubernetesClient getKubernetesClient() {
        return kubernetesClient;
    }

    public IntentContext<BaseOperation<T, L, ?, ?>> createContext(Intent intent, Session session) {
        return new IntentContext.Builder<BaseOperation<T, L, ?, ?>>()
                .withIntent(intent)
                .withSession(session)
                .withOperation(newOperation())
                .withClient(getKubernetesClient())
                .build();
    }

    public L list(IntentContext<BaseOperation<T, L, ?, ?>> ctx) {
        return ctx.asOperation(getListOperationFilters()).list();
    }

    public T get(IntentContext<BaseOperation<T, L, ?, ?>> ctx) {
        return ctx.asOperation(getGetOperationFilters()).get();
    }


    /**
     * @return The default set of filters to be applies to get operations.
     */
    public List<Function<IntentContext, IntentContext>> getGetOperationFilters() {
        return Arrays.asList(NAME_FILTER, NAMESPACE_FILTER);
    }

    /**
     * @return The default set of filters to be applies to list operations.
     */
    public List<Function<IntentContext, IntentContext>> getListOperationFilters() {
        return Arrays.asList(NAMESPACE_FILTER, LABEL_FILTER);
    }
}
