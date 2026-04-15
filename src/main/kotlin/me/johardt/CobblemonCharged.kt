package me.johardt

import com.mojang.logging.LogUtils
import me.johardt.energy.CobblemonChargedCapabilities
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.config.ModConfig
import org.slf4j.Logger

@Mod(CobblemonCharged.MODID)
class CobblemonCharged(modEventBus: IEventBus, modContainer: ModContainer) {
    init {
        modEventBus.addListener(CobblemonChargedCapabilities::registerCapabilities)
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }

    companion object {
        const val MODID: String = "cobblemoncharged"

        @JvmField
        val LOGGER: Logger = LogUtils.getLogger()
    }
}
