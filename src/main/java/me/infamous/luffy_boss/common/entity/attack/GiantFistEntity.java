package me.infamous.luffy_boss.common.entity.attack;

import me.infamous.luffy_boss.common.LogicHelper;
import me.infamous.luffy_boss.common.registry.LBEntityTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

public class GiantFistEntity extends DamagingProjectileEntity implements IEntityAdditionalSpawnData, IAnimatable {

   private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

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
   public void tick() {
      this.resetRot();
      super.tick();
      this.resetRot();
   }

   private void resetRot() {
      this.yRot = 0;
      this.xRot = 0;
      this.yRotO = 0;
      this.xRotO = 0;
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
   protected void onHit(RayTraceResult pResult) {
      super.onHit(pResult);
      if (!this.level.isClientSide) {
         LogicHelper.areaOfEffectAttack((ServerWorld) this.level, this.getOwner(), null, pResult.getLocation().x, pResult.getLocation().y, pResult.getLocation().z, 5.0F, AreaOfEffectAttack.KnockbackState.VERTICAL_ONLY);
         this.remove();
      }

   }

   @Override
   protected boolean canHitEntity(Entity entity) {
      if(entity instanceof PartEntity && ((PartEntity<?>)entity).getParent() == this.getOwner()){
         return false;
      }
      return super.canHitEntity(entity);
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

   /**
    * Methods for {@link IAnimatable}
    */

   @Override
   public void registerControllers(AnimationData data) {
      data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
   }

   private <T extends GiantFistEntity> PlayState predicate(AnimationEvent<T> event) {
      return PlayState.STOP;
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }
}