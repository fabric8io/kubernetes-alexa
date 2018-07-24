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

import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletResponseBuilder;
import io.fabric8.kubernetes.alexa.FeedbackUtils;
import io.fabric8.kubernetes.alexa.request.RequestHandler;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.internal.readiness.Readiness;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.client.OpenShiftClient;

import java.util.List;
import java.util.stream.Collectors;

public class Launch implements RequestHandler<LaunchRequest> {

    static final String TYPE = "Launch";

    private final KubernetesClient kubernetesClient;
    private final OpenShiftClient openshiftClient;
    private final Boolean isOpenshift;

    public Launch(KubernetesClient kubernetesClient) {
        this.kubernetesClient = kubernetesClient;
        this.isOpenshift = kubernetesClient.isAdaptable(OpenShiftClient.class);
        this.openshiftClient = this.isOpenshift ? kubernetesClient.adapt(OpenShiftClient.class) : null;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public SpeechletResponse onRequest(LaunchRequest request, Session session) throws SpeechletException {
        return getWelcomeResponse();
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String namespace = kubernetesClient.getNamespace();

        String speechText =
                "Welcome to "+getClusterType()+". You are currently using namespace " + namespace + ". " +
                (isOpenshift ? getDeploymentConfigReport() : getDeploymentReport());

        String repromptText =
                "Welcome to "+getClusterType()+". You are currently using namespace " + namespace + ".";

        return new SpeechletResponseBuilder()
                .withNewSimpleCard()
                    .withTitle("Kubernetes")
                    .withContent("Welcome to Kubernetes skill")
                .endSimpleCard()
                .withNewPlainTextOutputSpeech()
                    .withText(speechText)
                .endPlainTextOutputSpeech()
                .withNewReprompt()
                        .withNewPlainTextOutputSpeech()
                            .withText(repromptText)
                        .endPlainTextOutputSpeech()
                    .endReprompt()
                .withShouldEndSession(false)
                .build();
    }

    private String getClusterType() {
        return isOpenshift ? "Openshift" : "Kubernetes";
    }

    /**
     * Returns a simple report of the deployments.
     * @return  A string containing the text of the report.
     */
    private String getDeploymentConfigReport() {
        StringBuilder sb = new StringBuilder();
        List<DeploymentConfig> all = openshiftClient.deploymentConfigs().list().getItems();

        List<DeploymentConfig> pending = all.stream()
                .filter(d -> !Readiness.isDeploymentConfigReady(d))
                .collect(Collectors.toList());

        try {
            sb.append(createDeploymentReport(all.size(), all.size() - pending.size()));
            sb.append(createPendingReportForDeploymentConfig(pending));
        } catch (Throwable t) {
            //ignore
        }

        return sb.toString();
    }

    /**
     * Returns a simple report of the deployments.
     * @return  A string containing the text of the report.
     */
    private String getDeploymentReport() {
        StringBuilder sb = new StringBuilder();
        List<Deployment> all = kubernetesClient.extensions().deployments().list().getItems();

        List<Deployment> pending = all.stream()
                .filter(d -> !Readiness.isDeploymentReady(d))
                .collect(Collectors.toList());

        try {
            sb.append(createDeploymentReport(all.size(), all.size() - pending.size()));
            sb.append(createPendingReportForDeployment(pending));
        } catch (Throwable t) {
            //ignore
        }
        return sb.toString();
    }

    /**
     * Populates the deployment report based on the number of deployments running and ready.
     * @param total     The total number of deployments.
     * @param ready     The deployments in ready state.
     */
    private String createDeploymentReport(int total, int ready) {
        StringBuilder sb = new StringBuilder();
        sb.append("Your namespace has " + total + " deployments. ");

        if (ready == 0) {
            sb.append("None in desired state. ");
        } else if (ready == total){
            sb.append("All in desired state. ");
        } else {
            sb.append(ready + " in desired state and " + (total - ready) + " pending. ");
        }
        return sb.toString();
    }

    private String createPendingReportForDeployment(List<Deployment> pending) {
        if (pending == null || pending.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Failed deployments are: ");
        for (Deployment p : pending) {
            sb.append(p.getMetadata().getName()).append(" ");
            List<Event> events = FeedbackUtils.getFailedEvents(kubernetesClient, p);
            if (!events.isEmpty()) {
                sb.append(" , due to:" + FeedbackUtils.normalize(events.get(0).getMessage()));
            }
        }
        return sb.toString();
    }

    private String createPendingReportForDeploymentConfig(List<DeploymentConfig> pending) {
        if (pending == null || pending.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Failed deployment configs are: ");
        for (DeploymentConfig p : pending) {
            sb.append(p.getMetadata().getName()).append(" ");
            List<Event> events = FeedbackUtils.getFailedEvents(kubernetesClient, p);
            if (!events.isEmpty()) {
                sb.append(" , due to " + FeedbackUtils.normalize(events.get(0).getMessage())).append( ". ");
            }
        }
        return sb.toString();
    }
}
