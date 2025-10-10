package dev.jsinco.gringotts.integration;

import dev.jsinco.gringotts.registry.RegistryCrafter;

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
