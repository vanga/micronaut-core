/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.tracing.brave;

import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.scheduling.instrument.InvocationInstrumenter;
import io.micronaut.scheduling.instrument.ReactiveInvocationInstrumenterFactory;
import io.micronaut.tracing.instrument.util.TracingInvocationInstrumenterFactory;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * Tracing invocation instrument for Brave.
 *
 * @author Denis Stepanov
 * @since 1.3
 */
@Singleton
@Requires(beans = Tracing.class)
public class BraveTracingInvocationInstrumenter implements ReactiveInvocationInstrumenterFactory, TracingInvocationInstrumenterFactory {

    private final CurrentTraceContext currentTraceContext;

    /**
     * Create a tracing invocation instrumenter.
     *
     * @param tracing invocation tracer
     */
    public BraveTracingInvocationInstrumenter(Tracing tracing) {
        this.currentTraceContext = tracing.currentTraceContext();
    }

    @Override
    public Optional<InvocationInstrumenter> newReactiveInvocationInstrumenter() {
        return newTracingInvocationInstrumenter();
    }

    @Override
    public Optional<InvocationInstrumenter> newTracingInvocationInstrumenter() {
        return Optional.ofNullable(currentTraceContext.get()).map(invocationContext -> new InvocationInstrumenter() {

            CurrentTraceContext.Scope activeScope;

            @Override
            public void beforeInvocation() {
                activeScope = currentTraceContext.maybeScope(invocationContext);
            }

            @Override
            public void afterInvocation() {
                activeScope.close();
            }

        });
    }
}