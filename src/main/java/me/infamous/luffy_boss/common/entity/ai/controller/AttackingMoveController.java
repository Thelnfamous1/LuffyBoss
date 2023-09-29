package me.infamous.luffy_boss.common.entity.ai.controller;

import me.infamous.luffy_boss.common.entity.ai.AnimatableMeleeAttack;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.MovementController;

public class AttackingMoveController<T extends MobEntity & AnimatableMeleeAttack<?>> extends MovementController {
    private final T attacker;

    public AttackingMoveController(T attacker) {
        super(attacker);
        this.attacker = attacker;
    }

    @Override
    public void tick() {
        if(!this.attacker.isAttackAnimationInProgress()){
            super.tick();
        }
    }
}
