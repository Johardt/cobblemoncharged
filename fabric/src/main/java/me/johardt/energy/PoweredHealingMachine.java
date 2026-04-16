package me.johardt.energy;

import team.reborn.energy.api.EnergyStorage;

public interface PoweredHealingMachine {
    EnergyStorage getEnergyStorage();

    void syncDisplayedCharge();
}
