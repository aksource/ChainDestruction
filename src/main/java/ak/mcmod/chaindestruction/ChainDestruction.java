package ak.mcmod.chaindestruction;

import ak.mcmod.ak_lib.common.ForgeModEntryPoint;
import ak.mcmod.chaindestruction.client.ClientUtils;
import ak.mcmod.chaindestruction.event.*;
import ak.mcmod.chaindestruction.network.PacketHandler;
import ak.mcmod.chaindestruction.util.ConfigUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(ChainDestruction.MOD_ID)
public class ChainDestruction extends ForgeModEntryPoint {

  public static final String MOD_ID = "chaindestruction";
  public static final DigTaskEvents digTaskEvent = new DigTaskEvents();

  @Override
  protected void setupConstructor(IEventBus iEventBus) {
    MinecraftForge.EVENT_BUS.register(EntityEvents.class);
    MinecraftForge.EVENT_BUS.register(CapabilityEvents.class);
    MinecraftForge.EVENT_BUS.register(BlockEvents.class);
    MinecraftForge.EVENT_BUS.register(CommandEvents.class);
    ModLoadingContext.get().registerConfig(Type.COMMON, ConfigUtils.CONFIG_SPEC);
    iEventBus.register(ConfigUtils.class);
  }

  @Override
  protected void setupCommon(final FMLCommonSetupEvent event) {
    PacketHandler.init();
  }

  @Override
  protected void setupClient(final FMLClientSetupEvent event) {
    super.setupClient(event);
    ClientUtils.registerClientInfo();
  }
}