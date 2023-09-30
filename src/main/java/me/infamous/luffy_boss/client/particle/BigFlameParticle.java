package me.infamous.luffy_boss.client.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.util.math.MathHelper;

public class BigFlameParticle extends DeceleratingParticle {
   protected BigFlameParticle(ClientWorld world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
      super(world, x, y, z, xSpeed, ySpeed, zSpeed);
      this.scale(10.0F);
   }

   @Override
   public IParticleRenderType getRenderType() {
      return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
   }

   @Override
   public void move(double pX, double pY, double pZ) {
      this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
      this.setLocationFromBoundingbox();
   }

   @Override
   public float getQuadSize(float pScaleFactor) {
      float life = ((float)this.age + pScaleFactor) / (float)this.lifetime;
      return this.quadSize * (1.0F - life * life * 0.5F);
   }

   @Override
   public int getLightColor(float pPartialTick) {
      float life = ((float)this.age + pPartialTick) / (float)this.lifetime;
      life = MathHelper.clamp(life, 0.0F, 1.0F);
      int lightColor = super.getLightColor(pPartialTick);
      int blue = lightColor & 255;
      int red = lightColor >> 16 & 255;
      blue = blue + (int)(life * 15.0F * 16.0F);
      if (blue > 240) {
         blue = 240;
      }

      return blue | red << 16;
   }

   public static class Factory implements IParticleFactory<BasicParticleType> {
      protected final IAnimatedSprite sprite;

      public Factory(IAnimatedSprite pSprites) {
         this.sprite = pSprites;
      }

      @Override
      public Particle createParticle(BasicParticleType pType, ClientWorld pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
         BigFlameParticle flameparticle = new BigFlameParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
         flameparticle.pickSprite(this.sprite);
         return flameparticle;
      }
   }
}