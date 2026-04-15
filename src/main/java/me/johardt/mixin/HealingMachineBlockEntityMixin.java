package me.johardt.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity;
import me.johardt.CobblemonCharged;
import me.johardt.energy.PoweredHealingMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(HealingMachineBlockEntity.class)
public abstract class HealingMachineBlockEntityMixin extends BlockEntity implements PoweredHealingMachine {
    @Unique
    private static final String COBBLEMON_CHARGED_ENERGY_KEY = "ChargedEnergy";
    @Unique
    private static final int COBBLEMON_CHARGED_ENERGY_PER_CHARGE = 4_000;

    @Unique
    private ChargedEnergyStorage cobblemoncharged_neoforge$chargedEnergyStorage;

    protected HealingMachineBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow(remap = false)
    public abstract float getMaxCharge();

    @Shadow(remap = false)
    public abstract void setHealingCharge(float healingCharge);

    @Shadow(remap = false)
    public abstract boolean getInfinite();

    @Invoker(value = "updateRedstoneSignal", remap = false)
    protected abstract void invokeUpdateRedstoneSignal();

    @Invoker(value = "updateBlockChargeLevel", remap = false)
    protected abstract void invokeUpdateBlockChargeLevel(Integer chargeLevel);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initEnergy(BlockPos pos, BlockState state, CallbackInfo ci) {
        cobblemoncharged_neoforge$chargedEnergyStorage = new ChargedEnergyStorage(cobblemoncharged_neoforge$getEnergyCapacity(), () -> {
            syncDisplayedCharge();
            setChanged();
        });
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void readEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        ChargedEnergyStorage energyStorage = cobblemoncharged_neoforge$getChargedEnergyStorage();
        energyStorage.setStoredEnergy(tag.getInt(COBBLEMON_CHARGED_ENERGY_KEY));
        syncDisplayedCharge();
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = false)
    private void writeEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putInt(COBBLEMON_CHARGED_ENERGY_KEY, cobblemoncharged_neoforge$getChargedEnergyStorage().getEnergyStored());
    }

    @Inject(method = "canHeal", at = @At("HEAD"), cancellable = true, remap = false)
    private void checkEnergyForHealing(PartyStore party, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cobblemoncharged_neoforge$canHealWithEnergy(party));
    }

    @Inject(method = "activate", at = @At("HEAD"), cancellable = true, remap = false)
    private void consumeEnergy(UUID user, PartyStore party, CallbackInfo ci) {
        if (Cobblemon.config.getInfiniteHealerCharge() || getInfinite()) {
            return;
        }

        int energyCost = cobblemoncharged_neoforge$getEnergyCost(party);
        if (energyCost <= 0) {
            return;
        }

        int extracted = cobblemoncharged_neoforge$getChargedEnergyStorage().extractInternally(energyCost);
        if (extracted != energyCost) {
            CobblemonCharged.LOGGER.warn(
                "Healing machine at {} was blocked because it lacked stored energy (needed {}, extracted {}).",
                getBlockPos(),
                energyCost,
                extracted
            );
            ci.cancel();
        }
    }

    @Inject(method = "activate", at = @At("TAIL"), remap = false)
    private void resyncAfterActivation(UUID user, PartyStore party, CallbackInfo ci) {
        syncDisplayedCharge();
    }

    @Override
    public IEnergyStorage getEnergyStorage() {
        return cobblemoncharged_neoforge$getChargedEnergyStorage();
    }

    @Override
    @Unique
    public void syncDisplayedCharge() {
        Level currentLevel = getLevel();
        if (currentLevel == null || currentLevel.isClientSide) {
            return;
        }

        float displayedCharge = (float) cobblemoncharged_neoforge$getChargedEnergyStorage().getEnergyStored() / COBBLEMON_CHARGED_ENERGY_PER_CHARGE;
        setHealingCharge(Math.min(getMaxCharge(), displayedCharge));
        invokeUpdateRedstoneSignal();
        invokeUpdateBlockChargeLevel(null);
    }

    @Unique
    private ChargedEnergyStorage cobblemoncharged_neoforge$getChargedEnergyStorage() {
        if (cobblemoncharged_neoforge$chargedEnergyStorage == null) {
            throw new IllegalStateException("Healing machine energy storage was not initialized");
        }
        return cobblemoncharged_neoforge$chargedEnergyStorage;
    }

    @Unique
    private boolean cobblemoncharged_neoforge$canHealWithEnergy(PartyStore party) {
        if (Cobblemon.config.getInfiniteHealerCharge() || getInfinite()) {
            return true;
        }

        return cobblemoncharged_neoforge$getChargedEnergyStorage().getEnergyStored() >= cobblemoncharged_neoforge$getEnergyCost(party);
    }

    @Unique
    private int cobblemoncharged_neoforge$getEnergyCapacity() {
        return (int) Math.clamp(Math.ceil(getMaxCharge() * COBBLEMON_CHARGED_ENERGY_PER_CHARGE), 1D, Integer.MAX_VALUE);
    }

    @Unique
    private int cobblemoncharged_neoforge$getEnergyCost(PartyStore party) {
        float healingRemainder = Math.max(0.0F, party.getHealingRemainderPercent());
        return (int) Math.clamp(Math.ceil(healingRemainder * COBBLEMON_CHARGED_ENERGY_PER_CHARGE), 0D, Integer.MAX_VALUE);
    }

    @Inject(method = "TICKER$lambda$0", at = @At("TAIL"), remap = false)
    private static void syncTickerCharge(Level level, BlockPos pos, BlockState state, HealingMachineBlockEntity blockEntity, CallbackInfo ci) {
        ((PoweredHealingMachine) (Object) blockEntity).syncDisplayedCharge();
    }

    @Unique
    private static final class ChargedEnergyStorage extends EnergyStorage {
        private final Runnable onChanged;

        private ChargedEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity, capacity, 0);
            this.onChanged = onChanged;
        }

        private void setStoredEnergy(int amount) {
            energy = Math.clamp(amount, 0, capacity);
        }

        private int extractInternally(int amount) {
            if (amount <= 0) {
                return 0;
            }

            int extracted = Math.min(energy, amount);
            if (extracted > 0) {
                energy -= extracted;
                onChanged.run();
            }
            return extracted;
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);
            if (!simulate && received > 0) {
                onChanged.run();
            }
            return received;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return 0;
        }
    }
}
