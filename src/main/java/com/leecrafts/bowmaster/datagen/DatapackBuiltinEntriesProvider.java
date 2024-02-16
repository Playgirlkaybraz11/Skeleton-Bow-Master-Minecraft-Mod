package com.leecrafts.bowmaster.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.data.registries.RegistriesDatapackGenerator;
import net.minecraft.data.registries.RegistryPatchGenerator;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DatapackBuiltinEntriesProvider extends RegistriesDatapackGenerator {

    public DatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<RegistrySetBuilder.PatchedRegistries> registries, Set<String> modIds) {
        super(output, registries.thenApply(RegistrySetBuilder.PatchedRegistries::patches), modIds);
    }

    public DatapackBuiltinEntriesProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, RegistrySetBuilder registryBuilder, Set<String> modIds) {
        this(output, RegistryPatchGenerator.createLookup(registries, registryBuilder), modIds);
    }

}