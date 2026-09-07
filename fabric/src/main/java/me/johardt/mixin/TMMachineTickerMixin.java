package me.johardt.mixin;

import com.cobblemon.mod.common.block.entity.TMMachineBlockEntity;
import me.johardt.energy.PoweredTMMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TMMachineBlockEntity.Companion.class)
public abstract class TMMachineTickerMixin {
    @Inject(method = "serverTick", at = @At("HEAD"), cancellable = true, remap = false)
    private void requireEnergy(Level level, BlockPos pos, BlockState state, TMMachineBlockEntity blockEntity, CallbackInfo ci) {
        if (blockEntity.getBurnActive()
            && blockEntity.getBurnProgress() < TMMachineBlockEntity.BURN_TOTAL_TIME
            && !((PoweredTMMachine) (Object) blockEntity).consumeProcessingEnergy()) {
            ci.cancel();
        }
    }
}
