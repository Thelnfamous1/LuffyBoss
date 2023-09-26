package me.infamous.luffy_boss.client.events;

import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.client.renderer.gear_five_luffy.GearFiveLuffyRenderer;
import me.infamous.luffy_boss.common.registry.LBEntityTypes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LuffyBoss.MODID, value = Dist.CLIENT)
public class ModClientEventHandler {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(LBEntityTypes.GEAR_FIVE_LUFFY.get(), GearFiveLuffyRenderer::new);

    }
}
