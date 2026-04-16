package me.johardt.mixin;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.storage.party.PartyStore;
import com.cobblemon.mod.common.block.entity.HealingMachineBlockEntity;
import me.johardt.CobblemonCharged;
import me.johardt.energy.HealingMachineEnergy;
import me.johardt.energy.PoweredHealingMachine;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

import java.util.UUID;

@Mixin(HealingMachineBlockEntity.class)
public abstract class HealingMachineBlockEntityMixin extends BlockEntity implements PoweredHealingMachine {
    @Unique
    private ChargedEnergyStorage cobblemoncharged_fabric$chargedEnergyStorage;

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
        cobblemoncharged_fabric$chargedEnergyStorage = new ChargedEnergyStorage(cobblemoncharged_fabric$getEnergyCapacity(), () -> {
            syncDisplayedCharge();
            setChanged();
        });
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void readEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        cobblemoncharged_fabric$getChargedEnergyStorage().setStoredEnergy(tag.getInt(HealingMachineEnergy.NBT_KEY));
        syncDisplayedCharge();
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void writeEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putInt(HealingMachineEnergy.NBT_KEY, (int) cobblemoncharged_fabric$getChargedEnergyStorage().getAmount());
    }

    @Inject(method = "canHeal", at = @At("HEAD"), cancellable = true, remap = false)
    private void checkEnergyForHealing(PartyStore party, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cobblemoncharged_fabric$canHealWithEnergy(party));
    }

    @Inject(method = "activate", at = @At("HEAD"), cancellable = true, remap = false)
    private void consumeEnergy(UUID user, PartyStore party, CallbackInfo ci) {
        if (Cobblemon.INSTANCE.getConfig().getInfiniteHealerCharge() || getInfinite()) {
            return;
        }

        int energyCost = cobblemoncharged_fabric$getEnergyCost(party);
        if (energyCost <= 0) {
            return;
        }

        int extracted = cobblemoncharged_fabric$getChargedEnergyStorage().extractInternally(energyCost);
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
    public EnergyStorage getEnergyStorage() {
        return cobblemoncharged_fabric$getChargedEnergyStorage();
    }

    @Override
    @Unique
    public void syncDisplayedCharge() {
        Level currentLevel = getLevel();
        if (currentLevel == null || currentLevel.isClientSide) {
            return;
        }

        float displayedCharge = HealingMachineEnergy.displayedCharge((int) cobblemoncharged_fabric$getChargedEnergyStorage().getAmount());
        setHealingCharge(Math.min(getMaxCharge(), displayedCharge));
        invokeUpdateRedstoneSignal();
        invokeUpdateBlockChargeLevel(null);
    }

    @Unique
    private ChargedEnergyStorage cobblemoncharged_fabric$getChargedEnergyStorage() {
        if (cobblemoncharged_fabric$chargedEnergyStorage == null) {
            throw new IllegalStateException("Healing machine energy storage was not initialized");
        }

        return cobblemoncharged_fabric$chargedEnergyStorage;
    }

    @Unique
    private boolean cobblemoncharged_fabric$canHealWithEnergy(PartyStore party) {
        if (Cobblemon.INSTANCE.getConfig().getInfiniteHealerCharge() || getInfinite()) {
            return true;
        }

        return cobblemoncharged_fabric$getChargedEnergyStorage().getAmount() >= cobblemoncharged_fabric$getEnergyCost(party);
    }

    @Unique
    private int cobblemoncharged_fabric$getEnergyCapacity() {
        return HealingMachineEnergy.energyCapacity(getMaxCharge());
    }

    @Unique
    private int cobblemoncharged_fabric$getEnergyCost(PartyStore party) {
        return HealingMachineEnergy.energyCost(party.getHealingRemainderPercent());
    }

    @Inject(method = "TICKER$lambda$0", at = @At("TAIL"), remap = false)
    private static void syncTickerCharge(Level level, BlockPos pos, BlockState state, HealingMachineBlockEntity blockEntity, CallbackInfo ci) {
        ((PoweredHealingMachine) (Object) blockEntity).syncDisplayedCharge();
    }

    @Unique
    private static final class ChargedEnergyStorage extends SimpleEnergyStorage {
        private final Runnable onChanged;

        private ChargedEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity, capacity, 0);
            this.onChanged = onChanged;
        }

        private void setStoredEnergy(int amount) {
            this.amount = HealingMachineEnergy.clampStoredEnergy(amount, (int) this.capacity);
        }

        private int extractInternally(int maxAmount) {
            if (maxAmount <= 0) {
                return 0;
            }

            try (Transaction transaction = Transaction.openOuter()) {
                int extracted = (int) Math.min(this.amount, maxAmount);
                if (extracted > 0) {
                    updateSnapshots(transaction);
                    this.amount -= extracted;
                    transaction.commit();
                }
                return extracted;
            }
        }

        @Override
        protected void onFinalCommit() {
            onChanged.run();
        }
    }
}
