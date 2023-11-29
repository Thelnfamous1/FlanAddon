package org.enginehub.worldeditcui.fabric;

import me.Thelnfamous1.flan_improvements.FlanImprovements;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;

/**
 * Networking wrappers to integrate nicely with MultiConnect.
 *
 * <p>These methods generally first call </p>
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = FlanImprovements.MODID)
final class CUINetworking {

    private static boolean MULTICONNECT_AVAILABLE; //= FabricLoader.getInstance().isModLoaded("multiconnect");
    private static boolean VIAFABRICPLUS_AVAILABLE; //= FabricLoader.getInstance().isModLoaded("viafabricplus");

    static final String CHANNEL_LEGACY = "WECUI"; // pre-1.13 channel name
    static final ResourceLocation CHANNEL_WECUI = new ResourceLocation("worldedit", "cui");

    private CUINetworking() {
    }

    @SubscribeEvent
    static void onLoadComplete(FMLLoadCompleteEvent event){
        MULTICONNECT_AVAILABLE = ModList.get().isLoaded("multiconnect");
        VIAFABRICPLUS_AVAILABLE = ModList.get().isLoaded("viafabricplus");
    }

    /*
    public static void send(final ClientPacketListener handler, final FriendlyByteBuf codec) {
        if (!MULTICONNECT_AVAILABLE) {
            //ClientPlayNetworking.send(CHANNEL_WECUI, codec);
            return;
        }

        sendUnchecked(handler, codec);
    }

    private static void sendUnchecked(final ClientPacketListener handler, final FriendlyByteBuf data) {
        final MultiConnectAPI api = MultiConnectAPI.instance();
        if (api.getProtocolVersion() <= Protocols.V1_12_2) {
            // Legacy string-based
            api.forceSendStringCustomPayload(handler, CHANNEL_LEGACY, data);
        } else {
            api.forceSendCustomPayload(handler, CHANNEL_WECUI, data);
        }
    }


    /*
    public static void subscribeToCuiPacket(final ClientPlayNetworking.PlayChannelHandler handler) {
        ClientPlayNetworking.registerGlobalReceiver(CHANNEL_WECUI, handler);
        if (MULTICONNECT_AVAILABLE) {
            subscribeToCuiPacketUnchecked(handler);
        }
        if (VIAFABRICPLUS_AVAILABLE) {
            ViaFabricPlusHook.enable();
        }
    }

    private static void subscribeToCuiPacketUnchecked(final ClientPlayNetworking.PlayChannelHandler handler) {
        MultiConnectAPI.instance().addClientboundStringCustomPayloadListener(event -> {
            if (event.getChannel().equals(CHANNEL_LEGACY)) {
                handler.receive(Minecraft.getInstance(), event.getNetworkHandler(), event.getData(), ClientPlayNetworking.getSender());
            }
        });
        MultiConnectAPI.instance().addClientboundIdentifierCustomPayloadListener(event -> {
            if (event.getChannel().equals(CHANNEL_WECUI)) {
                handler.receive(Minecraft.getInstance(), event.getNetworkHandler(), event.getData(), ClientPlayNetworking.getSender());
            }
        });
    }
     */

}
