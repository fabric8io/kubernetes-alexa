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
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletResponseBuilder;
import io.fabric8.kubernetes.alexa.request.RequestHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestDispatcher<C> implements Speechlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestDispatcher.class);
    private static final String LAUNCH_HANDLER = "Launch";

    private final C context;

    public RequestDispatcher(C context) {
        this.context = context;
    }



    public void onSessionStarted(SessionStartedRequest request, Session session) throws SpeechletException {
        LOGGER.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),  session.getSessionId());
    }

    public SpeechletResponse onLaunch(LaunchRequest request, Session session) throws SpeechletException {
        LOGGER.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),  session.getSessionId());

        RequestHandlerFactory factory = GetRequestHandlerFactory.FUNCTION.apply(LAUNCH_HANDLER);

        if (factory != null) {
            return factory.create(context).onRequest(request, session);
        } else {
            throw new SpeechletException("Could not find RequestHandler for: "+LOGGER);
        }

    }

    public SpeechletResponse onIntent(IntentRequest request, Session session) throws SpeechletException {
        LOGGER.info("onRequest requestId={}, sessionId={}", request.getRequestId(), session.getSessionId());
        // Get intent from the request object.
        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        RequestHandlerFactory factory = GetRequestHandlerFactory.FUNCTION.apply(intentName);

        if (factory != null) {
            return factory.create(context).onRequest(request, session);
        } else {
            return new SpeechletResponseBuilder()
                    .withNewPlainTextOutputSpeech()
                        .withText("I don't know how to do that.")
                    .endPlainTextOutputSpeech()
                    .build();
        }
    }

    public void onSessionEnded(SessionEndedRequest request, Session session) throws SpeechletException {
        LOGGER.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),  session.getSessionId());
    }
}
