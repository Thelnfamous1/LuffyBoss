package me.infamous.luffy_boss.client.renderer.gear_five_luffy;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.entity.GearFiveLuffy;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoLayerRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class GearFiveLuffyCapeLayer extends GeoLayerRenderer<GearFiveLuffy> {
    // A resource location for the texture of the layer. This will be applied onto pre-existing cubes on the model
    private static final ResourceLocation LAYER = new ResourceLocation(LuffyBoss.MODID, "textures/entity/gear_five_luffy/cape.png");
    // A resource location for the model of the entity. This model is put on top of the normal one, which is then given the texture
    public GearFiveLuffyCapeLayer(IGeoRenderer<GearFiveLuffy> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, GearFiveLuffy entityLivingBaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        RenderType cameo =  RenderType.armorCutoutNoCull(LAYER);
        matrixStackIn.pushPose();
        //Move or scale the model as you see fit
        //matrixStackIn.scale(1.0f, 1.0f, 1.0f);
        //matrixStackIn.translate(0.0d, 0.0d, 0.0d);
        this.getRenderer().render(this.getEntityModel().getModel(GearFiveLuffyModel.MODEL_LOCATION), entityLivingBaseIn, partialTicks, cameo, matrixStackIn, bufferIn,
                bufferIn.getBuffer(cameo), packedLightIn, OverlayTexture.NO_OVERLAY, 1f, 1f, 1f, 1f);
        matrixStackIn.popPose();
    }
}
