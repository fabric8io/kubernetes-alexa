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

import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import io.fabric8.kubernetes.alexa.IntentContext;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.fabric8.kubernetes.alexa.IntentContext.LABEL_FILTER;
import static io.fabric8.kubernetes.alexa.IntentContext.NAME_FILTER;
import static org.apache.commons.lang3.StringUtils.join;

public class GetNamespaces extends BaseKubernetesIntentRequestHandler<Namespace, NamespaceList> {


    private static final Logger LOGGER = LoggerFactory.getLogger(GetNamespaces.class);

    static final String INTENT_NAME = "GetNamespaces";

    public GetNamespaces(KubernetesClient kubernetesClient) {
        super(kubernetesClient);
    }

    @Override
    public String getType() {
        return INTENT_NAME;
    }

    @Override
    public SpeechletResponse onRequest(IntentRequest request, Session session) throws SpeechletException {
        IntentContext<BaseOperation<Namespace, NamespaceList, ?, ?>> ctx = createContext(request.getIntent(), session);
        LOGGER.info("Listing all namespaces.");

        try {
            List<String> namespaces = list(ctx)
                    .getItems()
                    .stream()
                    .map(d -> d.getMetadata().getName()).collect(Collectors.toList());

            if (namespaces.isEmpty()) {
                return newResponse("No namespaces found.");
            } else {
                return newResponse("The available namespaces are: " + join(namespaces, ","));
            }
        } catch (KubernetesClientException e) {
            return newFailureNotice(e.getStatus().getMessage());
        }
    }


    @Override
    public BaseOperation<Namespace, NamespaceList, ?, ?> newOperation() {
        return (BaseOperation<Namespace, NamespaceList, ?, ?>) getKubernetesClient().namespaces();
    }

    public List<Function<IntentContext, IntentContext>> getGetOperationFilters() {
        return Arrays.asList(NAME_FILTER);
    }

    public List<Function<IntentContext, IntentContext>> getListOperationFilters() {
        return Arrays.asList(LABEL_FILTER);
    }
}
