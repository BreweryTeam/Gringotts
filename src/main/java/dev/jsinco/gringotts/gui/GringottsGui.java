package dev.jsinco.gringotts.gui;

import dev.jsinco.gringotts.obj.GringottsInventory;
import dev.jsinco.gringotts.gui.item.AbstractGuiItem;
import dev.jsinco.gringotts.gui.item.AutoRegisterGuiItems;
import dev.jsinco.gringotts.gui.item.IgnoreAutoRegister;
import dev.jsinco.gringotts.utility.Text;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class GringottsGui implements GringottsInventory {

    @Getter
    protected final Inventory inventory;
    protected final List<AbstractGuiItem> guiItems;
    boolean constructedViaFactory = false;


    public GringottsGui(String title, int size) {
        this.inventory = Bukkit.createInventory(this, size, Text.mm(title));
        this.guiItems = new ArrayList<>();
    }

    public abstract void onInventoryClick(InventoryClickEvent event);
    public abstract void open(Player player);



    public void addGuiItem(AbstractGuiItem item) {
        int index = item.index() != null ? item.index() : -1;
        addGuiItem(item, index);
    }

    public void addGuiItem(AbstractGuiItem item, int index) {
        if (index >= 0 && index < inventory.getSize()) {
            inventory.setItem(index, item.guiItemStack());
        }
        guiItems.add(item);
    }

    public void onPreInventoryClick(InventoryClickEvent event) {
        validate();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem != null) {
            for (AbstractGuiItem guiItem : guiItems) {
                guiItem.onClick(event, clickedItem);
            }
        }

        // Let children handle rest
        onInventoryClick(event);
    }

    protected void autoRegister(Class<?> forClass) {
        for (Field field : forClass.getDeclaredFields()) {
            field.setAccessible(true);
            if (!AbstractGuiItem.class.isAssignableFrom(field.getType()) || field.isAnnotationPresent(IgnoreAutoRegister.class)) continue;

            try {
                AbstractGuiItem guiItem = (AbstractGuiItem) field.get(this);
                if (guiItem != null) {
                    addGuiItem(guiItem);
                } else {
                    Text.debug("GuiItem field '" + field.getName() + "' is null in " + forClass.getSimpleName());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void autoRegister(boolean walk) {
        Class<?> currentClass = this.getClass();
        if (walk) {
            while (currentClass != null && currentClass != Object.class) {
                this.autoRegister(currentClass);
                currentClass = currentClass.getSuperclass();
            }
        } else {
            this.autoRegister(currentClass);
        }
    }

    private void validate() {
        if (!constructedViaFactory) {
            throw new IllegalStateException("GringottsGui must be constructed via a factory method.");
        }
    }



    // TODO: Remove
    public static <T extends GringottsGui> T factory(Supplier<T> supplier) {
        T instance = supplier.get();
        instance.constructedViaFactory = true;

        if (instance.getClass().isAnnotationPresent(AutoRegisterGuiItems.class)) {
            boolean walk = instance.getClass().getAnnotation(AutoRegisterGuiItems.class).walk();
            instance.autoRegister(walk);
        }

        return instance;
    }

}
