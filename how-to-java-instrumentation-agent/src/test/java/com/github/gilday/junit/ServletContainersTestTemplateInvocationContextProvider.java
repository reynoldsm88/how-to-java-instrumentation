package com.github.gilday.junit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

/**
 * {@link TestTemplateInvocationContextProvider} which decorates {@link ServletContainersTest} tests with logic to
 * launch and destroy java servlet containers in docker containers for use in the tests. {@see
 * ServletContainersTest}
 */
public class ServletContainersTestTemplateInvocationContextProvider implements TestTemplateInvocationContextProvider {

    private final DockerClient docker = DockerClientBuilder.getInstance().build();

    @Override
    public boolean supportsTestTemplate(final ExtensionContext ctx) {
        final ServletContainersTest containers = ctx.getRequiredTestMethod().getAnnotation(ServletContainersTest.class);
        return containers != null;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(final ExtensionContext ctx) {
        final ServletContainersTest containers = ctx.getRequiredTestMethod().getAnnotation(ServletContainersTest.class);
        final List<Class<? extends ServletContainerExecutionMetadataProvider>> classes = Arrays.asList(containers.value());
        if (classes.isEmpty()) {
            throw new TestFrameworkException("Annotation ServletContainersTest must contain a set of ServletContainerExecutionMetadataProvider classes");
        }
        return classes.stream()
            .map(ServletContainersTestTemplateInvocationContextProvider::instantiateOrDie)
            .map(ServletContainerExecutionMetadataProvider::get)
            .map(metadata -> new ServletContainerTestTemplateInvocationContext(docker, metadata));

    }

    private static ServletContainerExecutionMetadataProvider instantiateOrDie(final Class<? extends ServletContainerExecutionMetadataProvider> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new TestFrameworkException("Unable to instantiate class " + clazz + ". Does it have a nullary ctor?", e);
        }
    }

    @RequiredArgsConstructor
    private static class ServletContainerTestTemplateInvocationContext implements TestTemplateInvocationContext {

        private final DockerClient docker;
        private final ServletContainerExecutionMetadata metadata;

        @Override
        public String getDisplayName(final int invocationIndex) {
            return metadata.name();
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return Collections.singletonList(
                new ServletContainersTestExtension(docker, metadata)
            );
        }
    }
}