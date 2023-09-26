package me.infamous.luffy_boss.common.events;

import me.infamous.luffy_boss.LuffyBoss;
import me.infamous.luffy_boss.common.entity.GearFiveLuffy;
import me.infamous.luffy_boss.common.registry.LBEntityTypes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = LuffyBoss.MODID)
public class ModEventHandler {

    @SubscribeEvent
    static void onEntityAttributeCreation(EntityAttributeCreationEvent event){
        event.put(LBEntityTypes.GEAR_FIVE_LUFFY.get(), GearFiveLuffy.createAttributes().build());
    }

}
