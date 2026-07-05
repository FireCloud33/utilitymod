package com.utilitymod;

import com.utilitymod.gui.ModuleScreen;
import com.utilitymod.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class UtilityModClient implements ClientModInitializer {

    public static KeyBinding GUI_KEY;

    @Override
    public void onInitializeClient() {
        // Register the GUI keybind (Right Shift by default)
        GUI_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.utilitymod.gui",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                "category.utilitymod"
        ));

        // Register all modules and their keybinds
        ModuleManager.getInstance().registerAll();

        // Tick handler: GUI open + per-module ticks + keybind polling
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            if (GUI_KEY.wasPressed()) {
                client.setScreen(new ModuleScreen());
            }

            // Poll module keybinds
            ModuleManager.getInstance().pollKeybinds();

            // Tick every enabled module
            ModuleManager.getInstance().tickModules();
        });

        UtilityMod.LOGGER.info("[UtilityMod] Client initialized.");
    }
}
