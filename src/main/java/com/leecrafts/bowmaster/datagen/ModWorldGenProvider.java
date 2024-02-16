package com.leecrafts.bowmaster.datagen;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import com.leecrafts.bowmaster.world.dimension.ModDimensions;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

// The DatapackBuiltinEntriesProvider class was actually missing! It was removed during the update from 1.20.2 to 1.20.3
// I don't know when it's gonna be added back, but I had to manually implement this class.
// With my manually implemented DatapackBuiltinEntriesProvider class, I could only get the datagen working for the dimension type.
// So /src/generated/resources/data/bowmaster/dimension_type/arena_type.json was automatically generated,
// but /src/generated/resources/data/bowmaster/dimension/arena.json was created manually by me.
public class ModWorldGenProvider extends DatapackBuiltinEntriesProvider {

    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.DIMENSION_TYPE, ModDimensions::bootstrapType);
//            .add(Registries.LEVEL_STEM, ModDimensions::bootstrapStem);

    public ModWorldGenProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, BUILDER, Set.of(SkeletonBowMaster.MODID));
    }

}
