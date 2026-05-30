package nl.gjorgdy.golem_disc_jockey.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
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
import java.util.function.BiConsumer;
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
    protected abstract int getHorizontalSearchDistance(PathfinderMob mob);

    @Shadow
    @Nullable
    private TransportItemsBetweenContainers.TransportItemTarget target;

    @Shadow
    protected abstract boolean isPositionAlreadyVisited(Set<GlobalPos> visitedPositions, Set<GlobalPos> unreachablePositions, TransportItemsBetweenContainers.TransportItemTarget target, Level level);

    @Shadow
    private static Set<GlobalPos> getVisitedPositions(PathfinderMob mob) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private static Set<GlobalPos> getUnreachablePositions(PathfinderMob mob) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "getTransportTarget", at = @At(value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"), cancellable = true)
    public void onFindStorage(ServerLevel level, PathfinderMob body, CallbackInfoReturnable<Optional<TransportItemsBetweenContainers.TransportItemTarget>> cir) {
        var isDj = EntityUtils.isDj(body);
        if (!(GolemDiscJockey.shouldUseJukebox || isDj)) return;
        if (body instanceof CopperGolem copperGolem && ItemUtils.isMusicDisc(copperGolem.getMainHandItem())) {
            var optJukebox = this.findJukebox(level, copperGolem);
            if (optJukebox.isPresent()) {
                cir.setReturnValue(optJukebox);
                cir.cancel();
            }
            // If no jukebox found, don't sort discs
            else if (!GolemDiscJockey.shouldSortDiscIfNoJukebox || isDj) {
                cir.setReturnValue(Optional.empty());
                cir.cancel();
            }
        }
    }

    @WrapOperation(method = "pickUpItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;pickupItemFromContainer(Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack onPickupItem(Container container, Operation<ItemStack> original, @Local(argsOnly = true, name = "body") PathfinderMob body) {
        // to prevent an edge case where the golems target is still a jukebox, so it takes the disc out
        if (container instanceof JukeboxBlockEntity) {
            return ItemStack.EMPTY;
        }
        if (EntityUtils.isDj(body)) {
            return ContainerUtils.pickupDiscFromContainer(container);
        }
        return original.call(container);
    }

    @WrapMethod(method = "isWantedBlock")
    public boolean isWantedBlock(PathfinderMob mob, BlockState block, Operation<Boolean> original) {
        if (block.is(Blocks.JUKEBOX) && mob instanceof CopperGolem) return true;
        return original.call(mob, block);
    }

    @WrapMethod(method = "doReachedTargetInteraction")
    private void onReachedTargetInteraction(PathfinderMob body, Container container, BiConsumer<PathfinderMob, Container> onPickupSuccess, BiConsumer<PathfinderMob, Container> onPickupFailure, BiConsumer<PathfinderMob, Container> onPlaceSuccess, BiConsumer<PathfinderMob, Container> onPlaceFailure, Operation<Void> original) {
        if ((GolemDiscJockey.shouldWaitAtJukebox || EntityUtils.isDj(body))
                && target != null && target.blockEntity() instanceof JukeboxBlockEntity jukeboxBlockEntity
                && jukeboxBlockEntity.getTheItem() != ItemStack.EMPTY
                && body.level().getRandom().nextIntBetweenInclusive(0, 4) % 4 != 0
        ) {
            // If the target is a jukebox with a disc in it, sometimes do the interaction.
            return;
        }
        original.call(body, container, onPickupSuccess, onPickupFailure, onPlaceSuccess, onPlaceFailure);
    }

    @WrapOperation(method = "putDownItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/behavior/TransportItemsBetweenContainers;addItemsToContainer(Lnet/minecraft/world/entity/PathfinderMob;Lnet/minecraft/world/Container;)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack onSetStack(PathfinderMob body, Container container, Operation<ItemStack> original) {
        if (container instanceof JukeboxBlockEntity jukeboxBlockEntity) {
            var golemItem = body.getMainHandItem();
            if (jukeboxBlockEntity.getTheItem() == ItemStack.EMPTY) {
                var jukeboxItem = jukeboxBlockEntity.getItem(0);
                if (jukeboxItem.isEmpty()) {
                    jukeboxBlockEntity.setItem(0, golemItem.split(1));
                }
            }
            return golemItem;
        }
        return original.call(body, container);
    }

    @Unique
    private Optional<TransportItemsBetweenContainers.TransportItemTarget> findJukebox(ServerLevel level, CopperGolem entity) {
        Stream<ChunkPos> chunkPosStream = ChunkPos.rangeClosed(ChunkPos.containing(entity.blockPosition()), Math.floorDiv(this.getHorizontalSearchDistance(entity), 16) + 1);
        // Find the nearest empty jukebox
        boolean golemShouldWait = shouldWaitAtJukebox(entity);
        var visitedPositions = getVisitedPositions(entity);
        var unreachablePositions = getUnreachablePositions(entity);
        return chunkPosStream
                .map(chunkPos -> level.getChunkSource().getChunkNow(chunkPos.x(), chunkPos.z()))
                .filter(Objects::nonNull)
                .flatMap(worldChunk -> worldChunk.getBlockEntities().values().stream())
                .filter(blockEntity -> blockEntity instanceof JukeboxBlockEntity jbe && (jbe.isEmpty() || golemShouldWait))
                .map(jukeboxBlockEntity -> new TransportItemsBetweenContainers.TransportItemTarget(jukeboxBlockEntity.getBlockPos(), (JukeboxBlockEntity) jukeboxBlockEntity, jukeboxBlockEntity, jukeboxBlockEntity.getBlockState()))
                .filter(tit -> !this.isPositionAlreadyVisited(visitedPositions, unreachablePositions, tit, level) || golemShouldWait)
                .min(Comparator.comparingDouble(a -> a.blockEntity().getBlockPos().distSqr(entity.blockPosition())));
    }

    @Unique
    private boolean shouldWaitAtJukebox(CopperGolem entity) {
        return GolemDiscJockey.shouldWaitAtJukebox || EntityUtils.isDj(entity);
    }

}