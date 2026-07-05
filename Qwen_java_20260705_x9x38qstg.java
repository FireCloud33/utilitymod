package com.utilitymod.module.impl;

import com.utilitymod.module.Module;
import com.utilitymod.util.InventoryHelper;
import com.utilitymod.util.TimerHelper;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * "Legit" auto-totem:
 *   1. Wait 50-150 ms, then open inventory.
 *   2. Wait 170-280 ms, then move a totem to the offhand.
 *   3. Wait 50-250 ms, then close inventory.
 * Repeats while enabled.
 */
public class LegitAutoTotemModule extends Module {

    private enum State { WAITING_OPEN, OPENING, WAITING_MOVE, MOVING, WAITING_CLOSE }
    private State state = State.WAITING_OPEN;

    private final TimerHelper timer = new TimerHelper();
    private long nextDelay;

    public LegitAutoTotemModule() {
        super("Legit AutoTotem", "Legit-style auto totem", Category.COMBAT, -1);
    }

    private static long rand(long min, long max) {
        return min + (long) (Math.random() * (max - min + 1));
    }

    @Override
    protected void onEnable() {
        state = State.WAITING_OPEN;
        nextDelay = rand(50, 150);
        timer.reset();
    }

    @Override
    protected void onDisable() {
        // Close inventory if we left it open
        if (mc.player != null && mc.currentScreen != null) {
            mc.player.closeHandledScreen();
        }
    }

    @Override
    public void tick() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Already have a totem in offhand? Skip the cycle.
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        if (!InventoryHelper.hasItem(Items.TOTEM_OF_UNDYING)) return;

        switch (state) {
            case WAITING_OPEN:
                if (timer.hasElapsed(nextDelay)) {
                    // "Open" the inventory (press E equivalent)
                    InputUtil.Key eKey = InputUtil.fromTranslationKey(
                            mc.options.inventoryKey.getDefaultBoundKeyTranslationKey());
                    // Simulate key press via screen open
                    mc.setScreen(new net.minecraft.client.gui.screen.ingame.InventoryScreen(mc.player));
                    state = State.WAITING_MOVE;
                    nextDelay = rand(170, 280);
                    timer.reset();
                }
                break;

            case WAITING_MOVE:
                if (timer.hasElapsed(nextDelay)) {
                    int totemSlot = InventoryHelper.findInMain(Items.TOTEM_OF_UNDYING);
                    if (totemSlot == -1) totemSlot = InventoryHelper.findInHotbar(Items.TOTEM_OF_UNDYING);
                    if (totemSlot != -1) {
                        // Convert inventory slot index to screen-handler slot index
                        // Hotbar 0-8 -> screen slots 36-44 ; main 9-35 -> screen slots 9-35
                        int screenSlot;
                        if (totemSlot < 9) screenSlot = 36 + totemSlot;
                        else screenSlot = totemSlot;
                        ClientPlayerInteractionManager im = mc.interactionManager;
                        int syncId = mc.player.playerScreenHandler.syncId;
                        im.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
                        im.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
                        // Put anything remaining back
                        im.clickSlot(syncId, screenSlot, 0, SlotActionType.PICKUP, mc.player);
                    }
                    state = State.WAITING_CLOSE;
                    nextDelay = rand(50, 250);
                    timer.reset();
                }
                break;

            case WAITING_CLOSE:
                if (timer.hasElapsed(nextDelay)) {
                    if (mc.currentScreen != null) mc.player.closeHandledScreen();
                    // Restart cycle
                    state = State.WAITING_OPEN;
                    nextDelay = rand(50, 150);
                    timer.reset();
                }
                break;
        }
    }
}