package me.infamous.luffy_boss.common.entity.attack;

import com.google.common.collect.Maps;
import me.infamous.luffy_boss.common.LogicHelper;
import me.infamous.luffy_boss.common.registry.LBEntityTypes;
import net.minecraft.block.material.PushReaction;
import net.minecraft.entity.*;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.UUID;

public class StormEntity extends Entity implements IAnimatable {
   private static final DataParameter<Float> DATA_RADIUS = EntityDataManager.defineId(StormEntity.class, DataSerializers.FLOAT);
   private static final DataParameter<Integer> DATA_COLOR = EntityDataManager.defineId(StormEntity.class, DataSerializers.INT);
   private static final DataParameter<Boolean> DATA_ATTACHED_TO_OWNER = EntityDataManager.defineId(StormEntity.class, DataSerializers.BOOLEAN);
   private static final DataParameter<OptionalInt> DATA_OWNER_ID = EntityDataManager.defineId(StormEntity.class, DataSerializers.OPTIONAL_UNSIGNED_INT);
   public static final String ATTACHED_TO_OWNER_TAG = "AttachedToOwner";

   private final Map<Entity, Integer> victims = Maps.newHashMap();
   private int duration = 600;
   private int waitTime = 20;
   private int reapplicationDelay = 20;
   private boolean fixedColor;
   private int durationOnUse;
   private float radiusOnUse;
   private float radiusPerTick;
   private LivingEntity cachedOwner;
   private UUID ownerUUID;
   private static final AnimationBuilder IDLE_ANIM = new AnimationBuilder().addAnimation("idle", ILoopType.EDefaultLoopTypes.LOOP);
   private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);

   public StormEntity(EntityType<? extends StormEntity> entityType, World world) {
      super(entityType, world);
      this.noPhysics = true;
      this.setRadius(3.0F);
   }

   public StormEntity(World world, double x, double y, double z) {
      this(LBEntityTypes.STORM.get(), world);
      this.setPos(x, y, z);
   }

   public StormEntity(World world, @Nullable LivingEntity owner, double x, double y, double z) {
      this(world, x, y, z);
      this.setOwner(owner);
   }

   public StormEntity(World world, LivingEntity owner) {
      this(world, owner, owner.getX(), owner.getY(), owner.getZ());
      this.entityData.set(DATA_ATTACHED_TO_OWNER, true);
   }

   @Override
   protected void defineSynchedData() {
      this.getEntityData().define(DATA_COLOR, 0);
      this.getEntityData().define(DATA_RADIUS, 0.5F);
      this.getEntityData().define(DATA_ATTACHED_TO_OWNER, false);
      this.entityData.define(DATA_OWNER_ID, OptionalInt.empty());
   }

   public void setRadius(float pRadius) {
      if (!this.level.isClientSide) {
         this.getEntityData().set(DATA_RADIUS, pRadius);
      }
   }

   @Override
   public void refreshDimensions() {
      double x = this.getX();
      double y = this.getY();
      double z = this.getZ();
      super.refreshDimensions();
      this.setPos(x, y, z);
   }

   public float getRadius() {
      return this.getEntityData().get(DATA_RADIUS);
   }

   public int getColor() {
      return this.getEntityData().get(DATA_COLOR);
   }

   public void setFixedColor(int pColor) {
      this.fixedColor = true;
      this.getEntityData().set(DATA_COLOR, pColor);
   }

   /**
    * Sets if the radius should be ignored, and the effect should be shown in a single point instead of an area
    */
   protected void setAttachedToOwner(boolean pIgnoreRadius) {
      this.getEntityData().set(DATA_ATTACHED_TO_OWNER, pIgnoreRadius);
   }

   /**
    * Returns true if the radius should be ignored, and the effect should be shown in a single point instead of an area
    */
   public boolean isAttachedToOwner() {
      return this.getEntityData().get(DATA_ATTACHED_TO_OWNER);
   }

   public int getDuration() {
      return this.duration;
   }

   public void setDuration(int pDuration) {
      this.duration = pDuration;
   }

   private boolean hasOwnerId() {
      return this.entityData.get(DATA_OWNER_ID).isPresent();
   }

   @Override
   public void tick() {
      super.tick();

      if (this.isAttachedToOwner()) {
         LivingEntity owner = this.getOwner();
         if (owner != null) {
            this.setPos(owner.getX(), owner.getY(), owner.getZ());
            this.setDeltaMovement(owner.getDeltaMovement());
         }
      }

      if (!this.level.isClientSide){
         float radius = this.getRadius();
         if (this.tickCount >= this.waitTime + this.duration) {
            this.remove();
            return;
         }

         if (this.radiusPerTick != 0.0F) {
            radius += this.radiusPerTick;
            if (radius < 0.5F) {
               this.remove();
               return;
            }

            this.setRadius(radius);
         }

         if (this.tickCount % 5 == 0) {

            this.victims.entrySet().removeIf(entry -> this.tickCount >= entry.getValue());
            List<LivingEntity> nearbyEntities = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
            if (!nearbyEntities.isEmpty()) {
               for(LivingEntity nearbyEntity : nearbyEntities) {
                  if (!this.victims.containsKey(nearbyEntity) && nearbyEntity != this.getOwner()) {
                     double xDist = nearbyEntity.getX() - this.getX();
                     double zDist = nearbyEntity.getZ() - this.getZ();
                     double horizDistSqr = xDist * xDist + zDist * zDist;
                     if (horizDistSqr <= (double)(radius * radius)) {
                        this.victims.put(nearbyEntity, this.tickCount + this.reapplicationDelay);

                        // lightning strike
                        LightningBoltEntity lightningBolt = EntityType.LIGHTNING_BOLT.create(this.level);
                        lightningBolt.moveTo(nearbyEntity.getX(), nearbyEntity.getY(), nearbyEntity.getZ());
                        lightningBolt.setVisualOnly(true);
                        this.level.addFreshEntity(lightningBolt);
                        nearbyEntity.hurt(DamageSource.indirectMagic(lightningBolt, this.getOwner()), lightningBolt.getDamage());

                        if (this.radiusOnUse != 0.0F) {
                           radius += this.radiusOnUse;
                           if (radius < 0.5F) {
                              this.remove();
                              return;
                           }

                           this.setRadius(radius);
                        }

                        if (this.durationOnUse != 0) {
                           this.duration += this.durationOnUse;
                           if (this.duration <= 0) {
                              this.remove();
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void setRadiusOnUse(float pRadiusOnUse) {
      this.radiusOnUse = pRadiusOnUse;
   }

   public void setRadiusPerTick(float pRadiusPerTick) {
      this.radiusPerTick = pRadiusPerTick;
   }

   public void setWaitTime(int pWaitTime) {
      this.waitTime = pWaitTime;
   }

   public void setOwner(@Nullable LivingEntity owner) {
      this.cachedOwner = owner;
      this.ownerUUID = owner == null ? null : owner.getUUID();
      this.setOwnerId(owner);
   }

   private void setOwnerId(@Nullable LivingEntity pOwner) {
      this.entityData.set(DATA_OWNER_ID, pOwner == null ? OptionalInt.empty() : OptionalInt.of(pOwner.getId()));
   }

   private int getOwnerId() {
      return this.entityData.get(DATA_OWNER_ID).orElse(0);
   }

   @Nullable
   public LivingEntity getOwner() {
      UUID ownerUUID = this.ownerUUID;
      if(ownerUUID == null) return null;

      LivingEntity cachedOwner = this.cachedOwner;
      int ownerId = this.getOwnerId();
      // return cached owner if not null, not removed from level and its id matches the synced owner id
      if (cachedOwner != null && !cachedOwner.removed && cachedOwner.getId() == ownerId) {
         return cachedOwner;
      }
      // if on server, find and cache owner from owner UUID, and sync its network id to the client
      else if (this.level instanceof ServerWorld) {
         LivingEntity owner = LogicHelper.getLivingEntity(((ServerWorld) this.level).getEntity(this.ownerUUID));
         this.cachedOwner = owner;
         this.setOwnerId(owner);
         return owner;
      }
      // if on client, find and cache owner from synced owner id
      else if(ownerId != 0) {
         LivingEntity owner = LogicHelper.getLivingEntity(this.level.getEntity(ownerId));
         this.cachedOwner = owner;
         return owner;
      } else {
         return null;
      }
   }

   @Override
   protected void readAdditionalSaveData(CompoundNBT pCompound) {
      this.tickCount = pCompound.getInt("Age");
      this.duration = pCompound.getInt("Duration");
      this.waitTime = pCompound.getInt("WaitTime");
      this.reapplicationDelay = pCompound.getInt("ReapplicationDelay");
      this.durationOnUse = pCompound.getInt("DurationOnUse");
      this.radiusOnUse = pCompound.getFloat("RadiusOnUse");
      this.radiusPerTick = pCompound.getFloat("RadiusPerTick");
      this.setRadius(pCompound.getFloat("Radius"));
      if (pCompound.hasUUID("Owner")) {
         this.ownerUUID = pCompound.getUUID("Owner");
      }

      if (pCompound.contains("Color", 99)) {
         this.setFixedColor(pCompound.getInt("Color"));
      }

      this.setAttachedToOwner(pCompound.getBoolean(ATTACHED_TO_OWNER_TAG));

   }

   @Override
   protected void addAdditionalSaveData(CompoundNBT pCompound) {
      pCompound.putInt("Age", this.tickCount);
      pCompound.putInt("Duration", this.duration);
      pCompound.putInt("WaitTime", this.waitTime);
      pCompound.putInt("ReapplicationDelay", this.reapplicationDelay);
      pCompound.putInt("DurationOnUse", this.durationOnUse);
      pCompound.putFloat("RadiusOnUse", this.radiusOnUse);
      pCompound.putFloat("RadiusPerTick", this.radiusPerTick);
      pCompound.putFloat("Radius", this.getRadius());
      if (this.ownerUUID != null) {
         pCompound.putUUID("Owner", this.ownerUUID);
      }

      if (this.fixedColor) {
         pCompound.putInt("Color", this.getColor());
      }

      if(this.isAttachedToOwner()){
         pCompound.putBoolean(ATTACHED_TO_OWNER_TAG, true);
      }

   }

   @Override
   public void onSyncedDataUpdated(DataParameter<?> pKey) {
      if (DATA_RADIUS.equals(pKey)) {
         this.refreshDimensions();
      }

      super.onSyncedDataUpdated(pKey);
   }

   @Override
   public PushReaction getPistonPushReaction() {
      return PushReaction.IGNORE;
   }

   @Override
   public IPacket<?> getAddEntityPacket() {
      return NetworkHooks.getEntitySpawningPacket(this);
   }

   @Override
   public EntitySize getDimensions(Pose pPose) {
      return EntitySize.scalable(this.getRadius() * 2.0F, this.getRadius() * 2.0F);
   }

   /**
    * Methods for {@link IAnimatable}
    */

   @Override
   public void registerControllers(AnimationData data) {
      data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
   }

   private <T extends StormEntity> PlayState predicate(AnimationEvent<T> event) {
      event.getController().setAnimation(IDLE_ANIM);
      return PlayState.CONTINUE;
   }

   @Override
   public AnimationFactory getFactory() {
      return this.animationFactory;
   }
}