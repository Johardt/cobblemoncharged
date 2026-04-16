package me.johardt.mixin;

import com.cobblemon.mod.common.api.multiblock.builder.MultiblockStructureBuilder;
import com.cobblemon.mod.common.block.entity.FossilMultiblockEntity;
import me.johardt.energy.FossilAnalyzerEnergy;
import me.johardt.energy.PoweredFossilAnalyzer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FossilMultiblockEntity.class)
public abstract class FossilAnalyzerBlockEntityMixin extends BlockEntity implements PoweredFossilAnalyzer {
    @Unique
    private ChargedEnergyStorage cobblemoncharged_neoforge$energyStorage;

    @Unique
    private boolean cobblemoncharged_neoforge$restorationPowered = true;

    protected FossilAnalyzerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initEnergy(BlockPos pos, BlockState state, MultiblockStructureBuilder multiblockBuilder, BlockEntityType<?> type, CallbackInfo ci) {
        if (!FossilAnalyzerEnergy.usesEnergy(state)) {
            return;
        }

        cobblemoncharged_neoforge$energyStorage = new ChargedEnergyStorage(FossilAnalyzerEnergy.capacityInt(), this::setChanged);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void readEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (!FossilAnalyzerEnergy.usesEnergy(getBlockState())) {
            return;
        }

        cobblemoncharged_neoforge$getEnergyStorageInternal().setStoredEnergy(tag.getLong(FossilAnalyzerEnergy.NBT_ENERGY_KEY));
        cobblemoncharged_neoforge$restorationPowered = FossilAnalyzerEnergy.readPowered(tag);
    }

    @Override
    public IEnergyStorage getEnergyStorage() {
        return cobblemoncharged_neoforge$getEnergyStorageInternal();
    }

    @Override
    public boolean isRestorationPowered() {
        return cobblemoncharged_neoforge$restorationPowered;
    }

    @Override
    public boolean setRestorationPowered(boolean powered) {
        if (cobblemoncharged_neoforge$restorationPowered == powered) {
            return false;
        }

        cobblemoncharged_neoforge$restorationPowered = powered;
        setChanged();
        return true;
    }

    @Unique
    private ChargedEnergyStorage cobblemoncharged_neoforge$getEnergyStorageInternal() {
        if (cobblemoncharged_neoforge$energyStorage == null) {
            throw new IllegalStateException("Fossil analyzer energy storage was not initialized");
        }

        return cobblemoncharged_neoforge$energyStorage;
    }

    @Unique
    private static final class ChargedEnergyStorage extends EnergyStorage {
        private final Runnable onChanged;

        private ChargedEnergyStorage(int capacity, Runnable onChanged) {
            super(capacity, capacity, capacity);
            this.onChanged = onChanged;
        }

        private void setStoredEnergy(long amount) {
            energy = (int) FossilAnalyzerEnergy.clampStoredEnergy(amount);
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
            int extracted = super.extractEnergy(toExtract, simulate);
            if (!simulate && extracted > 0) {
                onChanged.run();
            }
            return extracted;
        }
    }
}
