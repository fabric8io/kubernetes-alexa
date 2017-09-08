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

import com.amazon.speech.slu.Intent;
import com.amazon.speech.speechlet.Session;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.base.BaseOperation;
import io.fabric8.kubernetes.client.utils.Utils;
import io.fabric8.openshift.client.OpenShiftClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.getLevenshteinDistance;

public class IntentContext<T extends BaseOperation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IntentContext.class);

    public static final Function<IntentContext, IntentContext> NAME_FILTER = o -> {
        String name = selectName(o.operation, getVariable(Variable.Name, o));
        if (Utils.isNotNullOrEmpty(name)) {
            return new IntentContext(o.intent, o.session, (BaseOperation) o.operation.withName(name), o.client);
        }
        return o;
    };

    public static final Function<IntentContext, IntentContext> NAMESPACE_FILTER = o -> {
        String namespace = selectNamespace(o.client, getVariable(Variable.Namespace, o));
        if (Utils.isNotNullOrEmpty(namespace)) {
            return new IntentContext(o.intent, o.session, (BaseOperation) o.operation.inNamespace(namespace), o.client);
        }
        return o;
    };

    public static final Function<IntentContext, IntentContext> LABEL_FILTER = o -> {
        String labels = (String) o.session.getAttribute(Variable.Labels.name());
        BaseOperation result = o.operation;
        if (Utils.isNotNullOrEmpty(labels)) {
            for (String label : StringUtils.split(labels, ",")) {
                result = (BaseOperation) result.withLabel(label);
            }
        }
        return new IntentContext(o.intent, o.session, result, o.client);
    };

    private static final String getVariable(Variable attr, IntentContext operation) {
        return getVariable(attr.name(), operation.intent, operation.session, null);
    }


    private static final String getVariable(Variable attr, IntentContext operation, String fallbackValue) {
        return getVariable(attr.name(), operation.intent, operation.session, fallbackValue);
    }

    private static final String getVariable(String key, Intent intent, Session session, String fallbackValue) {
        LOGGER.debug("Getting variable: [" + key + "], from slots:["
                + join(intent.getSlots().keySet(), " ")
                + "] and session: ["
                + join(session.getAttributes().keySet(), " ") + "].");

        String result = null;
        if (intent.getSlots().containsKey(key)) {
            result = intent.getSlot(key).getValue();
        } else if (session.getAttributes().containsKey(key)) {
            result = String.valueOf(session.getAttribute(key));
        }

        if (Utils.isNullOrEmpty(result)) {
            result = fallbackValue;
        }
        return result;
    }

    /**
     * Choose a namespace that sounds like the specified namespace.
     * @param client        The client instance to use to query namespaces.
     * @param namespace     The specified namespace.
     * @return              The closest namespace if resemblance is over 80%, else null.
     */
     public static final String selectNamespace(KubernetesClient client, String namespace) {
         if (client.isAdaptable(OpenShiftClient.class)) {
             return selectName(client.adapt(OpenShiftClient.class).projects()
                     .list()
                     .getItems()
                     .stream()
                     .map(n -> n.getMetadata().getName()), namespace);
         } else {
             return selectName(client.namespaces()
                     .list()
                     .getItems()
                     .stream()
                     .map(n -> n.getMetadata().getName()), namespace);
         }
    }


    /**
     * Choose a name that sounds like the specified namespace.
     * @param operation     The operation to use for listing names.
     * @param name          The specified names.
     * @return              The closest name if resemblance is over 80%, else null.
     */
    public static final String selectName(BaseOperation<?,? extends KubernetesResourceList<?>, ?, ?> operation, String name) {
        return selectName(operation.list().getItems().stream().map(n -> n.getMetadata().getName()), name);
    }


    /**
     * Choose a name that sounds like the specified namespace.
     * @param stream        The stream of names to use.
     * @param name          The specified names.
     * @return              The closest name if resemblance is over 80%, else null.
     */
    static final String selectName(Stream<String> stream, String name) {
        if (Utils.isNullOrEmpty(name)) {
            return null;
        }

        List<String> names = stream
                //Filter out all namespaces that differ by more than 80% to what Alexa understood.
                //TODO: This will only work for namespaces that sounds like actual english words. So, we need something smarter...
                .filter(n -> getLevenshteinDistance(n, name, Math.max(1, (int) (n.length() * 0.25)) ) >= 0)
                .sorted(Comparator.comparingInt(s -> getLevenshteinDistance(s, name)))
                .collect(Collectors.toList());


        if (names.isEmpty()) {
            return null;
        } else {
            return names.get(0);
        }
    }

    private final Intent intent;
    private final Session session;
    private final T operation;
    private final KubernetesClient client;

    public IntentContext(Intent intent, Session session, T operation, KubernetesClient client) {
        this.intent = intent;
        this.session = session;
        this.operation = operation;
        this.client = client;
    }

    public Session getSession() {
        return session;
    }

    public T getOperation() {
        return operation;
    }

    /**
     * Finds the value of a variable from the context.
     * @param key   The target variable.
     * @return      The slot value if exists, else the session attribute.
     */
    public String getVariable(Variable key) {
        return IntentContext.getVariable(key, this);
    }

    /**
     * Finds the value of a variable from the context.
     * @param key           The target variable.
     * @param fallbackValue A value to fallback if no slot or session attribute is found.
     * @return              The slot value if exists, else the session attribute if exists, else the fallback value.
     */
    public String getVariable(Variable key, String fallbackValue) {
        return IntentContext.getVariable(key, this, fallbackValue);
    }

    public T asOperation(Function<IntentContext, IntentContext>... functions) {
        Function<IntentContext, IntentContext> next = s -> s;
        for (Function<IntentContext, IntentContext> f : functions) {
            next = next.andThen(f);
        }

        return (T) next.apply(new IntentContext<T>(intent, session, operation, client)).getOperation();
    }

    public T asOperation(Iterable<Function<IntentContext, IntentContext>> functions) {
        Function<IntentContext, IntentContext> next = s -> s;
        for (Function<IntentContext, IntentContext> f : functions) {
            next = next.andThen(f);
        }

        return (T) next.apply(new IntentContext<T>(intent, session, operation, client)).getOperation();
    }

    public static class Builder<T extends BaseOperation> {
        private Intent intent;
        private Session session;
        private T operation;
        private KubernetesClient client;

        public Builder() {
        }

        public Builder(IntentContext<T> existing) {
            this.intent = existing.intent;
            this.session = existing.session;
            this.operation = existing.operation;
            this.client = existing.client;
        }

        public Builder<T> withIntent(Intent intent) {
            this.intent = intent;
            return (Builder<T>) this;
        }

        public Builder<T> withSession(Session session) {
            this.session = session;
            return (Builder<T>) this;
        }

        public Builder<T> withOperation(T operation) {
            this.operation = operation;
            return (Builder<T>) this;
        }

        public Builder<T> withClient(KubernetesClient client) {
            this.client = client;
            return (Builder<T>) this;
        }

        public IntentContext<T> build() {
            return new IntentContext<>(intent, session, operation, client);
        }
    }
}
