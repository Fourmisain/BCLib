package ru.bclib.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.BlockModelProvider;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.client.render.ERenderLayer;
import ru.bclib.interfaces.IRenderTyped;

public class BaseMetalBarsBlock extends IronBarsBlock implements BlockModelProvider, IRenderTyped {
	public BaseMetalBarsBlock(Block source) {
		super(FabricBlockSettings.copyOf(source).strength(5.0F, 6.0F).noOcclusion());
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return Collections.singletonList(new ItemStack(this));
	}

	public Optional<String> getModelString(String block) {
		ResourceLocation blockId = Registry.BLOCK.getKey(this);
		if (block.contains("item")) {
			return PatternsHelper.createJson(BasePatterns.ITEM_BLOCK, blockId);
		}
		if (block.contains("post")) {
			return PatternsHelper.createJson(BasePatterns.BLOCK_BARS_POST, blockId);
		}
		else {
			return PatternsHelper.createJson(BasePatterns.BLOCK_BARS_SIDE, blockId);
		}
	}

	@Override
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		return ModelsHelper.createBlockItem(resourceLocation);
	}

	@Override
	public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
		ResourceLocation thisId = Registry.BLOCK.getKey(this);
		String path = blockId.getPath();
		Optional<String> pattern = Optional.empty();
		if (path.endsWith("_post")) {
			pattern = PatternsHelper.createJson(BasePatterns.BLOCK_BARS_POST, thisId);
		}
		if (path.endsWith("_side")) {
			pattern = PatternsHelper.createJson(BasePatterns.BLOCK_BARS_SIDE, thisId);
		}
		return ModelsHelper.fromPattern(pattern);
	}

	@Override
	public UnbakedModel getModelVariant(ResourceLocation stateId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		ResourceLocation postId = new ResourceLocation(stateId.getNamespace(),
				"block/" + stateId.getPath() + "_post");
		ResourceLocation sideId = new ResourceLocation(stateId.getNamespace(),
				"block/" + stateId.getPath() + "_side");
		registerBlockModel(postId, postId, blockState, modelCache);
		registerBlockModel(sideId, sideId, blockState, modelCache);

		ModelsHelper.MultiPartBuilder builder = ModelsHelper.MultiPartBuilder.create(stateDefinition);
		builder.part(postId).setCondition(state ->
				!state.getValue(NORTH) && !state.getValue(EAST) &&
				!state.getValue(SOUTH) && !state.getValue(WEST)).add();
		builder.part(sideId).setCondition(state -> state.getValue(NORTH)).setUVLock(true).add();
		builder.part(sideId).setCondition(state -> state.getValue(EAST))
				.setTransformation(BlockModelRotation.X0_Y90.getRotation()).setUVLock(true).add();
		builder.part(sideId).setCondition(state -> state.getValue(SOUTH))
				.setTransformation(BlockModelRotation.X0_Y180.getRotation()).setUVLock(true).add();
		builder.part(sideId).setCondition(state -> state.getValue(WEST))
				.setTransformation(BlockModelRotation.X0_Y270.getRotation()).setUVLock(true).add();

		return builder.build();
	}

	@Environment(EnvType.CLIENT)
	public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
		if (direction.getAxis().isVertical() && stateFrom.getBlock().is(this) && !stateFrom.equals(state)) {
			return false;
		}
		return super.skipRendering(state, stateFrom, direction);
	}

	@Override
	public ERenderLayer getRenderLayer() {
		return ERenderLayer.CUTOUT;
	}
}
