package me.johardt.energy;

import com.cobblemon.mod.common.CobblemonBlockEntities;
import team.reborn.energy.api.EnergyStorage;

public final class CobblemonChargedEnergy {
    private static boolean initialized;

    private CobblemonChargedEnergy() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;
        EnergyStorage.SIDED.registerForBlockEntity(
            (blockEntity, direction) -> ((PoweredHealingMachine) (Object) blockEntity).getEnergyStorage(),
            CobblemonBlockEntities.HEALING_MACHINE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
            (blockEntity, direction) -> ((PoweredFossilAnalyzer) (Object) blockEntity).getEnergyStorage(),
            CobblemonBlockEntities.FOSSIL_ANALYZER
        );
    }
}
