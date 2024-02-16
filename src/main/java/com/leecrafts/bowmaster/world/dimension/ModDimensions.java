package com.leecrafts.bowmaster.world.dimension;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.OptionalLong;

public class ModDimensions {

//    public static final ResourceKey<LevelStem> ARENA_KEY = ResourceKey.create(Registries.LEVEL_STEM,
//            new ResourceLocation(SkeletonBowMaster.MODID, "arena"));

    public static final ResourceKey<Level> ARENA_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            new ResourceLocation(SkeletonBowMaster.MODID, "arena"));

    public static final ResourceKey<DimensionType> ARENA_DIM_TYPE =
            ResourceKey.create(Registries.DIMENSION_TYPE, ARENA_LEVEL_KEY.location());

    public static void bootstrapType(BootstapContext<DimensionType> context) {
        context.register(ARENA_DIM_TYPE, new DimensionType(
                OptionalLong.of(6000),
                false,
                false,
                false,
                false,
                1.0,
                false,
                false,
                0,
                256,
                128,
                BlockTags.INFINIBURN_OVERWORLD,
                BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                1.0f,
                new DimensionType.MonsterSettings(false, false, UniformInt.of(0, 7), 0)));
    }

//    public static void bootstrapStem(BootstapContext<LevelStem> context) {
//        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
//        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
//        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);
//
//        NoiseBasedChunkGenerator wrappedChunkGenerator = new NoiseBasedChunkGenerator(
//                new FixedBiomeSource(biomeRegistry.getOrThrow(Biomes.PLAINS)),
//                noiseGenSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
//
//        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ARENA_DIM_TYPE), wrappedChunkGenerator);
//
//        context.register(ARENA_KEY, stem);
//    }

    public static void register() {
        System.out.println("Registering ModDimensions for " + SkeletonBowMaster.MODID);
    }

}
