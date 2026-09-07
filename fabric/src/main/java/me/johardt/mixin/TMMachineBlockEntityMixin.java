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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.reborn.energy.api.EnergyStorage;

@Mixin(TMMachineBlockEntity.class)
public abstract class TMMachineBlockEntityMixin extends BlockEntity implements PoweredTMMachine {
    @Unique
    private TMMachineEnergyStorage cobblemoncharged_fabric$energyStorage;

    protected TMMachineBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initEnergy(BlockPos pos, BlockState state, CallbackInfo ci) {
        long capacity = TMMachineEnergy.capacity();
        cobblemoncharged_fabric$energyStorage = new TMMachineEnergyStorage(capacity, this::setChanged);
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"))
    private void readEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        cobblemoncharged_fabric$getEnergyStorage().setStoredEnergy(tag.getLong(TMMachineEnergy.NBT_KEY));
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"))
    private void writeEnergy(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        tag.putLong(TMMachineEnergy.NBT_KEY, cobblemoncharged_fabric$getEnergyStorage().getAmount());
    }

    @Override
    public EnergyStorage getEnergyStorage() {
        return cobblemoncharged_fabric$getEnergyStorage();
    }

    @Override
    public boolean consumeProcessingEnergy() {
        return cobblemoncharged_fabric$getEnergyStorage().extractInternally(TMMachineEnergy.energyPerProcessingTick());
    }

    @Unique
    private TMMachineEnergyStorage cobblemoncharged_fabric$getEnergyStorage() {
        if (cobblemoncharged_fabric$energyStorage == null) {
            throw new IllegalStateException("TM machine energy storage was not initialized");
        }
        return cobblemoncharged_fabric$energyStorage;
    }
}
