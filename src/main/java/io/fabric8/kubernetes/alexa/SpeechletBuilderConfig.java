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

import io.sundr.builder.annotations.ExternalBuildables;

@ExternalBuildables(builderPackage = "io.fabric8.kubernetes.api.builder", generateBuilderPackage = false,
        editableEnabled = false,
        value = {
                "com.amazon.speech.speechlet.SpeechletResponse",
                "com.amazon.speech.ui.OutputSpeech",
                "com.amazon.speech.ui.PlainTextOutputSpeech",
                "com.amazon.speech.ui.SsmlOutputSpeech",
                "com.amazon.speech.ui.Card",
                "com.amazon.speech.ui.SimpleCard",
                "com.amazon.speech.ui.LinkAccountCard",
                "com.amazon.speech.ui.Reprompt"
        })
public class SpeechletBuilderConfig {
}
