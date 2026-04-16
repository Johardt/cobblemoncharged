package me.johardt;

import com.mojang.logging.LogUtils;
import me.johardt.energy.CobblemonChargedCapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(CobblemonCharged.MODID)
public final class CobblemonCharged {
    public static final String MODID = "cobblemoncharged";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CobblemonCharged(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(CobblemonChargedCapabilities::registerCapabilities);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
