package me.infamous.luffy_boss.common.events;

import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.entity.GearFiveLuffy;
import me.infamous.luffy_boss.common.network.LBNetwork;
import me.infamous.luffy_boss.common.registry.LBEntityTypes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LuffyBoss.MODID)
public class ModEventHandler {

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(LBEntityTypes.GEAR_FIVE_LUFFY.get(), GearFiveLuffy.createAttributes().build());
    }

    @SubscribeEvent
    static void onCommonSetup(FMLCommonSetupEvent event){
        event.enqueueWork(() -> {
            LBNetwork.initialize();
        });
    }

}
