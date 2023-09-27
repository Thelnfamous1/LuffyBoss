package me.infamous.luffy_boss.common.network;

import me.infamous.luffy_boss.common.entity.attack.AreaOfEffectAttack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.Supplier;

public class ClientboundAOEAttackPacket {
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final float knockbackX;
    private final float knockbackY;
    private final float knockbackZ;

    public ClientboundAOEAttackPacket(double pX, double pY, double pZ, float pPower, Vector3d knockbackVec) {
        this(pX, pY, pZ, pPower, (float) knockbackVec.x, (float) knockbackVec.y, (float) knockbackVec.z);
    }

    public ClientboundAOEAttackPacket(double pX, double pY, double pZ, float pPower, float knockbackX, float knockbackY, float knockbackZ) {
        this.x = pX;
        this.y = pY;
        this.z = pZ;
        this.power = pPower;
        this.knockbackX = knockbackX;
        this.knockbackY = knockbackY;
        this.knockbackZ = knockbackZ;
    }

    public static void register(SimpleChannel channel, int id) {
        channel.registerMessage(
                id,
                ClientboundAOEAttackPacket.class,
                ClientboundAOEAttackPacket::encode,
                ClientboundAOEAttackPacket::decode,
                ClientboundAOEAttackPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }

    private static ClientboundAOEAttackPacket decode(PacketBuffer buf) {
        double x = buf.readFloat();
        double y = buf.readFloat();
        double z = buf.readFloat();

        float power = buf.readFloat();

        float knockbackX = buf.readFloat();
        float knockbackY = buf.readFloat();
        float knockbackZ = buf.readFloat();
        return new ClientboundAOEAttackPacket(x, y, z, power, knockbackX, knockbackY, knockbackZ);
    }

    private void encode(PacketBuffer pBuffer) {
        pBuffer.writeFloat((float)this.x);
        pBuffer.writeFloat((float)this.y);
        pBuffer.writeFloat((float)this.z);

        pBuffer.writeFloat(this.power);

        pBuffer.writeFloat(this.knockbackX);
        pBuffer.writeFloat(this.knockbackY);
        pBuffer.writeFloat(this.knockbackZ);
    }

    private void handle(Supplier<NetworkEvent.Context> sup) {
        final NetworkEvent.Context ctx = sup.get();
        ctx.enqueueWork(() -> {
            AreaOfEffectAttack areaOfEffectAttack = new AreaOfEffectAttack(Minecraft.getInstance().level, this.x, this.y, this.z, this.power);
            areaOfEffectAttack.finalizeAttack(true);
            Minecraft.getInstance().player.setDeltaMovement(Minecraft.getInstance().player.getDeltaMovement().add(this.knockbackX, this.knockbackY, this.knockbackZ));
        });
        ctx.setPacketHandled(true);
    }
}