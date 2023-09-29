package me.infamous.luffy_boss.client.renderer.giant_fist;

import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.entity.attack.GiantFistEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class GiantFistModel extends AnimatedGeoModel<GiantFistEntity> {

    public static final ResourceLocation ANIMATION_LOCATION = new ResourceLocation(LuffyBoss.MODID, "animations/giant_fist.animation.json");
    public static final ResourceLocation MODEL_LOCATION = new ResourceLocation(LuffyBoss.MODID, "geo/giant_fist.geo.json");
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(LuffyBoss.MODID, "textures/entity/giant_fist/giant_fist.png");

    @Override
    public ResourceLocation getAnimationFileLocation(GiantFistEntity animatable) {
        return ANIMATION_LOCATION;
    }

    @Override
    public ResourceLocation getModelLocation(GiantFistEntity object) {
        return MODEL_LOCATION;
    }

    @Override
    public ResourceLocation getTextureLocation(GiantFistEntity object) {
        return TEXTURE_LOCATION;
    }
}
