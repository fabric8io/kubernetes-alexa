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

package io.fabric8.kubernetes.alexa.request;

import com.amazon.speech.speechlet.SpeechletRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletResponseBuilder;
import io.fabric8.kubernetes.api.builder.TypedVisitor;
import io.fabric8.kubernetes.client.utils.Utils;

import static org.apache.commons.lang3.StringUtils.join;
import static org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase;

public abstract class BaseRequestHandler<R extends SpeechletRequest> implements RequestHandler<R> {

    public SpeechletResponse newResponse(String responseText) {
        return new SpeechletResponseBuilder()
                .withNewSimpleCard()
                    .withTitle("Kubernetes")
                    .withContent(getAction())
                .endSimpleCard()
                .withNewPlainTextOutputSpeech()
                    .withText(responseText)
                .endPlainTextOutputSpeech()
                .withShouldEndSession(false)
                .build();
    }

    public SpeechletResponse newFailureNotice(String responseText) {
        String message = "Failed to " + getAction() + "!" +
                (Utils.isNotNullOrEmpty(responseText) ? responseText : "");

        return new SpeechletResponseBuilder()
                .withNewSimpleCard()
                .withTitle("Kubernetes")
                .withContent(getAction())
                .endSimpleCard()
                .withNewPlainTextOutputSpeech()
                .withText(message)
                .endPlainTextOutputSpeech()
                .withShouldEndSession(false)
                .build();
    }

    public SpeechletResponse newResponse(String responseText, String repromptText) {
        return new SpeechletResponseBuilder(newResponse(responseText))
                .accept(new TypedVisitor<SpeechletResponseBuilder>() {
                    @Override
                    public void visit(SpeechletResponseBuilder builder) {
                        builder.withNewReprompt()
                          .withNewPlainTextOutputSpeech()
                                .withText(repromptText)
                                .endPlainTextOutputSpeech()
                                .endReprompt();
                    }
                }).build();
    }

    private String getAction() {
        return join(splitByCharacterTypeCamelCase(getType()), " ");
    }
}
