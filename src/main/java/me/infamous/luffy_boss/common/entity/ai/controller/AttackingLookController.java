package me.infamous.luffy_boss.common.entity.ai.controller;

import me.infamous.luffy_boss.common.entity.ai.AnimatableMeleeAttack;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.controller.LookController;

public class AttackingLookController<T extends MobEntity & AnimatableMeleeAttack<?>> extends LookController {
    private final T attacker;

    public AttackingLookController(T attacker) {
        super(attacker);
        this.attacker = attacker;
    }

    @Override
    public void tick() {
        if(true || !this.attacker.isAttackAnimationInProgress()){
            super.tick();
        }
    }
}
