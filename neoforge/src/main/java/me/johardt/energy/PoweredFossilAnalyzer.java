package me.johardt.energy;

import net.neoforged.neoforge.energy.IEnergyStorage;

public interface PoweredFossilAnalyzer {
    IEnergyStorage getEnergyStorage();

    boolean isRestorationPowered();

    boolean setRestorationPowered(boolean powered);
}
