package com.utilitymod.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

public class InventoryHelper {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    /** Returns hotbar slot index (0-8) holding the item, or -1. */
    public static int findInHotbar(Item item) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    /** Returns main inventory slot index (9-35) holding the item, or -1. */
    public static int findInMain(Item item) {
        ClientPlayerEntity player = mc.player;
        if (player == null) return -1;
        for (int i = 9; i < 36; i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == item) return i;
        }
        return -1;
    }

    /** Switch the player's selected hotbar slot. */
    public static void selectHotbarSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        if (mc.player != null) {
            mc.player.getInventory().selectedSlot = slot;
        }
    }

    /**
     * Move an item from a main-inventory slot (9-35) to the offhand slot (index 45 in the player screen handler).
     */
    public static void moveToOffhand(int invSlot) {
        ClientPlayerInteractionManager im = mc.interactionManager;
        if (im == null || mc.player == null) return;
        int syncId = mc.player.playerScreenHandler.syncId;
        // offhand slot index in PlayerScreenHandler is 45
        im.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, mc.player);
        im.clickSlot(syncId, 45, 0, SlotActionType.PICKUP, mc.player);
        // If anything is left on the cursor, put it back
        im.clickSlot(syncId, invSlot, 0, SlotActionType.PICKUP, mc.player);
    }

    /** Returns true if the player has the item anywhere in their inventory. */
    public static boolean hasItem(Item item) {
        return findInHotbar(item) != -1 || findInMain(item) != -1;
    }
}
