package me.johardt.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

public interface PoweredTMMachine {
    IEnergyStorage getEnergyStorage();

    boolean consumeProcessingEnergy();
}
