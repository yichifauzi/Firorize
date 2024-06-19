package com.oscimate.oscimate_soulflame;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import com.oscimate.oscimate_soulflame.config.ChangeFireColorScreen;
import com.oscimate.oscimate_soulflame.config.ConfigManager;
import com.oscimate.oscimate_soulflame.test.TestModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.client.render.entity.ZombieBaseEntityRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.EntityList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.oscimate.oscimate_soulflame.CustomRenderLayer.getCustomTint;


@Environment(EnvType.CLIENT)
public class Main implements ClientModInitializer {
    public static final String MODID = "oscimate_soulflame";
    public static final ConfigManager CONFIG_MANAGER = new ConfigManager();
    public static double currentFireHeight = 0.0;

    public static final Supplier<Sprite> BLANK_FIRE_0 = Suppliers.memoize(() -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("oscimate_soulflame:block/blank_fire_0")).getSprite());
    public static final Supplier<Sprite> BLANK_FIRE_0_OVERLAY = Suppliers.memoize(() -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("oscimate_soulflame:block/blank_fire_overlay_0")).getSprite());

    public static final Supplier<Sprite> SOUL_FIRE_1 = Suppliers.memoize(() -> new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("block/soul_fire_1")).getSprite());
    public static final Supplier<Sprite> SOUL_FIRE_0 = Suppliers.memoize(() -> new SpriteIdentifier(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, new Identifier("block/soul_fire_0")).getSprite());

    public static final Supplier<Sprite> BLANK_FIRE_1 = Suppliers.memoize(() -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("oscimate_soulflame:block/blank_fire_1")).getSprite());

    public static final Supplier<Sprite> BLANK_FIRE_1_OVERLAY = Suppliers.memoize(() -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("oscimate_soulflame:block/blank_fire_overlay_1")).getSprite());


    public int getColorInt(int r, int g, int b) {
        return r << 16 | g << 8 | b;
    }


    @Override
    public void onInitializeClient() {

        ChangeFireColorScreen.CONFIG.load();
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.FIRE, getCustomTint());
        ModelLoadingPlugin.register(pluginContext -> {
            pluginContext.modifyModelAfterBake().register(ModelModifier.WRAP_PHASE, (model, context) -> {
                if (context.id().getPath().contains("block/fire_")) {
                    return new TestModel(model);
                }
                return model;
            });
        });
        ColorProviderRegistry.BLOCK.register(((state, world, pos, tintIndex) -> {
            if (world.hasBiomes()) {
                RegistryKey<Biome> biome = world.getBiomeFabric(pos).getKey().orElseThrow();
                if (biome.equals(BiomeKeys.WARPED_FOREST)) {
                    if (tintIndex == 1) {
                        return this.getColorInt(0, 255, 200);
//                        return this.getColorInt(167, 68, 0);
                    }
                    if (tintIndex == 2) {
                        return this.getColorInt(195, 255, 0);
//                        return this.getColorInt(74, 68, 0);
                    }
                }
                else if (biome.equals(BiomeKeys.BASALT_DELTAS)) {
                    if (tintIndex == 1) {
                        return this.getColorInt(23, 68, 0);
                    }
                    if (tintIndex == 2) {
                        return this.getColorInt(45, 95, 0);
                    }
                } else {
                    return  this.getColorInt(0, 0, 0);
                }
            }
            return  this.getColorInt(0, 0, 0);
//            return -1;
        }), Blocks.FIRE);


        if(!CONFIG_MANAGER.fileExists()) {
            CONFIG_MANAGER.save();
        }
        CONFIG_MANAGER.getStartupConfig();
    }
}