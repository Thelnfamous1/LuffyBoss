package me.infamous.luffy_boss.common.entity.attack;

import com.google.common.collect.Maps;
import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.LogicHelper;
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
   private KnockbackState knockbackState = KnockbackState.ALL;

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

   public AreaOfEffectAttack(World pLevel, @Nullable Entity pSource, @Nullable DamageSource pDamageSource, double pToBlowX, double pToBlowY, double pToBlowZ, float pRadius, KnockbackState knockbackState) {
      this(pLevel, pSource, pDamageSource, pToBlowX, pToBlowY, pToBlowZ, pRadius);
      this.knockbackState = knockbackState;
   }

   public static DamageSource aoeAttack(@Nullable AreaOfEffectAttack pExplosion) {
      return aoeAttack(pExplosion != null ? pExplosion.getSourceMob() : null);
   }

   public static DamageSource aoeAttack(@Nullable LivingEntity pLivingEntity) {
      if(pLivingEntity instanceof PlayerEntity) return DamageSource.playerAttack((PlayerEntity) pLivingEntity);
      else if(pLivingEntity != null) return DamageSource.mobAttack(pLivingEntity);
      else return DamageSource.GENERIC;
   }

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
               float damage = this.radius * 2.0F;
               hitEntity.hurt(this.getDamageSource(), damage);
               Vector3d knockbackVec = this.constructKnockbackVector((LivingEntity) hitEntity);
               hitEntity.setDeltaMovement(hitEntity.getDeltaMovement().add(knockbackVec));
               if (hitEntity instanceof PlayerEntity) {
                  LuffyBoss.LOGGER.info("Knocking back {} with {}", hitEntity.getName().getString(), knockbackVec);
                  PlayerEntity hitPlayer = (PlayerEntity) hitEntity;
                  if (!hitPlayer.isSpectator() && (!hitPlayer.isCreative() || !hitPlayer.abilities.flying)) {
                     this.hitPlayers.put(hitPlayer, knockbackVec);
                  }
               }
            }
         }
      }

   }

   private Vector3d constructKnockbackVector(LivingEntity target) {
      if(this.attacker != null){
         double xRatio = MathHelper.sin(this.attacker.yRot * LogicHelper.TO_RADIANS) * MathHelper.cos(this.attacker.xRot * LogicHelper.TO_RADIANS);
         double yRatio = MathHelper.sin(this.attacker.xRot * LogicHelper.TO_RADIANS);
         double zRatio = -MathHelper.cos(this.attacker.yRot * LogicHelper.TO_RADIANS) * MathHelper.cos(this.attacker.xRot * LogicHelper.TO_RADIANS);
         switch (this.knockbackState){
            case HORIZONTAL_ONLY:
               return (new Vector3d(xRatio, 0.0D, zRatio)).normalize().scale(this.radius * this.radius);
            case VERTICAL_ONLY:
               return (new Vector3d(0.0D, yRatio, 0.0D)).normalize().scale(Math.sqrt(this.radius));
            default:
               return (new Vector3d(xRatio, yRatio, zRatio)).normalize().multiply(this.radius * this.radius, this.radius, this.radius * this.radius);
         }
      } else{
         return Vector3d.ZERO;
      }
   }

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

   public enum KnockbackState{
      ALL,
      HORIZONTAL_ONLY,
      VERTICAL_ONLY
   }

}
