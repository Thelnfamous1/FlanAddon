package org.enginehub.worldeditcui.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Thelnfamous1.flan_improvements.FlanImprovements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.enginehub.worldeditcui.WorldEditCUI;
import org.enginehub.worldeditcui.config.CUIConfiguration;
import org.enginehub.worldeditcui.event.listeners.CUIListenerChannel;
import org.enginehub.worldeditcui.event.listeners.CUIListenerWorldRender;
import org.enginehub.worldeditcui.fabric.mixins.MinecraftAccess;
import org.enginehub.worldeditcui.render.OptifinePipelineProvider;
import org.enginehub.worldeditcui.render.PipelineProvider;
import org.enginehub.worldeditcui.render.VanillaPipelineProvider;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Fabric mod entrypoint
 *
 * @author Mark Vainomaa
 */
public final class FabricModWorldEditCUI {
    private static final int DELAYED_HELO_TICKS = 10;

    public static final String MOD_ID = "worldeditcui";
    private static FabricModWorldEditCUI instance;

    private static final String KEYBIND_CATEGORY_WECUI = FlanImprovements.cuiModIdSuffix("key.categories.");
    private final KeyMapping keyBindToggleUI = key("toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindClearSel = key("clear", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);
    private final KeyMapping keyBindChunkBorder = key("chunk", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN);

    private static final List<PipelineProvider> RENDER_PIPELINES = List.of(
            new OptifinePipelineProvider(),
            new VanillaPipelineProvider()
    );

    private WorldEditCUI controller;
    private CUIListenerWorldRender worldRenderListener;
    private CUIListenerChannel channelListener;

    private Level lastWorld;
    private LocalPlayer lastPlayer;

    private boolean visible = true;
    private int delayedHelo = 0;

    /**
     * Register a key binding
     *
     * @param name id, will be used as a localization key under {@code key.worldeditcui.<name>}
     * @param type type
     * @param code default value
     * @return new, registered keybinding in the mod category
     */
    private static KeyMapping key(final String name, final InputConstants.Type type, final int code) {
        //return KeyBindingHelper.registerKeyBinding(new KeyMapping("key." + MOD_ID + '.' + name, type, code, KEYBIND_CATEGORY_WECUI));
        return new KeyMapping("key." + FlanImprovements.cuiModId() + '.' + name, type, code, KEYBIND_CATEGORY_WECUI);
    }

    //@Override
    public void onInitialize() {
        if (Boolean.getBoolean("wecui.debug.mixinaudit")) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }

        instance = this;

        // Set up event listeners
        //ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent event) -> {
            if(event.phase == TickEvent.Phase.END) this.onTick(Minecraft.getInstance());
        });

        //ClientLifecycleEvents.CLIENT_STARTED.register(this::onGameInitDone);
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLClientSetupEvent event) -> {
            event.enqueueWork(() -> this.onGameInitDone(Minecraft.getInstance()));
        });
        //CUINetworking.subscribeToCuiPacket(this::onPluginMessage);
        //ClientPlayConnectionEvents.JOIN.register(this::onJoinGame);
        MinecraftForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
            this.onJoinGame(event.getPlayer().connection, event.getPlayer(), Minecraft.getInstance());
        });
        /*
        WorldRenderEvents.AFTER_TRANSLUCENT.register(ctx -> {
            if (ctx.advancedTranslucency()) {
                try {
                    RenderSystem.getModelViewStack().pushPose();
                    RenderSystem.getModelViewStack().mulPoseMatrix(ctx.matrixStack().last().pose());
                    RenderSystem.applyModelViewMatrix();
                    ctx.worldRenderer().getTranslucentTarget().bindWrite(false);
                    this.onPostRenderEntities(ctx);
                } finally {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                    RenderSystem.getModelViewStack().popPose();
                }
            }
        });
         */
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if(event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) return;
            if (Minecraft.useShaderTransparency()) {
                try {
                    RenderSystem.getModelViewStack().pushPose();
                    RenderSystem.getModelViewStack().mulPoseMatrix(event.getPoseStack().last().pose());
                    RenderSystem.applyModelViewMatrix();
                    event.getLevelRenderer().getTranslucentTarget().bindWrite(false);
                    this.onPostRenderEntities(event);
                } finally {
                    Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
                    RenderSystem.getModelViewStack().popPose();
                }
            }
        });
        /*
        WorldRenderEvents.LAST.register(ctx -> {
            if (!ctx.advancedTranslucency()) {
                try {
                    RenderSystem.getModelViewStack().pushPose();
                    RenderSystem.getModelViewStack().mulPoseMatrix(ctx.matrixStack().last().pose());
                    RenderSystem.applyModelViewMatrix();
                    this.onPostRenderEntities(ctx);
                } finally {
                    RenderSystem.getModelViewStack().popPose();
                    RenderSystem.applyModelViewMatrix();
                }
            }
        });
         */
        MinecraftForge.EVENT_BUS.addListener((RenderLevelStageEvent event) -> {
            if(event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) return; // last stage?
            if (!Minecraft.useShaderTransparency()) {
                try {
                    RenderSystem.getModelViewStack().pushPose();
                    RenderSystem.getModelViewStack().mulPoseMatrix(event.getPoseStack().last().pose());
                    RenderSystem.applyModelViewMatrix();
                    this.onPostRenderEntities(event);
                } finally {
                    RenderSystem.getModelViewStack().popPose();
                    RenderSystem.applyModelViewMatrix();
                }
            }
        });
    }

    private void onTick(final Minecraft mc) {
        final CUIConfiguration config = this.controller.getConfiguration();
        final boolean inGame = mc.player != null;
        final boolean clock = ((MinecraftAccess) mc).getTimer().partialTick > 0;

        if (inGame && mc.screen == null) {
            while (this.keyBindToggleUI.consumeClick()) {
                this.visible = !this.visible;
            }

            while (this.keyBindClearSel.consumeClick()) {
                if (mc.player != null) {
                    mc.player.commandUnsigned("/sel");
                }

                if (config.isClearAllOnKey()) {
                    this.controller.clearRegions();
                }
            }

            while (this.keyBindChunkBorder.consumeClick()) {
                this.controller.toggleChunkBorders();
            }
        }

        if (inGame && clock && this.controller != null) {
            if (mc.level != this.lastWorld || mc.player != this.lastPlayer) {
                this.lastWorld = mc.level;
                this.lastPlayer = mc.player;

                this.controller.getDebugger().debug("World change detected, sending new handshake");
                this.controller.clear();
                this.helo(mc.getConnection());
                this.delayedHelo = FabricModWorldEditCUI.DELAYED_HELO_TICKS;
                if (mc.player != null && config.isPromiscuous()) {
                    mc.player.commandUnsigned("we cui"); // Tricks WE to send the current selection
                }
            }

            if (this.delayedHelo > 0) {
                this.delayedHelo--;
                if (this.delayedHelo == 0) {
                    this.helo(mc.getConnection());
                }
            }
        }
    }

    public void onPluginMessage(final Minecraft client, final FriendlyByteBuf data) {
        try {
            final int readableBytes = data.readableBytes();
            if (readableBytes > 0) {
                final String stringPayload = data.toString(0, data.readableBytes(), StandardCharsets.UTF_8);
                client.execute(() -> this.channelListener.onMessage(stringPayload));
            } else {
                this.getController().getDebugger().debug("Warning, invalid (zero length) payload received from server");
            }
        } catch (final Exception ex) {
            this.getController().getDebugger().info("Error decoding payload from server", ex);
        }
    }

    public void onGameInitDone(final Minecraft client) {
        this.controller = new WorldEditCUI();
        this.controller.initialise(client);
        this.worldRenderListener = new CUIListenerWorldRender(this.controller, client, RENDER_PIPELINES);
        this.channelListener = new CUIListenerChannel(this.controller);
    }

    public void onJoinGame(final ClientPacketListener handler, final LocalPlayer sender, final Minecraft client) {
        this.visible = true;
        this.controller.getDebugger().debug("Joined game, sending initial handshake");
        this.helo(handler);
    }

    public void onPostRenderEntities(final RenderLevelStageEvent ctx) {
        if (this.visible) {
            this.worldRenderListener.onRender(ctx.getPartialTick());
        }
    }

    private void helo(ClientPacketListener handler) {
        /*
        final String message = "v|" + WorldEditCUI.PROTOCOL_VERSION;
        final ByteBuf buffer = Unpooled.copiedBuffer(message, StandardCharsets.UTF_8);
        CUINetworking.send(handler, new FriendlyByteBuf(buffer));
         */
    }

    public WorldEditCUI getController()
    {
        return this.controller;
    }

    public static FabricModWorldEditCUI getInstance() {
        return instance;
    }
}
