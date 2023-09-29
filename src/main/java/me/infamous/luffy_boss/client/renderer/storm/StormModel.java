package me.infamous.luffy_boss.client.renderer.storm;

import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.entity.attack.StormEntity;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class StormModel extends AnimatedGeoModel<StormEntity> {

    public static final ResourceLocation ANIMATION_LOCATION = new ResourceLocation(LuffyBoss.MODID, "animations/storm.animation.json");
    public static final ResourceLocation MODEL_LOCATION = new ResourceLocation(LuffyBoss.MODID, "geo/storm.geo.json");
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(LuffyBoss.MODID, "textures/entity/storm/storm.png");

    @Override
    public ResourceLocation getAnimationFileLocation(StormEntity animatable) {
        return ANIMATION_LOCATION;
    }

    @Override
    public ResourceLocation getModelLocation(StormEntity object) {
        return MODEL_LOCATION;
    }

    @Override
    public ResourceLocation getTextureLocation(StormEntity object) {
        return TEXTURE_LOCATION;
    }
}
