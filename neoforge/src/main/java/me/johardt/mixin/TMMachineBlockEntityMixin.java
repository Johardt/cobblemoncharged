package me.johardt.mixin;

import com.cobblemon.mod.common.block.entity.TMMachineBlockEntity;
import me.johardt.energy.PoweredTMMachine;
import me.johardt.energy.TMMachineEnergy;
import me.johardt.energy.TMMachineEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TMMachineBlockEntity.class)
public abstract class TMMachineBlockEntityMixin extends BlockEntity implements PoweredTMMachine {
    @Unique
    private TMMachineEnergyStorage cobblemoncharged_neoforge$energyStorage;

    protected TMMachineBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initEnergy(BlockPos pos, BlockState state, CallbackInfo ci) {
        cobblemoncharged_neoforge$energyStorage = new TMMachineEnergyStorage(TMMachineEnergy.capacityInt(), this::setChanged);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void readEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        cobblemoncharged_neoforge$getEnergyStorage().setStoredEnergy(tag.getLong(TMMachineEnergy.NBT_KEY));
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void writeEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putInt(TMMachineEnergy.NBT_KEY, cobblemoncharged_neoforge$getEnergyStorage().getEnergyStored());
    }

    @Override
    public IEnergyStorage getEnergyStorage() {
        return cobblemoncharged_neoforge$getEnergyStorage();
    }

    @Override
    public boolean consumeProcessingEnergy() {
        return cobblemoncharged_neoforge$getEnergyStorage().extractInternally(TMMachineEnergy.energyPerProcessingTickInt());
    }

    @Unique
    private TMMachineEnergyStorage cobblemoncharged_neoforge$getEnergyStorage() {
        if (cobblemoncharged_neoforge$energyStorage == null) {
            throw new IllegalStateException("TM machine energy storage was not initialized");
        }
        return cobblemoncharged_neoforge$energyStorage;
    }
}
