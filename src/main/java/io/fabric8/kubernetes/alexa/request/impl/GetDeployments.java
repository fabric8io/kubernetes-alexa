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
import io.fabric8.kubernetes.alexa.Variable;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.api.model.extensions.DeploymentList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;

public class GetDeployments extends BaseKubernetesIntentRequestHandler<Deployment, DeploymentList> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetDeployments.class);

    static final String INTENT_NAME = "GetDeployments";

    public GetDeployments(KubernetesClient kubernetesClient) {
        super(kubernetesClient);
    }

    @Override
    public String getType() {
        return INTENT_NAME;
    }

    @Override
    public SpeechletResponse onRequest(IntentRequest request, Session session) throws SpeechletException {
        IntentContext<BaseOperation<Deployment, DeploymentList, ?, ?>> ctx = createContext(request.getIntent(), session);
        String namespace = ctx.getVariable(Variable.Namespace, getKubernetesClient().getNamespace());
        LOGGER.info("Listing all deployments for namespace:" + namespace);

        try {
            List<String> deployments = list(ctx)
                    .getItems()
                    .stream()
                    .map(d -> d.getMetadata().getName()).collect(Collectors.toList());

            if (deployments.isEmpty()) {
                return newResponse("No deployments found.");
            } else {
                return newResponse("The available deployments are: " + join(deployments, ","));
            }
        } catch (KubernetesClientException e) {
            return newFailureNotice(e.getStatus().getMessage());
        }
    }


    @Override
    public BaseOperation<Deployment, DeploymentList, ?, ?> newOperation() {
        return (BaseOperation<Deployment, DeploymentList, ?, ?>) getKubernetesClient().extensions().deployments();
    }
}
