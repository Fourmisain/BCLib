package ru.bclib;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import ru.bclib.api.TagAPI;
import ru.bclib.registry.BaseBlockEntities;
import ru.bclib.registry.BaseRegistry;
import ru.bclib.util.Logger;
import ru.bclib.world.surface.BCLSurfaceBuilders;

public class BCLib implements ModInitializer {
	public static final String MOD_ID = "bclib";
	public static final Logger LOGGER = new Logger(MOD_ID);
	
	@Override
	public void onInitialize() {
		BaseRegistry.register();
		BaseBlockEntities.register();
		BCLSurfaceBuilders.register();
		TagAPI.init();
	}
	
	public static boolean isDevEnvironment() {
		return FabricLoader.getInstance().isDevelopmentEnvironment();
	}
	
	public static boolean isClient() {
		return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
	}

	public static ResourceLocation makeID(String path) {
		return new ResourceLocation(MOD_ID, path);
	}
}
