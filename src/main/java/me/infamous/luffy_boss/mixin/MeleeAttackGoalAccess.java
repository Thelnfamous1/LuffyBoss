package me.infamous.luffy_boss.mixin;

import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MeleeAttackGoal.class)
public interface MeleeAttackGoalAccess {

    @Accessor("ticksUntilNextAttack")
    void luffy_boss_setTicksUntilNextAttack(int ticksUntilNextAttack);
}
