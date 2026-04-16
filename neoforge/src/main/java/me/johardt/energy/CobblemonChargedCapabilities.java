package me.johardt.energy;

import com.cobblemon.mod.common.CobblemonBlockEntities;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class CobblemonChargedCapabilities {
    private CobblemonChargedCapabilities() {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CobblemonBlockEntities.HEALING_MACHINE, (blockEntity, direction) ->
            ((PoweredHealingMachine) (Object) blockEntity).getEnergyStorage()
        );
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CobblemonBlockEntities.FOSSIL_ANALYZER, (blockEntity, direction) ->
            ((PoweredFossilAnalyzer) (Object) blockEntity).getEnergyStorage()
        );
    }
}
