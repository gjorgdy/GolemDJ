package nl.gjorgdy.golem_disc_jockey;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class GolemDiscJockey implements ModInitializer {

    public static final String MOD_NAME = "Golem DJ";
    public static final String MOD_ID = "golem_disc_jockey";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static boolean shouldSortDiscs = false;
    public static boolean shouldWaitAtJukebox = true;

    @Override
    public void onInitialize() {
        loadConfig();
    }

    public static void loadConfig() {
        if (FabricLoader.getInstance().isModLoaded("fzzy_config")) {
            var config = FzzyConfig.load();
            GolemDiscJockey.shouldSortDiscs = config.shouldSortDiscs;
            GolemDiscJockey.shouldWaitAtJukebox = config.shouldWaitAtJukebox;
        } else {
            LOGGER.log(Level.INFO, "Fzzy Config not found, using default settings.");
        }
    }
}
