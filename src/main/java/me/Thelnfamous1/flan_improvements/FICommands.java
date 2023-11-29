package me.Thelnfamous1.flan_improvements;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.flemmli97.flan.api.permission.PermissionRegistry;
import io.github.flemmli97.flan.claim.Claim;
import io.github.flemmli97.flan.claim.ClaimStorage;
import io.github.flemmli97.flan.claim.PermHelper;
import io.github.flemmli97.flan.config.ConfigHandler;
import io.github.flemmli97.flan.event.ItemInteractEvents;
import io.github.flemmli97.flan.platform.integration.permissions.PermissionNodeHandler;
import io.github.flemmli97.flan.player.EnumEditMode;
import io.github.flemmli97.flan.player.PlayerClaimData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FICommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher){
        dispatcher.register(Commands.literal("rg")
                .then(Commands.literal("claim")
                        .requires((src) -> PermissionNodeHandler.INSTANCE.perm(src, "flan.claim.create") && PermissionNodeHandler.INSTANCE.perm(src, "flan.command.name"))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> createClaim(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("delete")
                        .requires((src) -> PermissionNodeHandler.INSTANCE.perm(src, "flan.command.delete"))
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> deleteNamedClaim(context.getSource().getPlayerOrException(), StringArgumentType.getString(context, "name")))))
                .then(Commands.literal("list")
                        .requires((src) -> PermissionNodeHandler.INSTANCE.perm(src, "flan.command.list"))
                        .executes(context -> listClaims(context.getSource().getServer(), context.getSource().getPlayerOrException())))
        );
    }

    private static int createClaim(ServerPlayer player, String name) {
        PlayerClaimData data = PlayerClaimData.get(player);
        BlockPos from = data.editingCorner();
        BlockPos to = ((AdditionalPlayerClaimData)data).editingCorner0();
        // always need to reset editing corners
        clearEditingCorners(data);
        if (!ItemInteractEvents.canClaimWorld(player.getLevel(), player)) {
            return 0;
        } else {
            if(from == null || to == null){
                return 0; // TODO: Alert player that selection needs to be made
            }
            Availability availability = getNameAvailability(player, name, from, to);
            switch (availability){
                case AVAILABLE -> {
                    ClaimStorage storage = ClaimStorage.get(player.getLevel());
                    if(storage.createClaim(from, to, player)){
                        Claim claimAt = storage.getClaimAt(from);
                        claimAt.setClaimName(name);
                        player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimNameSet"), name), ChatFormatting.GOLD), false);
                        return 1;
                    } else{
                        return 0;
                    }
                }
                case UNAVAILABLE -> {
                    player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("claimNameUsed"), ChatFormatting.DARK_RED), false);
                    return 0;
                }
                default -> {
                    player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("noPermission"), ChatFormatting.DARK_RED), false);
                    return 0;
                }
            }
        }
    }

    private static void clearEditingCorners(PlayerClaimData data) {
        data.setEditingCorner(null);
        ((AdditionalPlayerClaimData) data).setEditingCorner0(null);
    }

    private static Availability getNameAvailability(ServerPlayer player, final String name, BlockPos from, BlockPos to) {
        PlayerClaimData data = PlayerClaimData.get(player);
        if (data.getEditMode() == EnumEditMode.DEFAULT) {
             return Availability.from(ClaimStorage.get(player.getLevel())
                     .allClaimsFromPlayer(player.getUUID())
                     .stream()
                     .map(Claim::getClaimName)
                     .noneMatch((namex) -> namex.equals(name)));
        } else {
            Availability availability = getNameAvailabilityAt(player, name, from);
            return availability.getValue() ? getNameAvailabilityAt(player, name, to) : availability;
        }
    }

    private static Availability getNameAvailabilityAt(ServerPlayer player, String name, BlockPos from) {
        Claim claim = ClaimStorage.get(player.getLevel()).getClaimAt(from);
        Claim sub = claim.getSubClaim(from);
        if (sub != null && (claim.canInteract(player, PermissionRegistry.EDITPERMS, from) || sub.canInteract(player, PermissionRegistry.EDITPERMS, from))) {
            return Availability.from(claim.getAllSubclaims()
                    .stream()
                    .map(Claim::getClaimName)
                    .noneMatch((namex) -> namex.equals(name)));
        } else if (claim.canInteract(player, PermissionRegistry.EDITPERMS, from)) {
            return Availability.from(ClaimStorage.get(player.getLevel())
                    .allClaimsFromPlayer(claim.getOwner())
                    .stream()
                    .map(Claim::getClaimName)
                    .noneMatch((namex) -> namex.equals(name)));
        } else {
            return Availability.UNKNOWN;
        }
    }

    private static int deleteNamedClaim(ServerPlayer player, String name) {
        ClaimStorage storage = ClaimStorage.get(player.getLevel());
        Optional<Claim> namedClaim = storage.allClaimsFromPlayer(player.getUUID())
                .stream()
                .filter((c) -> c.getClaimName().equals(name))
                .findAny();
        if(namedClaim.isEmpty()){
            PermHelper.noClaimMessage(player);
            return 0;
        }
        boolean check = PermHelper.check(player, player.blockPosition(), namedClaim.get(), PermissionRegistry.EDITCLAIM, (b) -> {
            if (b.isEmpty()) {
                PermHelper.noClaimMessage(player);
            } else if (!b.get()) {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaimError"), ChatFormatting.DARK_RED), false);
            } else {
                player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("deleteClaim"), ChatFormatting.RED), false);
            }

        });
        if (!check) {
            return 0;
        } else {
            storage.deleteClaim(namedClaim.get(), true, PlayerClaimData.get(player).getEditMode(), player.getLevel());
            return 1;
        }
    }

    private static int listClaims(MinecraftServer server, ServerPlayer player) {
        Map<Level, Collection<Claim>> claims = new HashMap<>();

        for (ServerLevel world : server.getAllLevels()) {
            ClaimStorage storage = ClaimStorage.get(world);
            claims.put(world, storage.allClaimsFromPlayer(player.getUUID()));
        }

        if (ConfigHandler.config.maxClaimBlocks != -1) {
            PlayerClaimData data = PlayerClaimData.get(player);
            player.displayClientMessage(PermHelper.simpleColoredText(String.format(ConfigHandler.langManager.get("claimBlocksFormat"), data.getClaimBlocks(), data.getAdditionalClaims(), data.usedClaimBlocks(), data.remainingClaimBlocks()), ChatFormatting.GOLD), false);
        }

        player.displayClientMessage(PermHelper.simpleColoredText(ConfigHandler.langManager.get("listClaims"), ChatFormatting.GOLD), false);

        for (Map.Entry<Level, Collection<Claim>> claimsForLevel : claims.entrySet()) {
            for (Claim claim : claimsForLevel.getValue()) {
                player.displayClientMessage(PermHelper.simpleColoredText(claimsForLevel.getKey().dimension().location() + " # " + claim.formattedClaim(), ChatFormatting.YELLOW), false);
            }
        }

        return 1;
    }

}
