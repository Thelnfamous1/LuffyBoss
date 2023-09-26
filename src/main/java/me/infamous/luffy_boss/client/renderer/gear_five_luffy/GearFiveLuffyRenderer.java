package me.infamous.luffy_boss.client.renderer.gear_five_luffy;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.infamous.luffy_boss.common.entity.GearFiveLuffy;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public class GearFiveLuffyRenderer extends GeoEntityRenderer<GearFiveLuffy> {
    public GearFiveLuffyRenderer(EntityRendererManager renderManager) {
        super(renderManager, new GearFiveLuffyModel());
        //this.addLayer(new GearFiveLuffyCapeLayer(this));
    }

    @Override
    public RenderType getRenderType(GearFiveLuffy animatable, float partialTicks, MatrixStack stack,
                                    IRenderTypeBuffer renderTypeBuffer, IVertexBuilder vertexBuilder, int packedLightIn,
                                    ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(this.getTextureLocation(animatable));
    }
}
