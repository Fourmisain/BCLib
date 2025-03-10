package ru.bclib.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.level.storage.loot.LootContext;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.BlockModelProvider;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;
import ru.bclib.client.render.ERenderLayer;
import ru.bclib.interfaces.IRenderTyped;

public class BaseChainBlock extends ChainBlock implements BlockModelProvider, IRenderTyped {
	public BaseChainBlock(MaterialColor color) {
		super(FabricBlockSettings.copyOf(Blocks.CHAIN).materialColor(color));
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return Collections.singletonList(new ItemStack(this));
	}

	@Override
	public BlockModel getItemModel(ResourceLocation blockId) {
		return ModelsHelper.createItemModel(blockId);
	}

	@Override
	public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
		Optional<String> pattern = PatternsHelper.createJson(BasePatterns.BLOCK_CHAIN, blockId);
		return ModelsHelper.fromPattern(pattern);
	}

	@Override
	public UnbakedModel getModelVariant(ResourceLocation stateId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		Direction.Axis axis = blockState.getValue(AXIS);
		ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(),
				"block/" + stateId.getPath());
		registerBlockModel(stateId, modelId, blockState, modelCache);
		return ModelsHelper.createRotatedModel(modelId, axis);
	}

	@Override
	public ERenderLayer getRenderLayer() {
		return ERenderLayer.CUTOUT;
	}
}
