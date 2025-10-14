package dev.jsinco.malts.registry;

public interface RegistryCrafter {
    interface Extension<E> extends RegistryCrafter {
        <T extends E> T craft(Class<?> clazz);
    }
    interface NoExtension extends RegistryCrafter {
        <T> T craft(Class<T> clazz);
    }
}
