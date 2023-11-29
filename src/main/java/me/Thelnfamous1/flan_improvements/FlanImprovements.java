package me.Thelnfamous1.flan_improvements;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.enginehub.worldeditcui.fabric.FabricModWorldEditCUI;
import org.slf4j.Logger;

@Mod(FlanImprovements.MODID)
public class FlanImprovements {
    public static final String MODID = "flan_improvements";
    public static final Logger LOGGER = LogUtils.getLogger();
    private final FabricModWorldEditCUI modWorldEditCUI;

    public FlanImprovements() {
        this.modWorldEditCUI = new FabricModWorldEditCUI();
        this.modWorldEditCUI.onInitialize();
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            FICommands.register(event.getDispatcher());
        });
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> {
            event.enqueueWork(() -> {
               FINetwork.init();
            });
        });
    }

    public static String cuiModIdPrefix(String path){
        return FabricModWorldEditCUI.MOD_ID + path;
    }

    public static String cuiModId(){
        return FabricModWorldEditCUI.MOD_ID;
    }

    public static String cuiModIdSuffix(String path){
        return path + FabricModWorldEditCUI.MOD_ID;
    }
}
