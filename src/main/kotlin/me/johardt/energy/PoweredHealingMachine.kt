package me.johardt.energy

import net.neoforged.neoforge.energy.IEnergyStorage

interface PoweredHealingMachine {
    fun getEnergyStorage(): IEnergyStorage

    fun syncDisplayedCharge()
}
