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

public interface RequestHandlerFactory<I extends RequestHandler, C> {

    /**
     * Returns the the type of the {@link RequestHandler} this factory supports.
     * @return  The type of the {@link RequestHandler}.
     */
    String getType();

    /**
     * Creates an {@link RequestHandler}.
     * @param context   The factory context.
     * @return          The created {@link RequestHandler}.
     */
    I create(C context);
}
