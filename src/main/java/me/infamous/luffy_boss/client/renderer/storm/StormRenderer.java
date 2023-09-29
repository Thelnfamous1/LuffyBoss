package me.infamous.luffy_boss.client.renderer.storm;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.infamous.luffy_boss.common.entity.attack.StormEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class StormRenderer extends GeoProjectilesRenderer<StormEntity> {
    public StormRenderer(EntityRendererManager renderManager) {
        super(renderManager, new StormModel());
    }

    @Override
    public RenderType getRenderType(StormEntity animatable, float partialTicks, MatrixStack stack,
                                    IRenderTypeBuffer renderTypeBuffer, IVertexBuilder vertexBuilder, int packedLightIn,
                                    ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }

    @Override
    public float getWidthScale(StormEntity animatable2) {
        return super.getWidthScale(animatable2) * (animatable2.getRadius() * 2);
    }

    @Override
    public float getHeightScale(StormEntity entity) {
        return super.getHeightScale(entity) * (entity.getRadius() * 2);
    }
}
