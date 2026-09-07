package me.johardt.energy;

import com.cobblemon.mod.common.block.entity.TMMachineBlockEntity;
import me.johardt.CobblemonChargedConfig;

public final class TMMachineEnergy {
    public static final String NBT_KEY = "ChargedTMMachineEnergy";

    private TMMachineEnergy() {
    }

    public static long capacity() {
        return (long) processingTicks() * energyPerProcessingTick();
    }

    public static int capacityInt() {
        return Math.toIntExact(capacity());
    }

    public static long energyPerProcessingTick() {
        long maxEnergyPerTick = Math.max(1L, Integer.MAX_VALUE / (long) processingTicks());
        return Math.min(CobblemonChargedConfig.tmMachineEnergyPerProcessingTick(), maxEnergyPerTick);
    }

    public static int energyPerProcessingTickInt() {
        return Math.toIntExact(energyPerProcessingTick());
    }

    public static long clampStoredEnergy(long amount) {
        return Math.clamp(amount, 0L, capacity());
    }

    private static int processingTicks() {
        return TMMachineBlockEntity.BURN_TOTAL_TIME / TMMachineBlockEntity.BURN_PROGRESS_PER_TICK;
    }
}
