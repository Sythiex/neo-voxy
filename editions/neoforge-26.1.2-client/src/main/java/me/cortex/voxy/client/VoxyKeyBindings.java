package me.cortex.voxy.client;

import com.mojang.blaze3d.platform.InputConstants;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.core.IVoxyRenderSystemHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = "voxy", value = Dist.CLIENT)
public final class VoxyKeyBindings {
   private static final KeyMapping.Category CATEGORY =
         new KeyMapping.Category(Identifier.fromNamespaceAndPath("voxy", "controls"));
   private static final KeyMapping RELOAD = new KeyMapping("key.voxy.reload", InputConstants.Type.KEYSYM,
         InputConstants.UNKNOWN.getValue(), CATEGORY);
   private static final KeyMapping TOGGLE_RENDERING = new KeyMapping("key.voxy.toggle_rendering", InputConstants.Type.KEYSYM,
         InputConstants.UNKNOWN.getValue(), CATEGORY);

   private VoxyKeyBindings() {}

   @SubscribeEvent
   public static void onClientTick(ClientTickEvent.Post event) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.level == null) return;
      if (consume(RELOAD)) {
         message(mc, VoxyCommands.reloadInstance() ? "message.voxy.reloaded" : "message.voxy.reload_unavailable");
      }
      if (consume(TOGGLE_RENDERING)) {
            VoxyConfig.CONFIG.enableRendering = !VoxyConfig.CONFIG.enableRendering;
            VoxyConfig.CONFIG.save();
            applyRenderingState();
          message(mc, VoxyConfig.CONFIG.enableRendering
                ? "message.voxy.rendering_enabled" : "message.voxy.rendering_disabled");
      }
   }

   private static void applyRenderingState() {
      var holder = IVoxyRenderSystemHolder.getNullableHolder();
      if (holder == null) return;
      boolean enabled = VoxyConfig.CONFIG.isRenderingEnabled();
      boolean active = holder.voxy$getRenderSystem() != null;
      if (enabled == active) return;
      if (enabled) holder.voxy$createRenderer();
      else holder.voxy$shutdownRenderer();
   }

   private static boolean consume(KeyMapping mapping) {
      if (!mapping.consumeClick()) return false;
      while (mapping.consumeClick()) {}
      return true;
   }

   private static void message(Minecraft mc, String key) {
      if (mc.player != null) mc.gui.setOverlayMessage(Component.translatable(key), false);
   }

   public static void register(RegisterKeyMappingsEvent event) {
      event.registerCategory(CATEGORY);
      event.register(RELOAD);
      event.register(TOGGLE_RENDERING);
   }
}
