package me.infamous.luffy_boss.common.entity.attack;

import com.google.common.collect.Maps;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class AreaOfEffectAttack {
   private final World level;
   private final double x;
   private final double y;
   private final double z;
   @Nullable
   private final Entity attacker;
   private final float radius;
   private final DamageSource damageSource;
   private final Map<PlayerEntity, Vector3d> hitPlayers = Maps.newHashMap();
   private final Vector3d position;

   //client
   public AreaOfEffectAttack(World pLevel, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius) {
      this(pLevel, null, null, pToBlowX, pToBlowY, pToBlowZ, pRadius);
   }

   public AreaOfEffectAttack(World pLevel, @Nullable Entity pSource, @Nullable DamageSource pDamageSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius) {
      this.level = pLevel;
      this.attacker = pSource;
      this.radius = pRadius;
      this.x = pToBlowX;
      this.y = pToBlowY;
      this.z = pToBlowZ;
      this.damageSource = pDamageSource == null ? aoeAttack(this) : pDamageSource;
      this.position = new Vector3d(this.x, this.y, this.z);
   }

   public static DamageSource aoeAttack(@Nullable AreaOfEffectAttack pExplosion) {
      return aoeAttack(pExplosion != null ? pExplosion.getSourceMob() : null);
   }

   public static DamageSource aoeAttack(@Nullable LivingEntity pLivingEntity) {
      if(pLivingEntity instanceof PlayerEntity) return DamageSource.playerAttack((PlayerEntity) pLivingEntity);
      else if(pLivingEntity != null) return DamageSource.mobAttack(pLivingEntity);
      else return DamageSource.GENERIC;
   }

   public static float getSeenPercent(Vector3d sourcePos, Entity target) {
      AxisAlignedBB targetBoundingBox = target.getBoundingBox();
      double xStep = 1.0D / ((targetBoundingBox.maxX - targetBoundingBox.minX) * 2.0D + 1.0D);
      double yStep = 1.0D / ((targetBoundingBox.maxY - targetBoundingBox.minY) * 2.0D + 1.0D);
      double zStep = 1.0D / ((targetBoundingBox.maxZ - targetBoundingBox.minZ) * 2.0D + 1.0D);
      double xOffset = (1.0D - Math.floor(1.0D / xStep) * xStep) / 2.0D;
      double zOffset = (1.0D - Math.floor(1.0D / zStep) * zStep) / 2.0D;
      if (!(xStep < 0.0D) && !(yStep < 0.0D) && !(zStep < 0.0D)) {
         int seenSteps = 0;
         int totalSteps = 0;

         for(float xDelta = 0.0F; xDelta <= 1.0F; xDelta = (float)((double)xDelta + xStep)) {
            for(float yDelta = 0.0F; yDelta <= 1.0F; yDelta = (float)((double)yDelta + yStep)) {
               for(float zDelta = 0.0F; zDelta <= 1.0F; zDelta = (float)((double)zDelta + zStep)) {
                  double x = MathHelper.lerp(xDelta, targetBoundingBox.minX, targetBoundingBox.maxX);
                  double y = MathHelper.lerp(yDelta, targetBoundingBox.minY, targetBoundingBox.maxY);
                  double z = MathHelper.lerp(zDelta, targetBoundingBox.minZ, targetBoundingBox.maxZ);
                  Vector3d from = new Vector3d(x + xOffset, y, z + zOffset);
                  if (target.level.clip(new RayTraceContext(from, sourcePos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, target)).getType() == RayTraceResult.Type.MISS) {
                     ++seenSteps;
                  }

                  ++totalSteps;
               }
            }
         }

         return (float)seenSteps / (float)totalSteps;
      } else {
         return 0.0F;
      }
   }

   /**
    * Does the first part of the explosion (destroy blocks)
    */
   public void attack() {
      float diameter = this.radius * 2.0F;
      int x1 = MathHelper.floor(this.x - (double)diameter - 1.0D);
      int x2 = MathHelper.floor(this.x + (double)diameter + 1.0D);
      int y1 = MathHelper.floor(this.y - (double)diameter - 1.0D);
      int y2 = MathHelper.floor(this.y + (double)diameter + 1.0D);
      int z1 = MathHelper.floor(this.z - (double)diameter - 1.0D);
      int z2 = MathHelper.floor(this.z + (double)diameter + 1.0D);
      List<Entity> hitEntities = this.level.getEntities(this.attacker, new AxisAlignedBB(x1, y1, z1, x2, y2, z2));
      Vector3d sourcePos = new Vector3d(this.x, this.y, this.z);

      for (Entity hitEntity : hitEntities) {
         if (hitEntity.isAlive() && hitEntity instanceof LivingEntity && ((LivingEntity) hitEntity).attackable()) {
            double distanceOverDiameter = MathHelper.sqrt(hitEntity.distanceToSqr(sourcePos)) / diameter;
            if (distanceOverDiameter <= 1.0D) {
               double xDist = hitEntity.getX() - this.x;
               double yDist = hitEntity.getEyeY() - this.y;
               double zDist = hitEntity.getZ() - this.z;
               double distance = MathHelper.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
               if (distance != 0.0D) {
                  xDist = xDist / distance;
                  yDist = yDist / distance;
                  zDist = zDist / distance;
                  double seenPercent = getSeenPercent(sourcePos, hitEntity);
                  double damageFactor = (1.0D - distanceOverDiameter) * seenPercent;
                  hitEntity.hurt(this.getDamageSource(), (float) calculateDamage(diameter, damageFactor));

                  hitEntity.setDeltaMovement(hitEntity.getDeltaMovement().add(xDist * damageFactor, yDist * damageFactor, zDist * damageFactor));
                  if (hitEntity instanceof PlayerEntity) {
                     PlayerEntity hitPlayer = (PlayerEntity) hitEntity;
                     if (!hitPlayer.isSpectator() && (!hitPlayer.isCreative() || !hitPlayer.abilities.flying)) {
                        this.hitPlayers.put(hitPlayer, new Vector3d(xDist * damageFactor, yDist * damageFactor, zDist * damageFactor));
                     }
                  }
               }
            }
         }
      }

   }

   private static int calculateDamage(double diameter, double damageFactor) {
      return (int) ((damageFactor * damageFactor + damageFactor) / 2.0D * 7.0D * diameter + 1.0D);
   }

   /**
    * Does the second part of the explosion (sound, particles, drop spawn)
    */
   public void finalizeAttack(boolean pSpawnParticles) {
      if (this.level.isClientSide) {
         this.level.playLocalSound(this.x, this.y, this.z, SoundEvents.GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (this.level.random.nextFloat() - this.level.random.nextFloat()) * 0.2F) * 0.7F, false);
      }

      if (pSpawnParticles) {
         if (!(this.radius < 2.0F)) {
            this.level.addParticle(ParticleTypes.EXPLOSION_EMITTER, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         } else {
            this.level.addParticle(ParticleTypes.EXPLOSION, this.x, this.y, this.z, 1.0D, 0.0D, 0.0D);
         }
      }

   }

   public DamageSource getDamageSource() {
      return this.damageSource;
   }

   public Map<PlayerEntity, Vector3d> getHitPlayers() {
      return this.hitPlayers;
   }

   /**
    * Returns either the entity that placed the explosive block, the entity that caused the explosion or null.
    */
   @Nullable
   public LivingEntity getSourceMob() {
      if (this.attacker == null) {
         return null;
      } else if (this.attacker instanceof TNTEntity) {
         return ((TNTEntity)this.attacker).getOwner();
      } else if (this.attacker instanceof LivingEntity) {
         return (LivingEntity)this.attacker;
      } else {
         if (this.attacker instanceof ProjectileEntity) {
            Entity entity = ((ProjectileEntity)this.attacker).getOwner();
            if (entity instanceof LivingEntity) {
               return (LivingEntity)entity;
            }
         }

         return null;
      }
   }

   public Vector3d getPosition() {
      return this.position;
   }

   @Nullable
   public Entity getAttacker() {
      return this.attacker;
   }

}
