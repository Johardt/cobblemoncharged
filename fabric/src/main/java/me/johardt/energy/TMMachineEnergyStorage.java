package me.johardt.energy;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import team.reborn.energy.api.base.SimpleEnergyStorage;

public final class TMMachineEnergyStorage extends SimpleEnergyStorage {
    private final Runnable onChanged;

    public TMMachineEnergyStorage(long capacity, Runnable onChanged) {
        super(capacity, capacity, 0);
        this.onChanged = onChanged;
    }

    public boolean extractInternally(long amount) {
        if (amount <= 0 || this.amount < amount) {
            return false;
        }

        try (Transaction transaction = Transaction.openOuter()) {
            updateSnapshots(transaction);
            this.amount -= amount;
            transaction.commit();
            return true;
        }
    }

    public void setStoredEnergy(long amount) {
        this.amount = TMMachineEnergy.clampStoredEnergy(amount);
    }

    @Override
    protected void onFinalCommit() {
        onChanged.run();
    }
}
