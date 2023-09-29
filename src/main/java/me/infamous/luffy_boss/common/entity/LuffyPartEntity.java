package me.infamous.luffy_boss.common.entity;

import me.infamous.luffy_boss.common.LogicHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.entity.PartEntity;

public class LuffyPartEntity extends PartEntity<GearFiveLuffy> {
    public final String name;
    private final EntitySize size;
    private double xOffset;
    private double yOffset;
    private double zOffset;
    private final boolean bodyPart;

    public LuffyPartEntity(GearFiveLuffy parent, String name, float width, float height, boolean bodyPart) {
        super(parent);
        this.size = EntitySize.scalable(width, height);
        this.refreshDimensions();
        this.name = name;
        this.bodyPart = bodyPart;
    }

    public LuffyPartEntity offset(double xOffset, double yOffset, double zOffset){
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        return this;
    }

    public static void ticker(LuffyPartEntity part){
        GearFiveLuffy parent = part.getParent();
        float yRot = part.bodyPart ? parent.yBodyRot : parent.yRot;
        float yRotRadians = -yRot * LogicHelper.TO_RADIANS;
        float cos = MathHelper.cos(yRotRadians);
        float sin = MathHelper.sin(yRotRadians);
        double xOffset = part.xOffset * (double)cos + part.zOffset * (double)sin;
        double zOffset = part.zOffset * (double)cos - part.xOffset * (double)sin;
        part.setPos(parent.getX() + xOffset, parent.getY() + part.yOffset, parent.getZ() + zOffset);
    }

    public String getPartName() {
        return this.name;
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void readAdditionalSaveData(CompoundNBT pCompound) {

    }

    @Override
    protected void addAdditionalSaveData(CompoundNBT pCompound) {

    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return !this.isInvulnerableTo(pSource) && this.getParent().hurt(this, pSource, pAmount);
    }

    @Override
    public boolean is(Entity pEntity) {
        return this == pEntity || this.getParent() == pEntity;
    }

    @Override
    public IPacket<?> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntitySize getDimensions(Pose pPose) {
        return this.size;
    }
}
