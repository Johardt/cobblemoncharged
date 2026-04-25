package me.johardt.energy;

import me.johardt.CobblemonChargedConfig;

public final class HealingMachineEnergy {
    public static final String NBT_KEY = "ChargedEnergy";

    private HealingMachineEnergy() {
    }

    public static int energyCapacity(float maxCharge) {
        return (int) Math.clamp(Math.ceil(maxCharge * CobblemonChargedConfig.healingMachineEnergyPerCharge()), 1D, Integer.MAX_VALUE);
    }

    public static int energyCost(float healingRemainderPercent) {
        return (int) Math.clamp(Math.ceil(Math.max(0.0F, healingRemainderPercent) * CobblemonChargedConfig.healingMachineEnergyPerCharge()), 0D, Integer.MAX_VALUE);
    }

    public static float displayedCharge(int storedEnergy) {
        return (float) storedEnergy / CobblemonChargedConfig.healingMachineEnergyPerCharge();
    }

    public static int clampStoredEnergy(int amount, int capacity) {
        return Math.clamp(amount, 0, capacity);
    }
}
