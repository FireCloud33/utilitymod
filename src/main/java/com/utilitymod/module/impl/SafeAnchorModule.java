package com.utilitymod.module.impl;

import com.utilitymod.module.Module;
import com.utilitymod.util.InventoryHelper;
import com.utilitymod.util.TimerHelper;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;

/**
 * Safe Anchor — spam cycle at the block the player is looking at:
 *   1. Switch to anchor         (50-100 ms)
 *   2. Place anchor             (50-100 ms)
 *   3. Switch to glowstone      (50-80  ms)
 *   4. Load anchor              (50-80  ms)
 *   5. Place protective glowstone (50-150 ms)
 *   6. Switch to totem          (50-135 ms)
 *   7. Detonate anchor          (50-135 ms)
 *   8. Repeat.
 */
public class SafeAnchorModule extends Module {

    private enum State {
        SWITCH_TO_ANCHOR, PLACE_ANCHOR,
        SWITCH_TO_GLOWSTONE, LOAD_ANCHOR, PLACE_PROTECTIVE,
        SWITCH_TO_TOTEM, DETONATE,
        WAIT
    }

    private State state = State.SWITCH_TO_ANCHOR;
    private final TimerHelper timer = new TimerHelper();
    private long nextDelay;

    public SafeAnchorModule() {
        super("Safe Anchor", "Anchor + glowstone spam cycle", Category.COMBAT, -1);
    }

    private static long rand(long min, long max) {
        return min + (long) (Math.random() * (max - min + 1));
    }

    @Override
    protected void onEnable() {
        state = State.SWITCH_TO_ANCHOR;
        nextDelay = rand(50, 100);
        timer.reset();
    }

    @Override
    protected void onDisable() { }

    @Override
    public void tick() {
        if (mc.player == null || mc.interactionManager == null || mc.world == null) return;

        // Ensure required items exist
        if (!InventoryHelper.hasItem(Items.RESPAWN_ANCHOR)
                || !InventoryHelper.hasItem(Items.GLOWSTONE)
                || !InventoryHelper.hasItem(Items.TOTEM_OF_UNDYING)) {
            return;
        }

        // Crosshair target
        HitResult hr = mc.crosshairTarget;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) return;

        switch (state) {

            case SWITCH_TO_ANCHOR: {
                int slot = InventoryHelper.findInHotbar(Items.RESPAWN_ANCHOR);
                if (slot != -1) InventoryHelper.selectHotbarSlot(slot);
                if (timer.hasElapsed(nextDelay)) {
                    state = State.PLACE_ANCHOR;
                    nextDelay = rand(50, 100);
                    timer.reset();
                }
                break;
            }

            case PLACE_ANCHOR: {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhrAt(bhr));
                if (timer.hasElapsed(nextDelay)) {
                    state = State.SWITCH_TO_GLOWSTONE;
                    nextDelay = rand(50, 80);
                    timer.reset();
                }
                break;
            }

            case SWITCH_TO_GLOWSTONE: {
                int slot = InventoryHelper.findInHotbar(Items.GLOWSTONE);
                if (slot != -1) InventoryHelper.selectHotbarSlot(slot);
                if (timer.hasElapsed(nextDelay)) {
                    state = State.LOAD_ANCHOR;
                    nextDelay = rand(50, 80);
                    timer.reset();
                }
                break;
            }

            case LOAD_ANCHOR: {
                // Right-click the placed anchor to load it with glowstone
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhrAt(bhr));
                if (timer.hasElapsed(nextDelay)) {
                    state = State.PLACE_PROTECTIVE;
                    nextDelay = rand(50, 150);
                    timer.reset();
                }
                break;
            }

            case PLACE_PROTECTIVE: {
                // Place a second glowstone as protection above/near the anchor
                BlockHitResult above = new BlockHitResult(
                        bhr.getPos().add(0, 1, 0),
                        Direction.UP,
                        bhr.getBlockPos().up(),
                        false);
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, above);
                if (timer.hasElapsed(nextDelay)) {
                    state = State.SWITCH_TO_TOTEM;
                    nextDelay = rand(50, 135);
                    timer.reset();
                }
                break;
            }

            case SWITCH_TO_TOTEM: {
                int slot = InventoryHelper.findInHotbar(Items.TOTEM_OF_UNDYING);
                if (slot != -1) InventoryHelper.selectHotbarSlot(slot);
                if (timer.hasElapsed(nextDelay)) {
                    state = State.DETONATE;
                    nextDelay = rand(50, 135);
                    timer.reset();
                }
                break;
            }

            case DETONATE: {
                // Left-click the anchor to detonate
                mc.interactionManager.attackBlock(bhr.getBlockPos(), bhr.getSide());
                if (timer.hasElapsed(nextDelay)) {
                    state = State.WAIT;
                    nextDelay = rand(40, 80);
                    timer.reset();
                }
                break;
            }

            case WAIT: {
                if (timer.hasElapsed(nextDelay)) {
                    state = State.SWITCH_TO_ANCHOR;
                    nextDelay = rand(50, 100);
                    timer.reset();
                }
                break;
            }
        }
    }

    /** Refresh the BlockHitResult against the current crosshair each action. */
    private BlockHitResult bhrAt(BlockHitResult base) {
        HitResult hr = mc.crosshairTarget;
        if (hr instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) return b;
        return base;
    }
}
