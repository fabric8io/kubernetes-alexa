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

import io.fabric8.kubernetes.api.model.Endpoints;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.EventList;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.LabelSelectorRequirement;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.PodListBuilder;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.extensions.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.openshift.api.model.DeploymentConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackUtils {

    public static List<Event> getFailedEvents(KubernetesClient client, Pod pod) {
        Map<String, String> fields = new HashMap<>();
        fields.put("involvedObject.uid", pod.getMetadata().getUid());
        fields.put("involvedObject.name", pod.getMetadata().getName());
        fields.put("involvedObject.namespace", pod.getMetadata().getNamespace());
        fields.put("reason", "Failed");

        EventList eventList = client.events().inNamespace(pod.getMetadata().getNamespace()).withFields(fields).list();
        return eventList.getItems();
    }

    public static List<Event> getFailedEvents(KubernetesClient client, Deployment deployment) {
        List<Event> result = new ArrayList<>();
        for (Pod pod : podsOf(client, deployment).getItems()) {
            result.addAll(getFailedEvents(client, pod));
        }
        return result;
    }

    public static List<Event> getFailedEvents(KubernetesClient client, DeploymentConfig deploymentConfig) {
        List<Event> result = new ArrayList<>();
        for (Pod pod : podsOf(client, deploymentConfig).getItems()) {
            result.addAll(getFailedEvents(client, pod));
        }
        return result;
    }

    /**
     * Returns the {@link PodList} that match the specified {@link Deployment}.
     *
     * @param deployment The {@link Deployment}
     */
    public static PodList findMatching(KubernetesClient client, Deployment deployment) {
        FilterWatchListDeletable<Pod, PodList, Boolean, Watch, Watcher<Pod>> podLister =
                client.pods().inNamespace(deployment.getMetadata().getNamespace());
        if (deployment.getSpec().getSelector().getMatchLabels() != null) {
            podLister.withLabels(deployment.getSpec().getSelector().getMatchLabels());
        }
        if (deployment.getSpec().getSelector().getMatchExpressions() != null) {
            for (LabelSelectorRequirement req : deployment.getSpec().getSelector().getMatchExpressions()) {
                switch (req.getOperator()) {
                    case "In":
                        podLister.withLabelIn(req.getKey(), req.getValues().toArray(new String[]{}));
                        break;
                    case "NotIn":
                        podLister.withLabelNotIn(req.getKey(), req.getValues().toArray(new String[]{}));
                        break;
                    case "DoesNotExist":
                        podLister.withoutLabel(req.getKey());
                        break;
                    case "Exists":
                        podLister.withLabel(req.getKey());
                        break;
                }
            }
        }
        return podLister.list();
    }


    /**
     * Finds the pod that correspond to the specified resource.
     *
     * @param resource The resource.
     * @return The podList with the matching pods.
     */
    public static <T extends HasMetadata> PodList podsOf(KubernetesClient client, T resource) {
        if (resource instanceof Pod) {
            return new PodListBuilder().withItems((Pod) resource).build();
        } else if (resource instanceof Endpoints) {
            return podsOf(client, client.services()
                    .inNamespace(resource.getMetadata().getNamespace())
                    .withName(resource.getMetadata().getName())
                    .get());
        } else if (resource instanceof Service) {
            return client.pods()
                    .inNamespace(resource.getMetadata().getNamespace())
                    .withLabels(((Service) resource).getSpec().getSelector())
                    .list();
        } else if (resource instanceof ReplicationController) {
            return client.pods()
                    .inNamespace(resource.getMetadata().getNamespace())
                    .withLabels(((ReplicationController) resource).getSpec().getSelector())
                    .list();
        } else if (resource instanceof Deployment) {
            return findMatching(client, (Deployment) resource);
        } else if (resource instanceof DeploymentConfig) {
            return client.pods().inNamespace(resource.getMetadata().getNamespace()).withLabel("deploymentconfig",
                    resource.getMetadata().getName()).list();
        } else {
            return new PodListBuilder().build();
        }
    }

    public static final String normalize(String str) {
        if (Utils.isNullOrEmpty(str)) {
            return str;
        }

        str = str.replaceAll("@sha256:[a-zA-Z0-9]*", "");
        str = str.replaceAll("details:\\ \\([^\\)]*\\)", "");
        str = str.replaceAll("\\([^\\)]*\\)", "");
        return str;
    }
}
