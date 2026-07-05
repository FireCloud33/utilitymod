package com.utilitymod.gui;

import com.utilitymod.module.Module;
import com.utilitymod.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ModuleScreen extends Screen {

    private final List<ButtonWidget> buttons = new ArrayList<>();

    public ModuleScreen() {
        super(Text.literal("Utility Mod"));
    }

    @Override
    protected void init() {
        buttons.clear();
        int panelW = 220;
        int panelH = 40 + ModuleManager.getInstance().getModules().size() * 26;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        int y = panelY + 28;
        for (Module m : ModuleManager.getInstance().getModules()) {
            ButtonWidget btn = ButtonWidget.builder(
                    buttonText(m),
                    b -> {
                        m.toggle();
                        ((ButtonWidget) b).setMessage(buttonText(m));
                    }
            ).dimensions(panelX + 10, y, panelW - 20, 20).build();
            addDrawableChild(btn);
            buttons.add(btn);
            y += 26;
        }
    }

    private Text buttonText(Module m) {
        String state = m.isEnabled() ? "§a[ON]" : "§c[OFF]";
        return Text.literal(m.getName() + "  " + state);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dark translucent background
        ctx.fill(0, 0, width, height, 0xAA101018);

        int panelW = 220;
        int panelH = 40 + ModuleManager.getInstance().getModules().size() * 26;
        int panelX = (width - panelW) / 2;
        int panelY = (height - panelH) / 2;

        // Panel background
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xFF20202A);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 1, 0xFF5555AA);
        ctx.fill(panelX, panelY + panelH - 1, panelX + panelW, panelY + panelH, 0xFF5555AA);

        // Title
        String title = "Utility Mod";
        int titleW = textRenderer.getWidth(title);
        ctx.drawText(textRenderer, Text.literal(title),
                panelX + (panelW - titleW) / 2, panelY + 10, 0xFFFFFF, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
