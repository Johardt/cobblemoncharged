package me.johardt.energy;

import team.reborn.energy.api.EnergyStorage;

public interface PoweredFossilAnalyzer {
    EnergyStorage getEnergyStorage();

    boolean isRestorationPowered();

    boolean setRestorationPowered(boolean powered);
}
