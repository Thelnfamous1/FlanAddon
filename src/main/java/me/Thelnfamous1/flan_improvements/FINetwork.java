package me.Thelnfamous1.flan_improvements;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class FINetwork {
    private static final ResourceLocation CHANNEL_NAME = new ResourceLocation(FlanImprovements.MODID, "cui");
    private static final String PROTOCOL_VERSION = "1.0";
    private static final SimpleChannel SYNC_CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME, () -> "1.0",
            versionFromServer -> PROTOCOL_VERSION.equals(versionFromServer) || NetworkRegistry.ACCEPTVANILLA.equals(versionFromServer),
            versionFromClient -> PROTOCOL_VERSION.equals(versionFromClient) || NetworkRegistry.ACCEPTVANILLA.equals(versionFromClient)
    );

    static void init() {
    }

    /*
    public static void onPacketData(CustomPayloadEvent event) {
        ServerPlayer player = event.getSource().getSender();
        LocalSession session = ForgeWorldEdit.inst.getSession(player);
        String text = event.getPayload().toString(StandardCharsets.UTF_8);
        final ForgePlayer actor = adaptPlayer(player);
        session.handleCUIInitializationMessage(text, actor);
    }

    public static void send(Connection connection, FriendlyByteBuf friendlyByteBuf) {
        HANDLER.send(friendlyByteBuf, connection);
    }
     */
}
