package me.johardt;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CobblemonChargedConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "cobblemoncharged.json";
    private static CobblemonChargedConfig INSTANCE = defaults();

    private HealingMachine healingMachine = new HealingMachine();
    private FossilAnalyzer fossilAnalyzer = new FossilAnalyzer();

    private CobblemonChargedConfig() {
    }

    public static void load(Path configDirectory) throws IOException {
        Files.createDirectories(configDirectory);

        Path configPath = configDirectory.resolve(FILE_NAME);
        if (Files.notExists(configPath)) {
            INSTANCE = defaults();
            write(configPath, INSTANCE);
            return;
        }

        try (Reader reader = Files.newBufferedReader(configPath)) {
            CobblemonChargedConfig loaded = GSON.fromJson(reader, CobblemonChargedConfig.class);
            INSTANCE = loaded == null ? defaults() : loaded.validated();
        } catch (JsonSyntaxException exception) {
            INSTANCE = defaults();
            throw new IOException("Could not parse " + configPath + ". Using default values for this run.", exception);
        }
    }

    public static int healingMachineEnergyPerCharge() {
        return INSTANCE.healingMachine.energyPerCharge;
    }

    public static long fossilAnalyzerEnergyPerRestorationTick() {
        return INSTANCE.fossilAnalyzer.energyPerRestorationTick;
    }

    private static CobblemonChargedConfig defaults() {
        return new CobblemonChargedConfig().validated();
    }

    private static void write(Path configPath, CobblemonChargedConfig config) throws IOException {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            GSON.toJson(config, writer);
        }
    }

    private CobblemonChargedConfig validated() {
        if (healingMachine == null) {
            healingMachine = new HealingMachine();
        }
        healingMachine.validated();

        if (fossilAnalyzer == null) {
            fossilAnalyzer = new FossilAnalyzer();
        }
        fossilAnalyzer.validated();

        return this;
    }

    private static final class HealingMachine {
        private int energyPerCharge = 4_000;

        private void validated() {
            energyPerCharge = Math.clamp(energyPerCharge, 1, Integer.MAX_VALUE);
        }
    }

    private static final class FossilAnalyzer {
        private long energyPerRestorationTick = 20L;

        private void validated() {
            energyPerRestorationTick = Math.clamp(energyPerRestorationTick, 1L, Integer.MAX_VALUE);
        }
    }
}
