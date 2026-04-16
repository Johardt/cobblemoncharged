package me.johardt.energy;

import com.cobblemon.mod.common.CobblemonBlocks;
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public final class FossilAnalyzerEnergy {
    public static final String NBT_ENERGY_KEY = "ChargedFossilEnergy";
    public static final String NBT_POWERED_KEY = "ChargedFossilPowered";
    public static final long ENERGY_PER_RESTORATION_TICK = 20L;

    private FossilAnalyzerEnergy() {
    }

    public static long capacity() {
        return (long) FossilMultiblockStructure.TIME_TO_TAKE * ENERGY_PER_RESTORATION_TICK;
    }

    public static int capacityInt() {
        return Math.toIntExact(capacity());
    }

    public static int energyPerRestorationTickInt() {
        return Math.toIntExact(ENERGY_PER_RESTORATION_TICK);
    }

    public static long clampStoredEnergy(long amount) {
        return Math.clamp(amount, 0L, capacity());
    }

    public static boolean readPowered(CompoundTag tag) {
        return !tag.contains(NBT_POWERED_KEY) || tag.getBoolean(NBT_POWERED_KEY);
    }

    public static boolean usesEnergy(BlockState state) {
        return state.getBlock() == CobblemonBlocks.FOSSIL_ANALYZER;
    }
}
