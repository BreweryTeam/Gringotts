package dev.jsinco.malts.integration;

import dev.jsinco.malts.registry.RegistryCrafter;

public class IntegrationCrafter implements RegistryCrafter.Extension<Integration> {
    @Override
    public <T extends Integration> T craft(Class<?> clazz) {
        try {
            T instance = (T) clazz.getDeclaredConstructor().newInstance();
            if (instance.canRegister()) {
                instance.register();
                return instance;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return null;
    }
}
