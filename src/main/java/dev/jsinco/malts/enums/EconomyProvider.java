package dev.jsinco.malts.enums;

import dev.jsinco.malts.integration.EconomyIntegration;
import dev.jsinco.malts.integration.Integration;
import dev.jsinco.malts.integration.external.VaultIntegration;
import dev.jsinco.malts.registry.Registry;
import org.jetbrains.annotations.Nullable;

public enum EconomyProvider {

    NONE(registry -> null),
    VAULT(registry -> registry.get(VaultIntegration.class));

    private final Provider provider;

    EconomyProvider(Provider provider) {
        this.provider = provider;
    }

    public @Nullable EconomyIntegration getIntegration() {
        if (provider == null) {
            return null;
        }
        return provider.getIntegration(Registry.INTEGRATIONS);
    }


    private interface Provider {
        @Nullable
        EconomyIntegration getIntegration(Registry<Integration> integrationRegistry);
    }
}
