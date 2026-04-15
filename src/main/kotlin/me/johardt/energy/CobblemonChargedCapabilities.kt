package me.johardt.energy

import com.cobblemon.mod.common.CobblemonBlockEntities
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

object CobblemonChargedCapabilities {
    @JvmStatic
    fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, CobblemonBlockEntities.HEALING_MACHINE) { blockEntity, _ ->
            (blockEntity as PoweredHealingMachine).getEnergyStorage()
        }
    }
}
