package me.johardt.energy

import net.minecraft.world.level.block.Block
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent

object CobblemonChargedCapabilities {
    private val healingMachineBlock: Block by lazy(LazyThreadSafetyMode.NONE) {
        val cobblemonBlocksClass = Class.forName("com.cobblemon.mod.common.CobblemonBlocks")
        val healingMachineField = cobblemonBlocksClass.getDeclaredField("HEALING_MACHINE")
        healingMachineField.get(null) as? Block
            ?: error("Cobblemon HEALING_MACHINE field was not a Block")
    }

    @JvmStatic
    fun registerCapabilities(event: RegisterCapabilitiesEvent) {
        event.registerBlock(Capabilities.EnergyStorage.BLOCK, { _, _, _, blockEntity, _ ->
            (blockEntity as? PoweredHealingMachine)?.`cobblemonCharged$getEnergyStorage`()
        }, healingMachineBlock)
    }
}
