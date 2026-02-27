package nl.gjorgdy.golem_disc_jockey.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import nl.gjorgdy.golem_disc_jockey.GolemDiscJockey;
import nl.gjorgdy.golem_disc_jockey.utils.ContainerUtils;
import nl.gjorgdy.golem_disc_jockey.utils.ItemUtils;
import nl.gjorgdy.golem_disc_jockey.utils.EntityUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.TransportItemsBetweenContainers;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.JukeboxBlockEntity;

@Mixin(TransportItemsBetweenContainers.class)
public abstract class TransportItemsBetweenContainersMixin {

    @Shadow
    protected abstract int getHorizontalSearchDistance(PathfinderMob entity);

    @Shadow
    @Nullable
    private TransportItemsBetweenContainers.TransportItemTarget target;

    @Inject(method = "getTransportTarget", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"), cancellable = true)
    public void onFindStorage(ServerLevel world, PathfinderMob pathfinderMob, CallbackInfoReturnable<Optional<TransportItemsBetweenContainers.TransportItemTarget>> cir) {
        if (pathfinderMob instanceof CopperGolem copperGolem && ItemUtils.isMusicDisc(copperGolem.getMainHandItem())) {
            var optJukebox = this.findJukebox(world, copperGolem);
            if (optJukebox.isPresent()) {
                cir.setReturnValue(optJukebox);
                cir.cancel();
            }
            // If no jukebox found, don't sort discs
            else if (!GolemDiscJockey.shouldSortDiscs || EntityUtils.isDj(copperGolem)) {
                cir.setReturnValue(Optional.empty());
                cir.cancel();
            }
        }
    }

    @WrapOperation(method = "pickUpItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;pickupItemFromContainer(Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack onPickupItem(Container container, Operation<ItemStack> original, @Local(argsOnly = true) PathfinderMob pathfinderMob) {
        // to prevent an edge case where the golems target is still a jukebox, so it takes the disc out
        if (container instanceof JukeboxBlockEntity) {
            return ItemStack.EMPTY;
        }
        if (EntityUtils.isDj(pathfinderMob)) {
            return ContainerUtils.pickupDiscFromContainer(container);
        }
        return original.call(container);
    }

    @Inject(method = "hasValidTarget", at = @At("TAIL"), cancellable = true)
    public void hasValidStorage(Level world, PathfinderMob entity, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(
            cir.getReturnValue() || (this.target != null && this.target.blockEntity() instanceof JukeboxBlockEntity)
        );
    }

    @WrapOperation(method = "putDownItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;addItemsToContainer(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onSetStack(PathfinderMob pathfinderMob, Container container, Operation<ItemStack> original) {
        if (container instanceof JukeboxBlockEntity jukeboxBlockEntity) {
            var golemItem = pathfinderMob.getMainHandItem();
            if (jukeboxBlockEntity.getTheItem() == ItemStack.EMPTY) {
                var jukeboxItem = jukeboxBlockEntity.getItem(0);
                if (jukeboxItem.isEmpty()) {
                    jukeboxBlockEntity.setItem(0, golemItem.split(1));
                }
            }
            return golemItem;
        }
        return original.call(pathfinderMob, container);
    }

    @Unique
    private Optional<TransportItemsBetweenContainers.TransportItemTarget> findJukebox(ServerLevel world, CopperGolem entity) {
        Stream<ChunkPos> chunkPosStream = ChunkPos.rangeClosed(new ChunkPos(entity.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance(entity), 16) + 1);
        // Find the nearest empty jukebox
        var jukebox = chunkPosStream
                .map(chunkPos -> world.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z))
                .filter(Objects::nonNull)
                .flatMap(worldChunk -> worldChunk.getBlockEntities().values().stream())
                .filter(blockEntity -> blockEntity instanceof JukeboxBlockEntity jbe && (jbe.isEmpty() || GolemDiscJockey.shouldWaitAtJukebox))
                .min(Comparator.comparingDouble(a -> a.getBlockPos().distSqr(entity.blockPosition())));
        // If found, return the storage
        if (jukebox.isPresent() && jukebox.get() instanceof JukeboxBlockEntity jukeboxBlockEntity) {
            return Optional.of(
                new TransportItemsBetweenContainers.TransportItemTarget(jukeboxBlockEntity.getBlockPos(), jukeboxBlockEntity, jukeboxBlockEntity, jukeboxBlockEntity.getBlockState())
            );
        }
        // Otherwise return empty
        return Optional.empty();
    }

}