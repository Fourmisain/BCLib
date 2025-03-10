package ru.bclib.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import ru.bclib.client.models.BasePatterns;
import ru.bclib.client.models.BlockModelProvider;
import ru.bclib.client.models.ModelsHelper;
import ru.bclib.client.models.PatternsHelper;

public class BaseGateBlock extends FenceGateBlock implements BlockModelProvider {
	private final Block parent;
	
	public BaseGateBlock(Block source) {
		super(FabricBlockSettings.copyOf(source).noOcclusion());
		this.parent = source;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return Collections.singletonList(new ItemStack(this));
	}

	@Override
	public BlockModel getItemModel(ResourceLocation resourceLocation) {
		return getBlockModel(resourceLocation, defaultBlockState());
	}

	@Override
	public @Nullable BlockModel getBlockModel(ResourceLocation blockId, BlockState blockState) {
		boolean inWall = blockState.getValue(IN_WALL);
		boolean isOpen = blockState.getValue(OPEN);
		ResourceLocation parentId = Registry.BLOCK.getKey(parent);
		Optional<String> pattern;
		if (inWall) {
			pattern = isOpen ? PatternsHelper.createJson(BasePatterns.BLOCK_GATE_OPEN_WALL, parentId) :
					PatternsHelper.createJson(BasePatterns.BLOCK_GATE_CLOSED_WALL, parentId);
		} else {
			pattern = isOpen ? PatternsHelper.createJson(BasePatterns.BLOCK_GATE_OPEN, parentId) :
					PatternsHelper.createJson(BasePatterns.BLOCK_GATE_CLOSED, parentId);
		}
		return ModelsHelper.fromPattern(pattern);
	}

	@Override
	public UnbakedModel getModelVariant(ResourceLocation stateId, BlockState blockState, Map<ResourceLocation, UnbakedModel> modelCache) {
		boolean inWall = blockState.getValue(IN_WALL);
		boolean isOpen = blockState.getValue(OPEN);
		String state = "" + (inWall ? "_wall" : "") + (isOpen ? "_open" : "_closed");
		ResourceLocation modelId = new ResourceLocation(stateId.getNamespace(),
				"block/" + stateId.getPath() + state);
		registerBlockModel(stateId, modelId, blockState, modelCache);
		return ModelsHelper.createFacingModel(modelId, blockState.getValue(FACING), true, false);
	}
}