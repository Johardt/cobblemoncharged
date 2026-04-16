package me.johardt.energy;

import com.cobblemon.mod.common.block.FossilAnalyzerBlock;
import com.cobblemon.mod.common.block.RestorationTankBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class FossilAnalyzerBlockStateHelper {
    private FossilAnalyzerBlockStateHelper() {
    }

    public static void setMachinePowered(Level level, BlockPos analyzerPos, BlockPos tankBasePos, boolean powered) {
        setPowered(level, analyzerPos, FossilAnalyzerBlock.Companion.getON(), powered);
        setPowered(level, tankBasePos.above(), RestorationTankBlock.Companion.getON(), powered);
    }

    private static void setPowered(Level level, BlockPos pos, BooleanProperty property, boolean powered) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(property) || state.getValue(property) == powered) {
            return;
        }

        level.setBlockAndUpdate(pos, state.setValue(property, powered));
    }
}
