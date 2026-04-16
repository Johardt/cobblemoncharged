package me.johardt.energy;

public final class HealingMachineEnergy {
    public static final String NBT_KEY = "ChargedEnergy";
    public static final int ENERGY_PER_CHARGE = 4_000;

    private HealingMachineEnergy() {
    }

    public static int energyCapacity(float maxCharge) {
        return (int) Math.clamp(Math.ceil(maxCharge * ENERGY_PER_CHARGE), 1D, Integer.MAX_VALUE);
    }

    public static int energyCost(float healingRemainderPercent) {
        return (int) Math.clamp(Math.ceil(Math.max(0.0F, healingRemainderPercent) * ENERGY_PER_CHARGE), 0D, Integer.MAX_VALUE);
    }

    public static float displayedCharge(int storedEnergy) {
        return (float) storedEnergy / ENERGY_PER_CHARGE;
    }

    public static int clampStoredEnergy(int amount, int capacity) {
        return Math.clamp(amount, 0, capacity);
    }
}
