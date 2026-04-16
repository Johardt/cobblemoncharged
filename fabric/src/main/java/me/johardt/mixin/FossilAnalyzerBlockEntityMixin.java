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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.reborn.energy.api.EnergyStorage;
import team.reborn.energy.api.base.SimpleEnergyStorage;

@Mixin(FossilMultiblockEntity.class)
public abstract class FossilAnalyzerBlockEntityMixin extends BlockEntity implements PoweredFossilAnalyzer {
    @Unique
    private SimpleEnergyStorage cobblemoncharged_fabric$energyStorage;

    @Unique
    private boolean cobblemoncharged_fabric$restorationPowered = true;

    protected FossilAnalyzerBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initEnergy(BlockPos pos, BlockState state, MultiblockStructureBuilder multiblockBuilder, BlockEntityType<?> type, CallbackInfo ci) {
        if (!FossilAnalyzerEnergy.usesEnergy(state)) {
            return;
        }

        long capacity = FossilAnalyzerEnergy.capacity();
        cobblemoncharged_fabric$energyStorage = new SimpleEnergyStorage(capacity, capacity, capacity) {
            @Override
            protected void onFinalCommit() {
                FossilAnalyzerBlockEntityMixin.this.setChanged();
            }
        };
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void readEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (!FossilAnalyzerEnergy.usesEnergy(getBlockState())) {
            return;
        }

        SimpleEnergyStorage energyStorage = cobblemoncharged_fabric$getEnergyStorage();
        energyStorage.amount = FossilAnalyzerEnergy.clampStoredEnergy(tag.getLong(FossilAnalyzerEnergy.NBT_ENERGY_KEY));
        cobblemoncharged_fabric$restorationPowered = FossilAnalyzerEnergy.readPowered(tag);
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return cobblemoncharged_fabric$getEnergyStorage();
    }

    @Override
    public boolean isRestorationPowered() {
        return cobblemoncharged_fabric$restorationPowered;
    }

    @Override
    public boolean setRestorationPowered(boolean powered) {
        if (cobblemoncharged_fabric$restorationPowered == powered) {
            return false;
        }

        cobblemoncharged_fabric$restorationPowered = powered;
        setChanged();
        return true;
    }

    @Unique
    private SimpleEnergyStorage cobblemoncharged_fabric$getEnergyStorage() {
        if (cobblemoncharged_fabric$energyStorage == null) {
            throw new IllegalStateException("Fossil analyzer energy storage was not initialized");
        }

        return cobblemoncharged_fabric$energyStorage;
    }
}
