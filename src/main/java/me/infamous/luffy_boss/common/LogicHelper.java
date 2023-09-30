package me.infamous.luffy_boss.common;

import me.infamous.luffy_boss.common.entity.attack.AreaOfEffectAttack;
import me.infamous.luffy_boss.common.network.LBNetwork;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

public class LogicHelper {

    public static final float TO_RADIANS = (float) Math.PI / 180F;

    public static int secondsToTicks(double seconds){
        return (int)Math.ceil(seconds * 20);
    }

    public static int pixelsToBlocks(int pixels){
        return Math.floorDiv(pixels, 16);
    }

    public static AreaOfEffectAttack areaOfEffectAttack(ServerWorld level, Entity attacker, @Nullable DamageSource pDamageSource, double pX, double pY, double pZ, float pSize, AreaOfEffectAttack.KnockbackState knockbackState) {
        AreaOfEffectAttack areaOfEffectAttack = new AreaOfEffectAttack(level, attacker, pDamageSource, pX, pY, pZ, pSize, knockbackState);
        areaOfEffectAttack.attack();
        areaOfEffectAttack.finalizeAttack(false);

        for(ServerPlayerEntity player : level.players()) {
            if (player.distanceToSqr(pX, pY, pZ) < 4096.0D) {
                LBNetwork.syncAOEAttack(PacketDistributor.PLAYER.with(() -> player), pX, pY, pZ, pSize, areaOfEffectAttack.getHitPlayers().get(player));
            }
        }

        return areaOfEffectAttack;
    }

    public static LivingEntity getLivingEntity(@Nullable Entity entity) {
       if(entity instanceof LivingEntity) return (LivingEntity) entity;
       return null;
    }
}
