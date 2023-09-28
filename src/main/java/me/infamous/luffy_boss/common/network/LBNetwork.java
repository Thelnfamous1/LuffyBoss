package me.infamous.luffy_boss.common.network;

import me.infamous.luffy_boss.LuffyBoss;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import javax.annotation.Nullable;

public class LBNetwork {
    private static final ResourceLocation KEY = new ResourceLocation(LuffyBoss.MODID, "main");

    private static final String PROTOCOL_VERSION = "0"; // This should be updated whenever packets change
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(KEY, () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void initialize() {
        // This would get incremented for every new message,
        // but we only have one right now
        int id = -1;

        // Server --> Client
        ClientboundAOEAttackPacket.register(CHANNEL, ++id);

    }

    public static void syncAOEAttack(PacketDistributor.PacketTarget target, double pX, double pY, double pZ, float pSize, @Nullable Vector3d knockbackVec) {
        CHANNEL.send(target, new ClientboundAOEAttackPacket(pX, pY, pZ, pSize, knockbackVec));
    }
}
