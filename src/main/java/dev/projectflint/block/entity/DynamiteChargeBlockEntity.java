package dev.projectflint.block.entity;

import dev.projectflint.registry.ModBlockEntities;
import dev.projectflint.system.BasinMaterialProcessingSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-controlled, stone-only fictional blasting. Does not use TNT block destruction. */
public final class DynamiteChargeBlockEntity extends BlockEntity {
    public static final int FUSE_TICKS = 90;
    private static final double RADIUS = 2.75;
    private int fuseTicks = FUSE_TICKS;

    public DynamiteChargeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DYNAMITE_CHARGE, pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DynamiteChargeBlockEntity charge) {
        if (!(level instanceof ServerLevel server)) return;
        if (--charge.fuseTicks <= 0) {
            detonate(server, pos);
            return;
        }
        if (charge.fuseTicks % 10 == 0) {
            server.sendParticles(ParticleTypes.SMOKE, pos.getX() + .5, pos.getY() + .8, pos.getZ() + .5,
                2, .10, .10, .10, .015);
            charge.setChanged();
        }
    }

    private static boolean blastable(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
            || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE)
            || state.is(Blocks.IRON_ORE) || state.is(Blocks.COPPER_ORE) || state.is(Blocks.COAL_ORE);
    }

    private static ItemStack basinInput(BlockState state) {
        if (state.is(Blocks.IRON_ORE)) return new ItemStack(Items.RAW_IRON);
        if (state.is(Blocks.COPPER_ORE)) return new ItemStack(Items.RAW_COPPER);
        if (state.is(Blocks.COAL_ORE)) return new ItemStack(Items.COAL);
        return new ItemStack(state.getBlock().asItem());
    }

    private static void detonate(ServerLevel server, BlockPos pos) {
        // Remove the charge first, then apply a selective non-TNT stone blast.
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        Vec3 center = Vec3.atCenterOf(pos);
        server.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.0F, 0.95F);
        server.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z, 1, 0, 0, 0, 0);
        int range = (int) Math.ceil(RADIUS);
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-range,-range,-range), pos.offset(range,range,range))) {
            if (Vec3.atCenterOf(candidate).distanceToSqr(center) > RADIUS * RADIUS) continue;
            BlockState original = server.getBlockState(candidate);
            if (!blastable(original)) continue; // never remove deepslate, non-ores or placed machines
            if (!server.setBlock(candidate.immutable(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) continue;
            // Every original Basin roll is independently increased by 50%, including odds above 100%.
            for (ItemStack drop : BasinMaterialProcessingSystem.processWithMultiplier(
                    basinInput(original), server.getRandom(), 1.5F)) {
                Block.popResource(server, candidate, drop);
            }
        }
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos).inflate(RADIUS), entity -> entity.isAlive())) {
            double distance = entity.position().distanceTo(center);
            if (distance > RADIUS) continue;
            float damage = 12.0F; // Fixed damage inside the blast radius, as specified.
            if (damage > 0) entity.hurtServer(server,
                server.damageSources().explosion((net.minecraft.world.level.Explosion) null), damage);
        }
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("FuseTicks", fuseTicks);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        fuseTicks = Math.clamp(input.getIntOr("FuseTicks", FUSE_TICKS), 1, FUSE_TICKS);
    }
}
