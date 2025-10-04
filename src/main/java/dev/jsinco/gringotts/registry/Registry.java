package dev.jsinco.gringotts.registry;

import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.commands.subcommands.ImportCommand;
import dev.jsinco.gringotts.commands.subcommands.VaultOtherCommand;
import dev.jsinco.gringotts.commands.subcommands.VaultsCommand;
import dev.jsinco.gringotts.commands.subcommands.WarehouseAdminCommand;
import dev.jsinco.gringotts.commands.subcommands.WarehouseCommand;
import dev.jsinco.gringotts.importers.Importer;
import dev.jsinco.gringotts.importers.PlayerVaultsImporter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Registry<T extends RegistryItem> {

    public static final Registry<SubCommand> SUB_COMMANDS = fromClasses(VaultsCommand.class, WarehouseCommand.class, ImportCommand.class, VaultOtherCommand.class, WarehouseAdminCommand.class);
    public static final Registry<Importer> IMPORTERS = fromClasses(PlayerVaultsImporter.class);

    private final Map<String, T> registry;

    public Registry(Collection<T> values) {
        this.registry = new HashMap<>();
        values.forEach(item -> {
            for (String name : item.names()) {
                registry.put(name, item);
            }
        });
    }

    public T get(String identifier) {
        return registry.get(identifier);
    }

    public Collection<T> values() {
        return registry.values();
    }

    public Collection<String> keySet() {
        return registry.keySet();
    }


    @SafeVarargs
    public static <E extends RegistryItem> Registry<E> fromClasses(Class<? extends E>... classes) {
        return ConstructableClassBuilder.builder().addClasses(classes).build();
    }

    public static <E extends RegistryItem> Registry<E> fromClasses(Collection<Class<?>> constructorParamTypes, Collection<Object> constructorParams, Collection<Class<? extends E>> classes) {
        return ConstructableClassBuilder.builder().addConstructorParameters(constructorParamTypes, constructorParams).addClasses(classes).build();
    }

    // TODO: Better class name
    public static class ConstructableClassBuilder {
        private final List<Class<?>> constructorClassTypes = new ArrayList<>();
        private final List<Object> constructorClassValues = new ArrayList<>();
        private final List<Class<?>> classes = new ArrayList<>();

        public static ConstructableClassBuilder builder() {
            return new ConstructableClassBuilder();
        }

        public ConstructableClassBuilder addConstructorParameter(Class<?> type, Object value) {
            constructorClassTypes.add(type);
            constructorClassValues.add(value);
            return this;
        }

        public ConstructableClassBuilder addConstructorParameters(Collection<Class<?>> types, Collection<Object> values) {
            constructorClassTypes.addAll(types);
            constructorClassValues.addAll(values);
            return this;
        }

        public ConstructableClassBuilder addClass(Class<?> clazz) {
            classes.add(clazz);
            return this;
        }

        public ConstructableClassBuilder addClasses(Class<?>... clazz) {
            classes.addAll(List.of(clazz));
            return this;
        }

        public <E> ConstructableClassBuilder addClasses(Collection<Class<? extends E>> clazz) {
            classes.addAll(clazz);
            return this;
        }


        public <T extends RegistryItem> Registry<T> build() {
            Class<?>[] constructorTypes = constructorClassTypes.toArray(new Class<?>[0]);
            Object[] constructorValues = constructorClassValues.toArray(new Object[0]);

            List<T> tClasses = new ArrayList<>();
            for (Class<?> clazz : classes) {
                if (!RegistryItem.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("Class " + clazz.getName() + " does not implement RegistryItem");
                }

                try {
                    tClasses.add((T) clazz.getConstructor(constructorTypes).newInstance(constructorValues));
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                         InvocationTargetException e) {
                    throw new RuntimeException("No constructor found for " + clazz.getName(), e);
                }
            }
            return new Registry<>(tClasses);
        }
    }
}
