package me.infamous.luffy_boss.mixin;

import me.infamous.luffy_boss.common.entity.LuffyPartEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CUseEntityPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(ServerPlayNetHandler.class)
public class ServerPlayNetHandlerMixin {

    @Shadow public ServerPlayerEntity player;

    // This is a dirty hack that I used as a last resort due to the headaches Luffy's multipart entities were causing me
    // Using an event handler for attacking as a player, Luffy's multipart entities can be as far away as over 15 blocks, even though the client says they are 4-5 blocks away!
    @ModifyConstant(method = "handleInteract", constant = @Constant(doubleValue = 36.0D))
    private double modifyMaxInteractDistSqr(double constant, CUseEntityPacket pPacket){
        if(pPacket.getTarget(this.player.getLevel()) instanceof LuffyPartEntity){
            return Double.MAX_VALUE; // arbitrary value, just needed to be significantly greater
        }
        return constant;
    }

}
