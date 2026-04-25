package me.johardt;

import com.mojang.logging.LogUtils;
import me.johardt.energy.CobblemonChargedEnergy;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;

public final class CobblemonCharged implements ModInitializer {
    public static final String MODID = "cobblemoncharged";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        try {
            CobblemonChargedConfig.load(FabricLoader.getInstance().getConfigDir());
        } catch (IOException exception) {
            LOGGER.warn("Failed to load Cobblemon: Charged config. Defaults will be used.", exception);
        }

        CobblemonChargedEnergy.initialize();
    }
}
