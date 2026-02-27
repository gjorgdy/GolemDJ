package nl.gjorgdy.golem_disk_jockey.utils;

import net.minecraft.world.entity.Entity;

public class EntityUtils {

    public static boolean isDj(Entity entity) {
        return entity.getName().getString().contains("DJ");
    }

}
