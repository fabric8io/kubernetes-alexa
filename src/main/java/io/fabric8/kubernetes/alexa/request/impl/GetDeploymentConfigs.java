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
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigList;
import io.fabric8.openshift.client.OpenShiftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.join;

public class GetDeploymentConfigs extends BaseKubernetesIntentRequestHandler<DeploymentConfig, DeploymentConfigList> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetDeploymentConfigs.class);

    static final String INTENT_NAME = "GetDeploymentConfigs";

    public GetDeploymentConfigs(KubernetesClient kubernetesClient) {
        super(kubernetesClient);
    }

    @Override
    public String getType() {
        return INTENT_NAME;
    }

    @Override
    public SpeechletResponse onRequest(IntentRequest request, Session session) throws SpeechletException {
        if (!getKubernetesClient().isAdaptable(OpenShiftClient.class)) {
            return newFailureNotice("Your cluster is not Openshift!");
        }

        IntentContext<BaseOperation<DeploymentConfig, DeploymentConfigList, ?, ?>> ctx = createContext(request.getIntent(), session);
        String namespace = ctx.getVariable(Variable.Namespace, getKubernetesClient().getNamespace());
        LOGGER.info("Listing all deployment configs for namespace:" + namespace);

        try {
            List<String> deployments = list(ctx)
                    .getItems()
                    .stream()
                    .map(d -> d.getMetadata().getName()).collect(Collectors.toList());

            if (deployments.isEmpty()) {
                return newResponse("No deployment configs found.");
            } else {
                return newResponse("The available deployment configs are: " + join(deployments, ","));
            }
        } catch (KubernetesClientException e) {
            return newFailureNotice(e.getStatus().getMessage());
        }
    }


    @Override
    public BaseOperation<DeploymentConfig, DeploymentConfigList, ?, ?> newOperation() {
        return (BaseOperation<DeploymentConfig, DeploymentConfigList, ?, ?>) getKubernetesClient().adapt(OpenShiftClient.class).deploymentConfigs();
    }
}
