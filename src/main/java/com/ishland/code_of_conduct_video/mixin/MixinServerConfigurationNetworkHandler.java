package com.ishland.code_of_conduct_video.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.listener.TickablePacketListener;
import net.minecraft.network.packet.s2c.config.CodeOfConductS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.SendCodeOfConductTask;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class MixinServerConfigurationNetworkHandler extends ServerCommonNetworkHandler
        implements ServerCommonPacketListener, TickablePacketListener {

    @Unique
    private int cocv$counter;

    @Shadow
    @Nullable
    private CommonListenerCookie clientData;

    public MixinServerConfigurationNetworkHandler(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
        super(server, connection, clientData);
    }

    @WrapOperation(
            method = "queueSendResourcePackTask",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/MinecraftServer;getCodeOfConducts()Ljava/util/Map;"
            )
    )
    private Map<String, String> addDummyCoC(MinecraftServer server, Operation<Map<String, String>> original) {
        Map<String, String> result = original.call(server);
        if (result.isEmpty()) {
            return Map.of("en_us", "Starting video playback...");
        }
        return result;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        if (this.clientData instanceof SendCodeOfConductTask) {
            if (this.cocv$counter < TheMod.videoFrames.length) {
                String frame = TheMod.videoFrames[this.cocv$counter];
                if (frame != null) {
                    this.send(new CodeOfConductS2CPacket(
                            "Frame: " + (this.cocv$counter + 1) + "/" + TheMod.videoFrames.length + "\n" + frame
                    ));
                }
            }
            this.cocv$counter++;
        }
    }
        }
