package me.johardt.energy;

import team.reborn.energy.api.EnergyStorage;

public interface PoweredTMMachine {
    EnergyStorage getEnergyStorage();

    boolean consumeProcessingEnergy();
}
