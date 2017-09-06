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

import io.fabric8.kubernetes.alexa.request.RequestHandlerFactory;
import io.fabric8.kubernetes.client.KubernetesClient;

public class GetNamespacesFactory implements RequestHandlerFactory<GetNamespaces, KubernetesClient> {

    @Override
    public String getType() {
        return GetNamespaces.INTENT_NAME;
    }

    @Override
    public GetNamespaces create(KubernetesClient client) {
        return new GetNamespaces(client);
    }
}
