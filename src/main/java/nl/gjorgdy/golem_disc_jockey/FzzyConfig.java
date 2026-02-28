package nl.gjorgdy.golem_disc_jockey;

import me.fzzyhmstrs.fzzy_config.annotations.Comment;
import me.fzzyhmstrs.fzzy_config.annotations.IgnoreVisibility;
import me.fzzyhmstrs.fzzy_config.api.ConfigApi;
import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;
import me.fzzyhmstrs.fzzy_config.config.Config;
import net.minecraft.resources.Identifier;

@IgnoreVisibility
public class FzzyConfig extends Config {

    static {
        ConfigApi.event().onSyncServer((a, b) -> FzzyConfig.load());
        ConfigApi.event().onSyncClient((a, b) -> FzzyConfig.load());
    }

    public static void load() {
        var config = ConfigApiJava.registerAndLoadConfig(FzzyConfig::new);
        GolemDiscJockey.shouldUseJukebox = config.useJukebox;
        GolemDiscJockey.shouldWaitAtJukebox = config.waitAtJukebox;
        GolemDiscJockey.shouldSortDiscIfNoJukebox = config.sortDiscIfNoJukebox;
    }

    private FzzyConfig() {
        super(Identifier.fromNamespaceAndPath(GolemDiscJockey.MOD_ID, "config"));
    }

    @Comment("Whether all golems should try to put a disc into a jukebox.")
    private boolean useJukebox = GolemDiscJockey.shouldUseJukebox;

    @Comment("Whether all golems should wait at a jukebox if it can't put the disc in.")
    private boolean waitAtJukebox = GolemDiscJockey.shouldWaitAtJukebox;

    @Comment("Whether a golem should sort a disc into a chest if it can't find a jukebox to put it in.")
    private boolean sortDiscIfNoJukebox = GolemDiscJockey.shouldSortDiscIfNoJukebox;

}
