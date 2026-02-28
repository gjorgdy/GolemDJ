package nl.gjorgdy.golem_disc_jockey;

import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.Identifier;

public class FzzyConfig extends Config {

    static {
        ConfigApi.event().onSyncServer((a, b) -> {
            GolemDiscJockey.loadConfig();
        });
        ConfigApi.event().onSyncClient((a, b) -> {
            GolemDiscJockey.loadConfig();
        });
    }

    public static FzzyConfig load() {
        return ConfigApiJava.registerAndLoadConfig(FzzyConfig::new);
    }

    private FzzyConfig() {
        super(Identifier.fromNamespaceAndPath(GolemDiscJockey.MOD_ID, "config"));
    }

    @Comment("Whether all golems should try to put a disc into a jukebox.")
    public boolean useJukebox = GolemDiscJockey.shouldUseJukebox;

    @Comment("Whether all golems should wait at a jukebox if it can't put the disc in.")
    public boolean waitAtJukebox = GolemDiscJockey.shouldWaitAtJukebox;

    @Comment("Whether a golem should sort a disc into a chest if it can't find a jukebox to put it in.")
    public boolean sortDiscIfNoJukebox = GolemDiscJockey.shouldSortDiscIfNoJukebox;

}
