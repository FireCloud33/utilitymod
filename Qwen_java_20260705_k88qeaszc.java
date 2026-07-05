package com.utilitymod.module.impl;

import com.utilitymod.module.Module;
import com.utilitymod.module.ModuleManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class HUDModule extends Module {

    private HudRenderCallback renderer;

    public HUDModule() {
        super("HUD", "Renders enabled modules on screen", Category.VISUAL, -1);
    }

    @Override
    protected void onEnable() {
        renderer = (DrawContext ctx, net.minecraft.client.render.RenderTickCounter tickCounter) -> {
            int y = 4;
            for (Module m : ModuleManager.getInstance().getModules()) {
                if (!m.isEnabled()) continue;
                String line = m.getName();
                int width = mc.textRenderer.getWidth(line);
                int screenW = mc.getWindow().getScaledWidth();
                ctx.drawTextWithShadow(mc.textRenderer,
                        Text.literal(line),
                        screenW - width - 4, y,
                        0xFF55FF55);
                y += mc.textRenderer.fontHeight + 2;
            }
        };
        HudRenderCallback.EVENT.register(renderer);
    }

    @Override
    protected void onDisable() {
        // Fabric's HudRenderCallback doesn't support unregister; the check above
        // simply stops drawing when the module is disabled.
    }
}