package me.infamous.luffy_boss.common.entity;

import me.infamous.luffy_boss.common.registry.LBEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

public class GiantFistEntity extends DamagingProjectileEntity implements IEntityAdditionalSpawnData {

   public GiantFistEntity(EntityType<? extends GiantFistEntity> entityType, World world) {
      super(entityType, world);
   }

   // typical server-side instantiation
   public GiantFistEntity(World world, LivingEntity shooter, double xDist, double yDist, double zDist) {
      super(LBEntityTypes.GIANT_FIST.get(), shooter, xDist, yDist, zDist, world);
   }

   // UNUSED: vanilla-style client-side instantiation, would be done through SSSpawnObjectPacket handling
   public GiantFistEntity(World world, double x, double y, double z, double xDist, double yDist, double zDist) {
      super(LBEntityTypes.GIANT_FIST.get(), x, y, z, xDist, yDist, zDist, world);
   }

   public static DamageSource damageSource(GiantFistEntity giantFist, Entity pIndirectEntity) {
      return (new IndirectEntityDamageSource("giantFist", giantFist, pIndirectEntity)).setProjectile();
   }

   @Override
   protected float getInertia() {
      return super.getInertia();
   }

   @Override
   public boolean isOnFire() {
      return false;
   }

   @Override
   public float getBlockExplosionResistance(Explosion pExplosion, IBlockReader pLevel, BlockPos pPos, BlockState pBlockState, FluidState pFluidState, float pExplosionPower) {
      return pBlockState.canEntityDestroy(pLevel, pPos, this) ? Math.min(0.8F, pExplosionPower) : pExplosionPower;
   }

   @Override
   protected void onHitEntity(EntityRayTraceResult pResult) {
      super.onHitEntity(pResult);
      if (!this.level.isClientSide) {
         Entity target = pResult.getEntity();
         Entity owner = this.getOwner();
         boolean hurt;
         if (owner instanceof LivingEntity) {
            LivingEntity shooter = (LivingEntity)owner;
            hurt = target.hurt(damageSource(this, shooter), 8.0F);
            if (hurt) {
               if (target.isAlive()) {
                  this.doEnchantDamageEffects(shooter, target);
               } else {
                  shooter.heal(5.0F);
               }
            }
         } else {
            hurt = target.hurt(DamageSource.MAGIC, 5.0F);
         }

         if (hurt && target instanceof LivingEntity) {
            int effectDurationMultiplier = 0;
            if (this.level.getDifficulty() == Difficulty.NORMAL) {
               effectDurationMultiplier = 10;
            } else if (this.level.getDifficulty() == Difficulty.HARD) {
               effectDurationMultiplier = 40;
            }

            if (effectDurationMultiplier > 0) {
               ((LivingEntity)target).addEffect(new EffectInstance(Effects.WITHER, 20 * effectDurationMultiplier, 1));
            }
         }

      }
   }

   @Override
   protected void onHit(RayTraceResult pResult) {
      super.onHit(pResult);
      if (!this.level.isClientSide) {
         Explosion.Mode explosion$mode = ForgeEventFactory.getMobGriefingEvent(this.level, this.getOwner()) ? Explosion.Mode.DESTROY : Explosion.Mode.NONE;
         this.level.explode(this, this.getX(), this.getY(), this.getZ(), 1.0F, false, explosion$mode);
         this.remove();
      }

   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public boolean hurt(DamageSource pSource, float pAmount) {
      return false;
   }

   protected boolean shouldBurn() {
      return false;
   }

   @Override
   public IPacket<?> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public void writeSpawnData(PacketBuffer buffer) {
      Entity owner = this.getOwner();
      int ownerId = owner == null ? 0 : owner.getId();
      buffer.writeInt(ownerId);

      int xPower = (int)(MathHelper.clamp(this.xPower, -3.9D, 3.9D) * 8000.0D);
      int yPower = (int)(MathHelper.clamp(this.yPower, -3.9D, 3.9D) * 8000.0D);
      int zPower = (int)(MathHelper.clamp(this.zPower, -3.9D, 3.9D) * 8000.0D);
      buffer.writeShort(xPower);
      buffer.writeShort(yPower);
      buffer.writeShort(zPower);
   }

   @Override
   public void readSpawnData(PacketBuffer additionalData) {
      int ownerId = additionalData.readInt();
      Entity owner = this.level.getEntity(ownerId);
      if(owner != null) this.setOwner(owner);

      this.xPower = additionalData.readShort() / 8000.0D;
      this.yPower = additionalData.readShort() / 8000.0D;
      this.zPower = additionalData.readShort() / 8000.0D;
   }
}