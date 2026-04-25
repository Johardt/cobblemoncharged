package me.johardt.energy;

import com.cobblemon.mod.common.CobblemonBlocks;
import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure;
import me.johardt.CobblemonChargedConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public final class FossilAnalyzerEnergy {
    public static final String NBT_ENERGY_KEY = "ChargedFossilEnergy";
    public static final String NBT_POWERED_KEY = "ChargedFossilPowered";

    private FossilAnalyzerEnergy() {
    }

    public static long capacity() {
        return (long) FossilMultiblockStructure.TIME_TO_TAKE * energyPerRestorationTick();
    }

    public static int capacityInt() {
        return Math.toIntExact(capacity());
    }

    public static int energyPerRestorationTickInt() {
        return Math.toIntExact(energyPerRestorationTick());
    }

    public static long energyPerRestorationTick() {
        long maxEnergyPerTick = Math.max(1L, Integer.MAX_VALUE / (long) FossilMultiblockStructure.TIME_TO_TAKE);
        return Math.min(CobblemonChargedConfig.fossilAnalyzerEnergyPerRestorationTick(), maxEnergyPerTick);
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
