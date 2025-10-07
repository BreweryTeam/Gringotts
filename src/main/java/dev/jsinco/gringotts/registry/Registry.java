package dev.jsinco.gringotts.registry;

import dev.jsinco.gringotts.commands.interfaces.SubCommand;
import dev.jsinco.gringotts.commands.subcommands.ImportCommand;
import dev.jsinco.gringotts.commands.subcommands.MaxCommand;
import dev.jsinco.gringotts.commands.subcommands.VaultOtherCommand;
import dev.jsinco.gringotts.commands.subcommands.VaultsCommand;
import dev.jsinco.gringotts.commands.subcommands.WarehouseAdminCommand;
import dev.jsinco.gringotts.commands.subcommands.WarehouseCommand;
import dev.jsinco.gringotts.configuration.ConfigManager;
import dev.jsinco.gringotts.configuration.OkaeriFile;
import dev.jsinco.gringotts.configuration.files.Config;
import dev.jsinco.gringotts.configuration.files.GuiConfig;
import dev.jsinco.gringotts.configuration.files.Lang;
import dev.jsinco.gringotts.importers.Importer;
import dev.jsinco.gringotts.importers.PlayerVaultsImporter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Registry<T extends RegistryItem> {

    public static final Registry<SubCommand> SUB_COMMANDS = fromClasses(VaultsCommand.class, WarehouseCommand.class, ImportCommand.class, VaultOtherCommand.class, WarehouseAdminCommand.class, MaxCommand.class);
    public static final Registry<Importer> IMPORTERS = fromClasses(PlayerVaultsImporter.class);
    public static final Registry<OkaeriFile> CONFIGS = fromClassesWithCrafter(new ConfigManager.ConfigCrafter(), Config.class, GuiConfig.class, Lang.class);

    private final Map<String, T> map;

    public Registry(Collection<T> values) {
        this.map = new HashMap<>();
        values.forEach(item -> {
            for (String name : item.names()) {
                map.put(name, item);
            }
        });
    }

    public T get(String identifier) {
        return map.get(identifier);
    }

    public T get(Class<T> clazz) {
        return map.values().stream()
                .filter(it -> it.getClass().equals(clazz))
                .findFirst()
                .orElse(null);
    }

    public Collection<T> values() {
        return map.values();
    }

    public Collection<String> keySet() {
        return map.keySet();
    }

    //@SuppressWarnings("unchecked")
    @SafeVarargs
    public static <E extends RegistryItem> Registry<E> fromClassesWithCrafter(RegistryCrafter crafter, Class<? extends E>... classes) {
        List<E> eClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            if (crafter instanceof RegistryCrafter.Extension<?> crafter1) {
                eClasses.add((E) crafter1.craft(clazz));
            } else if (crafter instanceof RegistryCrafter.NoExtension crafter2) {
                eClasses.add((E) crafter2.craft(clazz));
            } else {
                throw new IllegalArgumentException("Unknown crafter type");
            }
        }
        return new Registry<>(eClasses);
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
