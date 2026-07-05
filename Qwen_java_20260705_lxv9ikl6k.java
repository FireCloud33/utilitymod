package com.utilitymod.module.impl;

import com.utilitymod.module.Module;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;

public class VisualOverlayModule extends Module {

    public VisualOverlayModule() {
        super("Visual Overlay", "Client-side vignette effect", Category.VISUAL, -1);
    }

    @Override
    protected void onEnable() {
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            if (!isEnabled()) return;
            int w = mc.getWindow().getScaledWidth();
            int h = mc.getWindow().getScaledHeight();
            // Dark vignette border
            int color = 0x55000000;
            int thickness = 6;
            ctx.fill(0, 0, w, thickness, color);
            ctx.fill(0, h - thickness, w, h, color);
            ctx.fill(0, 0, thickness, h, color);
            ctx.fill(w - thickness, 0, w, h, color);
        });
    }

    @Override
    protected void onDisable() { }
}