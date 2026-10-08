package com.jobcommandcenter.ai.infrastructure.provider;

import com.jobcommandcenter.ai.domain.AIProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Provider factory that manages registered AIProvider implementations
 * and resolves the active provider based on configuration.
 */
@Component
public class AIProviderFactory {

    private final Map<String, AIProvider> providers;
    private final String activeProviderName;

    public AIProviderFactory(List<AIProvider> providerList,
                             @Value("${app.ai.provider:MOCK}") String activeProviderName) {
        this.providers = providerList.stream()
            .collect(Collectors.toMap(
                p -> p.getProviderName().toUpperCase(),
                Function.identity(),
                (existing, replacement) -> existing
            ));
        this.activeProviderName = activeProviderName.toUpperCase();
    }

    public AIProvider getActiveProvider() {
        return getProvider(activeProviderName)
            .orElseGet(() -> getProvider("MOCK")
                .orElseThrow(() -> new IllegalStateException("No valid AI provider found")));
    }

    public Optional<AIProvider> getProvider(String providerName) {
        if (providerName == null) return Optional.empty();
        return Optional.ofNullable(providers.get(providerName.toUpperCase()));
    }

    public List<String> getAvailableProviders() {
        return List.copyOf(providers.keySet());
    }
}
