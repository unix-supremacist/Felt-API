package net.feltmc.feltapi.api.tool;

import com.mojang.datafixers.util.Pair;
import net.feltmc.feltapi.FeltAPI;
import net.feltmc.feltapi.mixin.tool.AxeMixin;
import net.feltmc.feltapi.mixin.tool.HoeMixin;
import net.feltmc.feltapi.mixin.tool.ShovelMixin;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class FeltToolHelper {
    public static ArrayList<BlockPos> getArea(BlockPos anchor, BlockPos negative, BlockPos positive){
        ArrayList<BlockPos> blocks = new ArrayList<>();

        for(int x = negative.getX(); x <= positive.getX(); x++)
            for (int y = negative.getY(); y <= positive.getY(); y++)
                for (int z = negative.getZ(); z <= positive.getZ(); z++)
                    blocks.add(anchor.offset(x, y, z));
        return blocks;
    }

    public static ArrayList<BlockPos> getAreaFromFacing(Direction dir, BlockPos anchor, int size, int depth){
        switch(dir) {
            case SOUTH:
                return getArea(anchor, new BlockPos(-size, -size, -depth), new BlockPos(size, size, 0));
            case NORTH:
                return getArea(anchor, new BlockPos(-size, -size, 0), new BlockPos(size, size, depth));
            case EAST:
                return getArea(anchor, new BlockPos(-depth, -size, -size), new BlockPos(0, size, size));
            case WEST:
                return getArea(anchor, new BlockPos(0, -size, -size), new BlockPos(depth, size, size));
            case UP:
                return getArea(anchor, new BlockPos(-size, -depth, -size), new BlockPos(size, 0, size));
            case DOWN:
                return getArea(anchor, new BlockPos(-size, 0, -size), new BlockPos(size, depth, size));
            default:
                FeltAPI.LOGGER.error("HOW DID YOU CLICK A DIRECTION THAT ISN'T NORTH SOUTH EAST WEST UP OR DOWN");
                return null;
        }
    }

    public static void destroy(Level lvl, BlockPos pos, LivingEntity entity){
        if(!lvl.isClientSide() && !(lvl.getBlockState(pos).getBlock().defaultDestroyTime() < 0))
            lvl.destroyBlock(pos, entity instanceof Player ? !((Player) entity).isCreative() : true);
    }

    public static InteractionResult destroyArea(Level lvl, ArrayList<BlockPos> blocks, LivingEntity entity){
        if(!lvl.isClientSide() && !blocks.isEmpty()){
            for (BlockPos b : blocks) destroy(lvl, b, entity);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult destroyAreaFromFacing(Level lvl, LivingEntity entity, BlockPos anchor, int size, int depth){
        Vec3 look = entity.getLookAngle();
        ArrayList<BlockPos> blocks = getAreaFromFacing(Direction.getNearest(look.x(), look.y(), look.z()), anchor, size, depth);
        return destroyArea(lvl, blocks, entity);
    }

    public static InteractionResult strip(UseOnContext ctx){
        Level lvl = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Player player = ctx.getPlayer();
        BlockState state = lvl.getBlockState(pos);
        ItemStack item = ctx.getItemInHand();
        Optional<BlockState> strippedState = getStrippedState(state);
        Optional<BlockState> waxedBlocks = Optional.ofNullable((Block) HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
        if (strippedState.isPresent()) {
            lvl.playSound(player, pos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (WeatheringCopper.getPrevious(state).isPresent()) {
            lvl.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, pos, 0);
            lvl.playSound(player, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (waxedBlocks.isPresent()) {
            lvl.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, pos, 0);
            lvl.playSound(player, pos, SoundEvents.AXE_WAX_OFF, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer) CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger((ServerPlayer)player, pos, item);
        lvl.setBlock(pos, strippedState.get(), Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        if (player != null) item.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
        return InteractionResult.sidedSuccess(lvl.isClientSide);
    }

    public static boolean canStrip(UseOnContext ctx){
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        Optional<BlockState> waxedBlocks = Optional.ofNullable((Block) HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
        return getStrippedState(state).isPresent() || WeatheringCopper.getPrevious(state).isPresent() || waxedBlocks.isPresent();
    }

    public static Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(AxeMixin.getStripped().get(state.getBlock())).map(block -> (BlockState)block.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS)));
    }

    public static InteractionResult torch(UseOnContext ctx){
        Player player = ctx.getPlayer();
        BlockPlaceContext blockPlaceCtx = new BlockPlaceContext(ctx);
        Level lvl = ctx.getLevel();
        BlockPos pos = blockPlaceCtx.getClickedPos();
        BlockState state = getTorchedState(blockPlaceCtx);
        if (canTorch(ctx)){
            if (ctx.getLevel().setBlock(blockPlaceCtx.getClickedPos(), state, 11)) {
                SoundType blockSoundGroup = state.getSoundType();
                lvl.playSound(player, pos, blockSoundGroup.getPlaceSound(), SoundSource.BLOCKS, (blockSoundGroup.getVolume() + 1.0F) / 2.0F, blockSoundGroup.getPitch() * 0.8F);
                if (player == null || !player.getAbilities().instabuild) removeTorch(player.getInventory());
            }
        }
        return InteractionResult.SUCCESS;
    }

    public static BlockState getTorchedState(BlockPlaceContext ctx) {
        BlockState state = null;
        Level lvl = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction[] directions = ctx.getNearestLookingDirections();

        for (int i = 0; i < directions.length; i++) {
            Direction direction = directions[i];
            if (direction != Direction.UP) {
                BlockState face = direction == Direction.DOWN ? Blocks.TORCH.getStateForPlacement(ctx) : Blocks.WALL_TORCH.getStateForPlacement(ctx);
                if (face != null && face.canSurvive(lvl, pos)) {
                    state = face;
                    break;
                }
            }
        }
        return state != null && lvl.isUnobstructed(state, pos, CollisionContext.empty()) ? state : null;
    }

    public static boolean canTorch(UseOnContext ctx){
        Inventory inv = ctx.getPlayer().getInventory();
        if (getTorchedState(new BlockPlaceContext(ctx)) != null){
            for (int i = 0; i < inv.getContainerSize(); i++) {
                if (inv.getItem(i).getItem() == Items.TORCH) return true;
            }
        }
        return false;
    }

    public static void removeTorch(Inventory inv){
        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (inv.getItem(i).getItem() == Items.TORCH) {
                inv.getItem(i).shrink(1);
                break;
            }
        }
    }

    public static InteractionResult till(UseOnContext ctx){
        Player player = ctx.getPlayer();
        if (getTilledPair(ctx) == null) return InteractionResult.PASS;
        if (getTilledPair(ctx).getFirst().test(ctx)) {
            ctx.getLevel().playSound(player, ctx.getClickedPos(), SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (!ctx.getLevel().isClientSide) {
                getTilledPair(ctx).getSecond().accept(ctx);
                if (player != null) ctx.getItemInHand().hurtAndBreak(1, player, p -> p.broadcastBreakEvent(ctx.getHand()));
            }
            return InteractionResult.sidedSuccess(ctx.getLevel().isClientSide);
        }
        return InteractionResult.PASS;
    }

    public static Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> getTilledPair(UseOnContext ctx){
        return HoeMixin.getTilled().get(ctx.getLevel().getBlockState(ctx.getClickedPos()).getBlock());
    }

    public static InteractionResult path(UseOnContext ctx){
        Level lvl = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = lvl.getBlockState(pos);
        if (ctx.getClickedFace() != Direction.DOWN) {
            BlockState blockState2 = getPathedState(ctx);
            BlockState blockState3 = null;
            if (blockState2 != null && lvl.getBlockState(pos.above()).isAir()) {
                lvl.playSound(ctx.getPlayer(), pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0f, 1.0f);
                blockState3 = blockState2;
            } else if (state.getBlock() instanceof CampfireBlock && state.getValue(CampfireBlock.LIT).booleanValue()) {
                if (!lvl.isClientSide()) lvl.levelEvent(null, LevelEvent.SOUND_EXTINGUISH_FIRE, pos, 0);
                CampfireBlock.dowse(ctx.getPlayer(), lvl, pos, state);
                blockState3 = (BlockState)state.setValue(CampfireBlock.LIT, false);
            }
            if (blockState3 != null) {
                if (!lvl.isClientSide) {
                    lvl.setBlock(pos, blockState3, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
                    if (ctx.getPlayer() != null) ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), p -> p.broadcastBreakEvent(ctx.getHand()));
                }
                return InteractionResult.sidedSuccess(lvl.isClientSide);
            }
        }
        return InteractionResult.PASS;
    }

    public static BlockState getPathedState(UseOnContext ctx){
        return ShovelMixin.getPathed().get(ctx.getLevel().getBlockState(ctx.getClickedPos()).getBlock());
    }

    public static InteractionResult light(UseOnContext ctx){
        Player player = ctx.getPlayer();
        Level lvl = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = lvl.getBlockState(pos);
        BlockPos rel = pos.relative(ctx.getClickedFace());
        if (!CampfireBlock.canLight(state) && !CandleBlock.canLight(state) && !CandleCakeBlock.canLight(state)) {
            if (BaseFireBlock.canBePlacedAt(lvl, rel, ctx.getHorizontalDirection())) {
                fire(ctx, BaseFireBlock.getState(lvl, rel), rel);
                if (player instanceof ServerPlayer) CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayer)player, rel, ctx.getItemInHand());
                return InteractionResult.sidedSuccess(lvl.isClientSide());
            }
            return InteractionResult.FAIL;
        }
        fire(ctx, state.setValue(BlockStateProperties.LIT, true), pos);
        return InteractionResult.sidedSuccess(lvl.isClientSide());
    }

    public static void fire(UseOnContext ctx, BlockState state, BlockPos pos){
        Player player = ctx.getPlayer();
        Level lvl = ctx.getLevel();
        lvl.playSound(player, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, lvl.getRandom().nextFloat() * 0.4F + 0.8F);
        lvl.setBlock(pos, state, 11);
        lvl.gameEvent(player, GameEvent.BLOCK_PLACE, ctx.getClickedPos());
        if (player != null) ctx.getItemInHand().hurtAndBreak(1, player, (p) -> { p.broadcastBreakEvent(ctx.getHand()); });
    }
}
