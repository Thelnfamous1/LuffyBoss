package me.infamous.luffy_boss.client.renderer.gear_five_luffy;

import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.entity.GearFiveLuffy;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

public class GearFiveLuffyModel extends AnimatedGeoModel<GearFiveLuffy> {

    public static final ResourceLocation ANIMATION_LOCATION = new ResourceLocation(LuffyBoss.MODID, "animations/gear_five_luffy.animation.json");
    public static final ResourceLocation MODEL_LOCATION = new ResourceLocation(LuffyBoss.MODID, "geo/gear_five_luffy.geo.json");
    public static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(LuffyBoss.MODID, "textures/entity/gear_five_luffy/body.png");

    @Override
    public ResourceLocation getAnimationFileLocation(GearFiveLuffy animatable) {
        return ANIMATION_LOCATION;
    }

    @Override
    public ResourceLocation getModelLocation(GearFiveLuffy object) {
        return MODEL_LOCATION;
    }

    @Override
    public ResourceLocation getTextureLocation(GearFiveLuffy object) {
        return TEXTURE_LOCATION;
    }

    @Override
    public void setLivingAnimations(GearFiveLuffy entity, Integer uniqueID, AnimationEvent event) {
        super.setLivingAnimations(entity, uniqueID, event);
        IBone head = this.getAnimationProcessor().getBone("head");

        EntityModelData extraData = (EntityModelData) event.getExtraDataOfType(EntityModelData.class).get(0);
        if (extraData.headPitch != 0 || extraData.netHeadYaw != 0) {
            head.setRotationX(head.getRotationX() + (extraData.headPitch * ((float) Math.PI / 180F)));
            head.setRotationY(head.getRotationY() + (extraData.netHeadYaw * ((float) Math.PI / 180F)));
        }
    }
}
