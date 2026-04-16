package me.johardt;

import com.mojang.logging.LogUtils;
import me.johardt.energy.CobblemonChargedEnergy;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;

public final class CobblemonCharged implements ModInitializer {
    public static final String MODID = "cobblemoncharged";
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
        CobblemonChargedEnergy.initialize();
    }
}
