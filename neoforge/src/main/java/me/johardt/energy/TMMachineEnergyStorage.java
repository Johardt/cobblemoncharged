package me.johardt.energy;

import net.neoforged.neoforge.energy.EnergyStorage;

public final class TMMachineEnergyStorage extends EnergyStorage {
    private final Runnable onChanged;

    public TMMachineEnergyStorage(int capacity, Runnable onChanged) {
        super(capacity, capacity, 0);
        this.onChanged = onChanged;
    }

    public boolean extractInternally(int amount) {
        if (amount <= 0 || energy < amount) {
            return false;
        }

        energy -= amount;
        onChanged.run();
        return true;
    }

    public void setStoredEnergy(long amount) {
        energy = (int) TMMachineEnergy.clampStoredEnergy(amount);
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        int received = super.receiveEnergy(toReceive, simulate);
        if (!simulate && received > 0) {
            onChanged.run();
        }
        return received;
    }
}
