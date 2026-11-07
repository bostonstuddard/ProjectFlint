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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Server-controlled, stone-only fictional blasting. Does not use TNT block destruction. */
public final class DynamiteChargeBlockEntity extends BlockEntity {
    public static final int FUSE_TICKS = 90;
    // This weak contact charge is for isolated small ore veins, not excavation.
    private static final double ORE_RADIUS = 1.75;
    private static final double ROCK_RADIUS = 1.25;
    private static final double ENTITY_RADIUS = 1.75;
    private static final int MAX_ORE_BLOCKS = 2;
    private static final int MAX_ROCK_BLOCKS = 1;
    private static final double ROCK_BREAK_CHANCE = 0.50;
    private static final float BASIN_YIELD_MULTIPLIER = 1.20F;
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

    private static boolean blastableOre(BlockState state) {
        return state.is(Blocks.IRON_ORE) || state.is(Blocks.COPPER_ORE) || state.is(Blocks.COAL_ORE);
    }

    private static boolean blastableRock(BlockState state) {
        return state.is(Blocks.STONE) || state.is(Blocks.COBBLESTONE)
            || state.is(Blocks.ANDESITE) || state.is(Blocks.DIORITE) || state.is(Blocks.GRANITE);
    }

    private static ItemStack basinInput(BlockState state) {
        if (state.is(Blocks.IRON_ORE)) return new ItemStack(Items.RAW_IRON);
        if (state.is(Blocks.COPPER_ORE)) return new ItemStack(Items.RAW_COPPER);
        if (state.is(Blocks.COAL_ORE)) return new ItemStack(Items.COAL);
        return new ItemStack(state.getBlock().asItem());
    }

    private static void detonate(ServerLevel server, BlockPos pos) {
        // Never route this through the vanilla explosion's unrestricted block destruction.
        server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        Vec3 center = Vec3.atCenterOf(pos);
        server.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.1F, 1.15F);
        server.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 2, .12, .12, .12, .01);
        server.sendParticles(ParticleTypes.SMOKE, center.x, center.y, center.z, 8, .3, .3, .3, .02);

        // Collect only allowlisted ordinary stone/ores; never deepslate, machines or other rock.
        List<BlockPos> oreTargets = new ArrayList<>();
        List<BlockPos> rockTargets = new ArrayList<>();
        int range = (int) Math.ceil(ORE_RADIUS);
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-range, -range, -range),
                pos.offset(range, range, range))) {
            BlockState original = server.getBlockState(candidate);
            double distanceSq = Vec3.atCenterOf(candidate).distanceToSqr(center);
            if (blastableOre(original) && distanceSq <= ORE_RADIUS * ORE_RADIUS)
                oreTargets.add(candidate.immutable());
            else if (blastableRock(original) && distanceSq <= ROCK_RADIUS * ROCK_RADIUS)
                rockTargets.add(candidate.immutable());
        }
        // Randomize target order so nearby ore has an equal chance, independent of axis order.
        shuffle(oreTargets, server);
        shuffle(rockTargets, server);
        int oresBroken = 0;
        for (BlockPos candidate : oreTargets) {
            if (oresBroken >= MAX_ORE_BLOCKS) break;
            if (breakAndDrop(server, candidate)) oresBroken++;
        }
        int rocksBroken = 0;
        for (BlockPos candidate : rockTargets) {
            if (rocksBroken >= MAX_ROCK_BLOCKS) break;
            if (server.getRandom().nextDouble() >= ROCK_BREAK_CHANCE) continue;
            if (breakAndDrop(server, candidate)) rocksBroken++;
        }
        for (LivingEntity entity : server.getEntitiesOfClass(LivingEntity.class,
                new AABB(pos).inflate(ENTITY_RADIUS), entity -> entity.isAlive())) {
            if (entity.position().distanceTo(center) > ENTITY_RADIUS) continue;
            entity.hurtServer(server,
                server.damageSources().explosion((net.minecraft.world.level.Explosion) null), 12.0F);
        }
    }

    private static boolean breakAndDrop(ServerLevel server, BlockPos pos) {
        BlockState original = server.getBlockState(pos);
        if (!blastableOre(original) && !blastableRock(original)) return false;
        if (!server.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL)) return false;
        // Each Basin yield roll is independent: 100%+ always produces its guaranteed
        // portion plus a fractional chance for an extra piece.
        for (ItemStack drop : BasinMaterialProcessingSystem.processWithMultiplier(
                basinInput(original), server.getRandom(), BASIN_YIELD_MULTIPLIER)) {
            Block.popResource(server, pos, drop);
        }
        return true;
    }

    private static void shuffle(List<BlockPos> positions, ServerLevel server) {
        for (int i = positions.size() - 1; i > 0; i--)
            Collections.swap(positions, i, server.getRandom().nextInt(i + 1));
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
