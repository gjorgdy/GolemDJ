package nl.gjorgdy.golem_disc_jockey.utils;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class ContainerUtils {

    public static ItemStack pickupDiscFromContainer(Container container) {
        int i = 0;
        for(ItemStack itemStack : container) {
            if (!itemStack.isEmpty() && ItemUtils.isMusicDisc(itemStack)) {
                return container.removeItem(i, 1);
            }
            ++i;
        }
        return ItemStack.EMPTY;
    }

}
