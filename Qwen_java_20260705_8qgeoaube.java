package com.utilitymod.module;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import org.lwjgl.glfw.GLFW;

public abstract class Module {

    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private KeyBinding keyBinding;
    protected final MinecraftClient mc = MinecraftClient.getInstance();

    public enum Category {
        COMBAT, VISUAL, MISC
    }

    public Module(String name, String description, Category category, int defaultKey) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = false;
        this.keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.utilitymod.module." + name.toLowerCase().replaceAll(" ", ""),
                InputUtil.Type.KEYSYM,
                defaultKey,
                "category.utilitymod.modules"
        ));
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable();
        else onDisable();
    }

    public void pollKey() {
        if (keyBinding != null && keyBinding.wasPressed()) {
            toggle();
        }
    }

    public void tick() { }

    protected abstract void onEnable();
    protected abstract void onDisable();

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public boolean isEnabled() { return enabled; }
    public KeyBinding getKeyBinding() { return keyBinding; }
}