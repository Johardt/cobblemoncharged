package me.johardt.mixin;

import com.cobblemon.mod.common.block.multiblock.FossilMultiblockStructure;
import me.johardt.energy.FossilAnalyzerBlockStateHelper;
import me.johardt.energy.FossilAnalyzerEnergy;
import me.johardt.energy.PoweredFossilAnalyzer;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FossilMultiblockStructure.class)
public abstract class FossilMultiblockStructureMixin {
    @Shadow(remap = false)
    @Final
    private BlockPos analyzerPos;

    @Shadow(remap = false)
    @Final
    private BlockPos tankBasePos;

    @Shadow(remap = false)
    private int timeRemaining;

    @Shadow(remap = false)
    public abstract void updateOnStatus(Level level);

    @Shadow(remap = false)
    public abstract void syncToClient(Level level);

    @Shadow(remap = false)
    public abstract void markDirty(Level level);

    @Inject(method = "startMachine", at = @At("HEAD"), remap = false)
    private void resetPoweredOnStart(Level level, CallbackInfo ci) {
        PoweredFossilAnalyzer analyzer = cobblemoncharged_fabric$getAnalyzer(level);
        if (analyzer != null) {
            analyzer.setRestorationPowered(true);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, remap = false)
    private void requireContinuousPower(Level level, CallbackInfo ci) {
        if (timeRemaining <= 0) {
            return;
        }

        PoweredFossilAnalyzer analyzer = cobblemoncharged_fabric$getAnalyzer(level);
        if (analyzer == null) {
            return;
        }

        if (level.isClientSide) {
            if (!analyzer.isRestorationPowered()) {
                FossilAnalyzerBlockStateHelper.setMachinePowered(level, analyzerPos, tankBasePos, false);
                ci.cancel();
            }
            return;
        }

        try (Transaction transaction = Transaction.openOuter()) {
            long energyPerTick = FossilAnalyzerEnergy.energyPerRestorationTick();
            long extracted = analyzer.getEnergyStorage().extract(energyPerTick, transaction);
            if (extracted == energyPerTick) {
                transaction.commit();
                if (analyzer.setRestorationPowered(true)) {
                    updateOnStatus(level);
                    syncToClient(level);
                    markDirty(level);
                }
                return;
            }
        }

        if (analyzer.setRestorationPowered(false)) {
            FossilAnalyzerBlockStateHelper.setMachinePowered(level, analyzerPos, tankBasePos, false);
            syncToClient(level);
            markDirty(level);
        }
        ci.cancel();
    }

    @Unique
    private PoweredFossilAnalyzer cobblemoncharged_fabric$getAnalyzer(Level level) {
        BlockEntity blockEntity = level.getBlockEntity(analyzerPos);
        if (blockEntity instanceof PoweredFossilAnalyzer analyzer) {
            return analyzer;
        }

        return null;
    }
}
