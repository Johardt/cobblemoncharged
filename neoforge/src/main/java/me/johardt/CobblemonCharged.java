package me.johardt;

import com.mojang.logging.LogUtils;
import me.johardt.energy.CobblemonChargedCapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;

@Mod(CobblemonCharged.MODID)
public final class CobblemonCharged {
    public static final String MODID = "cobblemoncharged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobblemonCharged(IEventBus modEventBus, ModContainer modContainer) {
        try {
            CobblemonChargedConfig.load(FMLPaths.CONFIGDIR.get());
        } catch (IOException exception) {
            LOGGER.warn("Failed to load Cobblemon: Charged config. Defaults will be used.", exception);
        }

        modEventBus.addListener(CobblemonChargedCapabilities::registerCapabilities);
    }
}
