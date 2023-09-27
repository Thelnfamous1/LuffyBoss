package me.infamous.luffy_boss.common.entity.attack;

import me.infamous.luffy_boss.common.LogicHelper;

public enum LuffyAttackType implements AnimatableMeleeAttack.AttackType {
    NONE(0, 0, 0),
    LIGHTNING(1, LogicHelper.secondsToTicks(0.75D), LogicHelper.secondsToTicks(1.5D)),
    SHOCKWAVE(2, LogicHelper.secondsToTicks(1.2D), LogicHelper.secondsToTicks(2.3333D)),
    GROUND_PUNCH(3, LogicHelper.secondsToTicks(0.75D), LogicHelper.secondsToTicks(1.9583D)),
    GIANT_FIST(4, LogicHelper.secondsToTicks(2.0D), LogicHelper.secondsToTicks(4.0D));

    private final int id;
    private final int attackAnimationActionPoint;
    private final int attackAnimationLength;

    LuffyAttackType(int id, int attackAnimationActionPoint, int attackAnimationLength){
        this.id = id;
        this.attackAnimationActionPoint = attackAnimationActionPoint;
        this.attackAnimationLength = attackAnimationLength;
    }

    public static LuffyAttackType byId(int pId) {
        for (LuffyAttackType attackType : values()) {
            if (pId == attackType.id) {
                return attackType;
            }
        }

        return NONE;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public int getAttackAnimationActionPoint() {
        return this.attackAnimationActionPoint;
    }

    @Override
    public int getAttackAnimationLength() {
        return this.attackAnimationLength;
    }
}
