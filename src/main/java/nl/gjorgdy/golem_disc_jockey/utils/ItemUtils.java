package nl.gjorgdy.golem_disc_jockey.utils;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

public abstract class ItemUtils {

    public static boolean isMusicDisc(ItemStack stack) {
        return stack.get(DataComponents.JUKEBOX_PLAYABLE) != null;
    }

}
