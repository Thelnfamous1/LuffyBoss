package me.infamous.luffy_boss.common.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraftforge.event.ForgeEventFactory;
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
import java.util.function.Predicate;

public class GearFiveLuffy extends MonsterEntity implements IAnimatable {
    private static final DataParameter<Integer> DATA_ID_ATTACK_TARGET = EntityDataManager.defineId(GearFiveLuffy.class, DataSerializers.INT);

    private int destroyBlocksTick;
    private final ServerBossInfo bossEvent = (ServerBossInfo)(new ServerBossInfo(
            this.getDisplayName(),
            BossInfo.Color.PURPLE,
            BossInfo.Overlay.PROGRESS))
            .setDarkenScreen(true);
    private static final Predicate<LivingEntity> LIVING_ENTITY_SELECTOR = LivingEntity::attackable;
    private final AnimationFactory animationFactory = GeckoLibUtil.createFactory(this);
    protected static final AnimationBuilder IDLE_ANIM = new AnimationBuilder().addAnimation("idle", ILoopType.EDefaultLoopTypes.LOOP);
    protected static final AnimationBuilder WALK_ANIM = new AnimationBuilder().addAnimation("walk", ILoopType.EDefaultLoopTypes.LOOP);

    public GearFiveLuffy(EntityType<? extends GearFiveLuffy> entityType, World world) {
        super(entityType, world);
        this.setHealth(this.getMaxHealth());
        this.getNavigation().setCanFloat(true);
        this.xpReward = 50;
    }

    public static AttributeModifierMap.MutableAttribute createAttributes() {
        return MonsterEntity.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.6F)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    @Override
    protected void registerGoals() {
        //this.goalSelector.addGoal(2, new RangedAttackGoal(this, 1.0D, 40, 20.0F));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundNBT pCompound) {
        super.addAdditionalSaveData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundNBT pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }

    }

    @Override
    public void setCustomName(@Nullable ITextComponent pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.WITHER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.WITHER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    public void aiStep() {
        Vector3d deltaMovement = this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D);
        if (!this.level.isClientSide && this.getActiveAttackTargetId() > 0) {
            Entity activeAttackTarget = this.level.getEntity(this.getActiveAttackTargetId());
            if (activeAttackTarget != null) {
                double yD = deltaMovement.y;
                if (this.getY() < activeAttackTarget.getY() || this.getY() < activeAttackTarget.getY() + 5.0D) {
                    yD = Math.max(0.0D, yD);
                    yD = yD + (0.3D - yD * (double)0.6F);
                }

                deltaMovement = new Vector3d(deltaMovement.x, yD, deltaMovement.z);
                Vector3d horizontalDistanceVec = new Vector3d(activeAttackTarget.getX() - this.getX(), 0.0D, activeAttackTarget.getZ() - this.getZ());
                if (getHorizontalDistanceSqr(horizontalDistanceVec) > 9.0D) {
                    Vector3d normalHorizontalDIstanceVec = horizontalDistanceVec.normalize();
                    deltaMovement = deltaMovement.add(normalHorizontalDIstanceVec.x * 0.3D - deltaMovement.x * 0.6D, 0.0D, normalHorizontalDIstanceVec.z * 0.3D - deltaMovement.z * 0.6D);
                }
            }
        }

        this.setDeltaMovement(deltaMovement);
        if (getHorizontalDistanceSqr(deltaMovement) > 0.05D) {
            this.yRot = (float) MathHelper.atan2(deltaMovement.z, deltaMovement.x) * (180F / (float)Math.PI) - 90.0F;
        }

        // call super
        super.aiStep();

    }

    @Override
    protected void customServerAiStep() {
        // call super
        super.customServerAiStep();

        if (this.getTarget() != null) {
            this.setActiveAttackTargetId(this.getTarget().getId());
        } else {
            this.setActiveAttackTargetId(0);
        }

        // destroy nearby blocks to aid in pathfinding
        if (this.destroyBlocksTick > 0) {
            --this.destroyBlocksTick;
            if (this.destroyBlocksTick == 0 && ForgeEventFactory.getMobGriefingEvent(this.level, this)) {
                int yFloor = MathHelper.floor(this.getY());
                int xFloor = MathHelper.floor(this.getX());
                int zFloor = MathHelper.floor(this.getZ());
                boolean destroyedBlocks = false;

                for(int xOffset = -1; xOffset <= 1; ++xOffset) {
                    for(int zOffset = -1; zOffset <= 1; ++zOffset) {
                        for(int yOffset = 0; yOffset <= 3; ++yOffset) {
                            int x = xFloor + xOffset;
                            int y = yFloor + yOffset;
                            int z = zFloor + zOffset;
                            BlockPos blockpos = new BlockPos(x, y, z);
                            BlockState blockstate = this.level.getBlockState(blockpos);
                            if (blockstate.canEntityDestroy(this.level, blockpos, this) && ForgeEventFactory.onEntityDestroyBlock(this, blockpos, blockstate)) {
                                destroyedBlocks = this.level.destroyBlock(blockpos, true, this) || destroyedBlocks;
                            }
                        }
                    }
                }

                if (destroyedBlocks) {
                    this.level.levelEvent(null, 1022, this.blockPosition(), 0);
                }
            }
        }

        if (this.tickCount % 20 == 0) {
            this.heal(1.0F);
        }

        this.bossEvent.setPercent(this.getHealth() / this.getMaxHealth());
    }

    @Deprecated //Forge: DO NOT USE, use BlockState.canEntityDestroy
    public static boolean canDestroy(BlockState pBlock) {
        return !pBlock.isAir() && !BlockTags.WITHER_IMMUNE.contains(pBlock.getBlock());
    }

    @Override
    public void makeStuckInBlock(BlockState pState, Vector3d pMotionMultiplier) {
    }

    @Override
    public void startSeenByPlayer(ServerPlayerEntity pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayerEntity pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
    }

    private void performRangedAttack(LivingEntity pTarget) {
        this.performRangedAttack(pTarget.getX(), pTarget.getY() + (double)pTarget.getEyeHeight() * 0.5D, pTarget.getZ());
    }

    private void performRangedAttack(double targetX, double targetY, double targetZ) {
        if (!this.isSilent()) {
            this.level.levelEvent(null, 1024, this.blockPosition(), 0);
        }

        double x = this.getX();
        double y = this.getY() + 3.0D;
        double z = this.getZ();
        double xDist = targetX - x;
        double yDist = targetY - y;
        double zDist = targetZ - z;
        GiantFistEntity giantFistEntity = new GiantFistEntity(this.level, this, xDist, yDist, zDist);
        giantFistEntity.setOwner(this);

        giantFistEntity.setPosRaw(x, y, z);
        this.level.addFreshEntity(giantFistEntity);
    }

    /*
    @Override
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        this.performRangedAttack(pTarget);
    }
     */

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else if (pSource != DamageSource.DROWN && !(pSource.getEntity() instanceof GearFiveLuffy)) {
            if (this.destroyBlocksTick <= 0) {
                this.destroyBlocksTick = 20;
            }

            return super.hurt(pSource, pAmount);
        } else {
            return false;
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource pSource, int pLooting, boolean pRecentlyHit) {
        super.dropCustomDeathLoot(pSource, pLooting, pRecentlyHit);
        ItemEntity bossDrop = this.spawnAtLocation(Items.NETHER_STAR);
        if (bossDrop != null) {
            bossDrop.setExtendedLifetime();
        }

    }

    @Override
    public void checkDespawn() {
        if (this.level.getDifficulty() == Difficulty.PEACEFUL && this.shouldDespawnInPeaceful()) {
            this.remove();
        } else {
            this.noActionTime = 0;
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier) {
        return false;
    }

    @Override
    public boolean addEffect(EffectInstance pEffectInstance) {
        return false;
    }

    public int getActiveAttackTargetId() {
        return this.entityData.get(DATA_ID_ATTACK_TARGET);
    }

    public void setActiveAttackTargetId(int pNewId) {
        this.entityData.set(DATA_ID_ATTACK_TARGET, pNewId);
    }

    @Override
    protected boolean canRide(Entity pEntity) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }


    @Override
    public boolean canBeAffected(EffectInstance pPotioneffect) {
        return super.canBeAffected(pPotioneffect);
    }

    /**
     * Methods for {@link IAnimatable}
     */

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GearFiveLuffy> PlayState predicate(AnimationEvent<T> tAnimationEvent) {
        if(tAnimationEvent.isMoving()){
            tAnimationEvent.getController().setAnimation(WALK_ANIM);
        } else{
            tAnimationEvent.getController().setAnimation(IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }
}
