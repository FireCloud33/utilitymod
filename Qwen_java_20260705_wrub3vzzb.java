package com.utilitymod.module.impl;

import com.utilitymod.module.Module;
import com.utilitymod.util.TimerHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 * AutoCrystal:
 *   - Player must be within 3 blocks of an obsidian block.
 *   - Player must be holding an end crystal.
 *   - Player must be looking at the obsidian block.
 *   - Right-click to place (43-98 ms).
 *   - Left-click to break (117-287 ms).
 */
public class AutoCrystalModule extends Module {

    private enum State { IDLE, PLACING, BREAKING }
    private State state = State.IDLE;
    private final TimerHelper timer = new TimerHelper();
    private long nextDelay;
    private BlockPos target;

    public AutoCrystalModule() {
        super("Auto Crystal", "Auto place/break end crystals", Category.COMBAT, -1);
    }

    private static long rand(long min, long max) {
        return min + (long) (Math.random() * (max - min + 1));
    }

    @Override
    protected void onEnable() {
        state = State.IDLE;
        timer.reset();
    }

    @Override
    protected void onDisable() { }

    @Override
    public void tick() {
        if (mc.player == null || mc.interactionManager == null || mc.world == null) return;

        // Must be holding a crystal
        if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL) return;

        // Must be looking at an obsidian block within 3 blocks
        HitResult hr = mc.crosshairTarget;
        if (!(hr instanceof BlockHitResult bhr) || bhr.getType() != HitResult.Type.BLOCK) return;
        BlockPos looked = bhr.getBlockPos();
        if (mc.world.getBlockState(looked).getBlock() != Blocks.OBSIDIAN) return;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(looked)) > 3.0) return;

        target = looked;

        switch (state) {
            case IDLE:
                state = State.PLACING;
                nextDelay = rand(43, 98);
                timer.reset();
                break;

            case PLACING:
                if (timer.hasElapsed(nextDelay)) {
                    // Refresh the hit result
                    HitResult cur = mc.crosshairTarget;
                    if (cur instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
                        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, b);
                    }
                    state = State.BREAKING;
                    nextDelay = rand(117, 287);
                    timer.reset();
                }
                break;

            case BREAKING:
                if (timer.hasElapsed(nextDelay)) {
                    // Find a crystal near the target and attack it
                    EndCrystalEntity crystal = findCrystalNear(target);
                    if (crystal != null) {
                        mc.interactionManager.attackEntity(mc.player, crystal);
                    } else {
                        // Fallback: attack the block itself
                        mc.interactionManager.attackBlock(target, net.minecraft.util.math.Direction.UP);
                    }
                    state = State.IDLE;
                }
                break;
        }
    }

    private EndCrystalEntity findCrystalNear(BlockPos pos) {
        for (net.minecraft.entity.Entity e : mc.world.getEntities()) {
            if (e instanceof EndCrystalEntity c) {
                if (c.getBlockPos().isWithinDistance(pos, 2)) return c;
            }
        }
        return null;
    }
}