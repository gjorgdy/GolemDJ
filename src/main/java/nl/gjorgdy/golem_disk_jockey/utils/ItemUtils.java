package nl.gjorgdy.golem_disk_jockey.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;

public abstract class ItemUtils {

    public static boolean isMusicDisc(ServerLevel world, ItemStack stack) {
        return JukeboxSong.fromStack(world.registryAccess(), stack).isPresent();
    }

}
