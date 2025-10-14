package dev.jsinco.malts.integration;

import dev.jsinco.malts.registry.RegistryCrafter;
import dev.jsinco.malts.utility.Text;

public class IntegrationCrafter implements RegistryCrafter.Extension<Integration> {
    @Override
    public <T extends Integration> T craft(Class<?> clazz) {
        try {
            T instance = (T) clazz.getDeclaredConstructor().newInstance();
            if (instance.canRegister()) {
                instance.register();
                Text.log("Registered integration for: " + instance.name());
                return instance;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
