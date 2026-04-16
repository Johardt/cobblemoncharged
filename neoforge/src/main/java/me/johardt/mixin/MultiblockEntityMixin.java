package me.johardt.mixin;

import com.cobblemon.mod.common.api.multiblock.MultiblockEntity;
import me.johardt.energy.FossilAnalyzerEnergy;
import me.johardt.energy.PoweredFossilAnalyzer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiblockEntity.class)
public abstract class MultiblockEntityMixin extends BlockEntity {
    protected MultiblockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void writeFossilEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (!(this instanceof PoweredFossilAnalyzer analyzer) || !FossilAnalyzerEnergy.usesEnergy(getBlockState())) {
            return;
        }

        IEnergyStorage energyStorage = analyzer.getEnergyStorage();
        tag.putLong(FossilAnalyzerEnergy.NBT_ENERGY_KEY, energyStorage.getEnergyStored());
        tag.putBoolean(FossilAnalyzerEnergy.NBT_POWERED_KEY, analyzer.isRestorationPowered());
    }
}
