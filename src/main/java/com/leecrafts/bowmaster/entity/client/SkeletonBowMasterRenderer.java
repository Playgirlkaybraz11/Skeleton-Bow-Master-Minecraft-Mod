package com.leecrafts.bowmaster.entity.client;

import com.leecrafts.bowmaster.SkeletonBowMaster;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.jetbrains.annotations.NotNull;

public class SkeletonBowMasterRenderer extends HumanoidMobRenderer<AbstractSkeleton, SkeletonBowMasterModel<AbstractSkeleton>> {

    private static final ResourceLocation SKELETON_BOW_MASTER_LOCATION =
            new ResourceLocation(SkeletonBowMaster.MODID, "textures/entity/skeleton_bow_master.png");

    public SkeletonBowMasterRenderer(EntityRendererProvider.Context pContext) {
        this(pContext, SkeletonBowMasterModel.LAYER_LOCATION, ModelLayers.SKELETON_INNER_ARMOR, ModelLayers.SKELETON_OUTER_ARMOR);
    }

    public SkeletonBowMasterRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pSkeletonBowMasterLayer, ModelLayerLocation pInnerModelLayer, ModelLayerLocation pOuterModelLayer) {
        super(pContext, new SkeletonBowMasterModel<>(pContext.bakeLayer(pSkeletonBowMasterLayer)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this, new SkeletonBowMasterModel<>(pContext.bakeLayer(pInnerModelLayer)), new SkeletonBowMasterModel<>(pContext.bakeLayer(pOuterModelLayer)), pContext.getModelManager()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull AbstractSkeleton pEntity) {
        return SKELETON_BOW_MASTER_LOCATION;
    }

    @Override
    protected boolean isShaking(@NotNull AbstractSkeleton pEntity) {
        return super.isShaking(pEntity);
    }

}
