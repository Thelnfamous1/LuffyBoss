package me.infamous.luffy_boss.common.entity;

import me.infamous.luffy_boss.common.LogicHelper;
import me.infamous.luffy_boss.common.entity.attack.AnimatableMeleeAttack;
import me.infamous.luffy_boss.common.entity.attack.AnimatableMeleeAttackGoal;
import me.infamous.luffy_boss.common.entity.attack.LuffyAttackType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.entity.PartEntity;
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

public class GearFiveLuffy extends MonsterEntity implements IAnimatable, AnimatableMeleeAttack<LuffyAttackType>{
    private static final DataParameter<Integer> DATA_ID_ATTACK_TARGET = EntityDataManager.defineId(GearFiveLuffy.class, DataSerializers.INT);
    private static final DataParameter<Byte> DATA_ATTACK_TYPE_ID = EntityDataManager.defineId(GearFiveLuffy.class, DataSerializers.BYTE);
    public static final double ARM_X_OFFSET = 3.4375;
    public static final double ARM_Y_OFFSET = 5.3857;
    public static final double BODY_Y_OFFSET = 5.56;
    public static final double HEAD_Y_OFFSET = 12.7;
    public static final double LEG_X_OFFSET = 1.25;
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
    protected static final AnimationBuilder STORM_ANIM = new AnimationBuilder().addAnimation("storm", ILoopType.EDefaultLoopTypes.LOOP);
    protected static final AnimationBuilder GROUND_PUNCH_ANIM = new AnimationBuilder().addAnimation("Attack3", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    protected static final AnimationBuilder GIANT_FIST_ANIM = new AnimationBuilder().addAnimation("Attack4", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    protected static final AnimationBuilder SHOCKWAVE_ANIM = new AnimationBuilder().addAnimation("shockwave", ILoopType.EDefaultLoopTypes.LOOP);
    private int attackAnimationTick;
    private LuffyAttackType currentAttackType;
    private final LuffyPartEntity[] subEntities;
    private final LuffyPartEntity head;
    private final LuffyPartEntity body;
    private final LuffyPartEntity leftArm;
    private final LuffyPartEntity rightArm;
    private final LuffyPartEntity leftLeg;
    private final LuffyPartEntity rightLeg;

    public GearFiveLuffy(EntityType<? extends GearFiveLuffy> entityType, World world) {
        super(entityType, world);
        this.setHealth(this.getMaxHealth());
        this.getNavigation().setCanFloat(true);
        this.xpReward = 50;

        this.head = new LuffyPartEntity(this, "head", 3.6F, 2.9F);
        this.body = new LuffyPartEntity(this, "body", 6.0F, 5.0F);
        this.leftArm = new LuffyPartEntity(this, "leftArm", 1.9F, 6.7F);
        this.rightArm = new LuffyPartEntity(this, "rightArm", 1.9F, 6.7F);
        this.leftLeg = new LuffyPartEntity(this, "leftLeg", 2.0F, 8.1375F);
        this.rightLeg = new LuffyPartEntity(this, "rightLeg", 2.0F, 8.1375F);
        this.subEntities = new LuffyPartEntity[]{this.head, this.body, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg};
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
        this.goalSelector.addGoal(2, new AnimatableMeleeAttackGoal<GearFiveLuffy, LuffyAttackType>(this, GearFiveLuffy::selectAttackType, 1.0F, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(7, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, 0, false, false, LIVING_ENTITY_SELECTOR));
    }

    private static LuffyAttackType selectAttackType(GearFiveLuffy luffy){
        return LuffyAttackType.byId(luffy.random.nextInt(4) + 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_ID_ATTACK_TARGET, 0);
        this.entityData.define(DATA_ATTACK_TYPE_ID, (byte)0);
    }

    @Override
    public void onSyncedDataUpdated(DataParameter<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if(DATA_ATTACK_TYPE_ID.equals(pKey)){
            this.startAttackAnimation(this.getCurrentAttackType());
        }
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
    public void baseTick() {
        super.baseTick();
        if(this.attackAnimationTick > 0){
            this.attackAnimationTick--;
        }
        if(!this.level.isClientSide && this.attackAnimationTick <= 0){
            this.resetAttackType();
        }
    }

    @Override
    public void aiStep() {
        /*
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
         */

        // call super
        super.aiStep();


        Vector3d[] prevPositions = new Vector3d[this.subEntities.length];

        for(int j = 0; j < this.subEntities.length; ++j) {
            prevPositions[j] = new Vector3d(this.subEntities[j].getX(), this.subEntities[j].getY(), this.subEntities[j].getZ());
        }

        this.tickPart(this.head, 0, HEAD_Y_OFFSET, 0, this.yRot);
        this.tickPart(this.body, 0, BODY_Y_OFFSET, 0, this.yBodyRot);
        this.tickPart(this.leftArm, -ARM_X_OFFSET, ARM_Y_OFFSET, 0, this.yBodyRot);
        this.tickPart(this.rightArm, ARM_X_OFFSET, ARM_Y_OFFSET, 0, this.yBodyRot);
        this.tickPart(this.leftLeg, -LEG_X_OFFSET, 0, 0, this.yBodyRot);
        this.tickPart(this.rightLeg, LEG_X_OFFSET, 0, 0, this.yBodyRot);

        for(int i = 0; i < this.subEntities.length; ++i) {
            this.subEntities[i].xo = prevPositions[i].x;
            this.subEntities[i].yo = prevPositions[i].y;
            this.subEntities[i].zo = prevPositions[i].z;
            this.subEntities[i].xOld = prevPositions[i].x;
            this.subEntities[i].yOld = prevPositions[i].y;
            this.subEntities[i].zOld = prevPositions[i].z;
        }
    }

    private void tickPart(LuffyPartEntity pPart, double xOffset, double yOffset, double zOffset, float yRot) {
        float yBodyRotRadians = -yRot * LogicHelper.TO_RADIANS;
        float cos = MathHelper.cos(yBodyRotRadians);
        float sin = MathHelper.sin(yBodyRotRadians);
        double x = xOffset * (double)cos + zOffset * (double)sin;
        double z = zOffset * (double)cos - xOffset * (double)sin;
        pPart.setPos(this.getX() + x, this.getY() + yOffset, this.getZ() + z);
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
                AxisAlignedBB pArea = this.getBoundingBox();
                int minX = MathHelper.floor(pArea.minX);
                int minY = MathHelper.floor(pArea.minY);
                int minZ = MathHelper.floor(pArea.minZ);
                int maxX = MathHelper.floor(pArea.maxX);
                int maxY = MathHelper.floor(pArea.maxY);
                int maxZ = MathHelper.floor(pArea.maxZ);
                boolean destroyedBlocks = false;

                for(int x = minX; x <= maxX; ++x) {
                    for(int z = minZ; z <= maxZ; ++z) {
                        for(int y = minY; y <= maxY; ++y) {
                            BlockPos blockPos = new BlockPos(x, y, z);
                            BlockState stateAtPos = this.level.getBlockState(blockPos);
                            if (stateAtPos.canEntityDestroy(this.level, blockPos, this) && ForgeEventFactory.onEntityDestroyBlock(this, blockPos, stateAtPos)) {
                                destroyedBlocks = this.level.destroyBlock(blockPos, true, this) || destroyedBlocks;
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

        double y = this.getY() + this.getBbHeight();
        double xDist = 0.0;
        double yDist = targetY - y;
        double zDist = 0.0;
        GiantFistEntity giantFistEntity = new GiantFistEntity(this.level, this, xDist, yDist, zDist);
        giantFistEntity.setOwner(this);

        giantFistEntity.setPosRaw(targetX, y, targetZ);
        this.level.addFreshEntity(giantFistEntity);
    }

    /*
    @Override
    public void performRangedAttack(LivingEntity pTarget, float pDistanceFactor) {
        this.performRangedAttack(pTarget);
    }
     */

    public boolean hurt(LuffyPartEntity pPart, DamageSource pSource, float pDamage) {
        return this.reallyHurt(pSource, pDamage);
    }

    protected boolean reallyHurt(DamageSource pSource, float pAmount) {
        return super.hurt(pSource, pAmount);
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return this.hurt(this.body, pSource, pAmount);
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

    // Need to return false so the regular hitbox is not used for hit detection
    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    /**
     * Methods for {@link IAnimatable}
     */

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GearFiveLuffy> PlayState predicate(AnimationEvent<T> event) {
        if(this.isAttackAnimationInProgress()){
            switch (this.getCurrentAttackType()){
                case STORM:
                    event.getController().setAnimation(STORM_ANIM);
                    break;
                case SHOCKWAVE:
                    event.getController().setAnimation(SHOCKWAVE_ANIM);
                    break;
                case GROUND_PUNCH:
                    event.getController().setAnimation(GROUND_PUNCH_ANIM);
                    break;
                case GIANT_FIST:
                    event.getController().setAnimation(GIANT_FIST_ANIM);
                    break;
                default:
                    break;
            }
        } else if(event.isMoving()){
            event.getController().setAnimation(WALK_ANIM);
        } else{
            event.getController().setAnimation(IDLE_ANIM);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimationFactory getFactory() {
        return this.animationFactory;
    }

    /**
     * {@link AnimatableMeleeAttack} methods
     */

    @Override
    public int getAttackAnimationTick() {
        return this.attackAnimationTick;
    }

    @Override
    public void setAttackAnimationTick(int attackAnimationTick) {
        this.attackAnimationTick = attackAnimationTick;
    }

    @Override
    public LuffyAttackType getCurrentAttackType() {
        return !this.level.isClientSide ? this.currentAttackType : LuffyAttackType.byId(this.entityData.get(DATA_ATTACK_TYPE_ID));
    }

    @Override
    public void setCurrentAttackType(LuffyAttackType attackType) {
        this.currentAttackType = attackType;
        this.entityData.set(DATA_ATTACK_TYPE_ID, (byte)attackType.getId());
    }

    @Override
    public LuffyAttackType getDefaultAttackType() {
        return LuffyAttackType.NONE;
    }

    @Override
    public void performAttack(LivingEntity target, double distanceToTarget) {
        switch (this.getCurrentAttackType()){
            case STORM:
                StormEntity storm = new StormEntity(this.level, target.getX(), target.getY(), target.getZ());
                storm.setOwner(this);
                storm.setRadius(5.0F);
                storm.setDuration(LuffyAttackType.STORM.getAttackAnimationLength());
                this.level.addFreshEntity(storm);
                break;
            case SHOCKWAVE:
                LogicHelper.areaOfEffectAttack((ServerWorld) this.level, this, null, target.getX(), target.getY(), target.getZ(), 5.0F);
                break;
            case GROUND_PUNCH:
                LogicHelper.areaOfEffectAttack((ServerWorld) this.level, this, null, target.getX(), target.getY(), target.getZ(), 2.5F);
                break;
            case GIANT_FIST:
                this.performRangedAttack(target);
                break;
            default:
                break;
        }
    }

}
