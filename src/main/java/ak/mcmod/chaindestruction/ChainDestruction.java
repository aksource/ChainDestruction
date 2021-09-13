package ak.mcmod.chaindestruction;

import ak.mcmod.chaindestruction.client.ClientUtils;
import ak.mcmod.chaindestruction.event.DigTaskEvent;
import ak.mcmod.chaindestruction.event.InteractBlockHook;
import ak.mcmod.chaindestruction.capability.CDPlayerStatus;
import ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler;
import ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler;
import ak.mcmod.chaindestruction.capability.CapabilityEventHook;
import ak.mcmod.chaindestruction.command.CommandCopyRtoLCDStatus;
import ak.mcmod.chaindestruction.command.CommandResetCDPlayerStatus;
import ak.mcmod.chaindestruction.command.CommandShowItemCDStatus;
import ak.mcmod.chaindestruction.command.CommandShowPlayerCDStatus;
import ak.mcmod.chaindestruction.network.PacketHandler;
import ak.mcmod.chaindestruction.util.ConfigUtils;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ChainDestruction.MOD_ID)
public class ChainDestruction {

  public static final String MOD_ID = "chaindestruction";
  public static final InteractBlockHook interactBlockHook = new InteractBlockHook();
  public static final DigTaskEvent digTaskEvent = new DigTaskEvent();

  public ChainDestruction() {
    final IEventBus modEventBus =
        FMLJavaModLoadingContext.get().getModEventBus();
    modEventBus.addListener(this::preInit);
    modEventBus.addListener(this::doClientStuff);
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(new CapabilityEventHook());
    MinecraftForge.EVENT_BUS.register(interactBlockHook);
    ModLoadingContext.get().registerConfig(Type.COMMON, ConfigUtils.configSpec);
    modEventBus.register(ConfigUtils.class);
  }

  private void preInit(final FMLCommonSetupEvent event) {
    PacketHandler.init();
    CapabilityCDPlayerStatusHandler.register();
    CapabilityCDItemStackStatusHandler.register();
  }

  private void doClientStuff(final FMLClientSetupEvent event) {
    ClientUtils.registerClientInfo();
  }

  private void registerCommand(final RegisterCommandsEvent event) {
    CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();
    CommandCopyRtoLCDStatus.register(commandDispatcher);
    CommandResetCDPlayerStatus.register(commandDispatcher);
    CommandShowItemCDStatus.register(commandDispatcher);
    CommandShowPlayerCDStatus.register(commandDispatcher);
  }

  @SubscribeEvent
  public void joinInWorld(final EntityJoinWorldEvent event) {
    if (!event.getWorld().isClientSide && event.getEntity() instanceof PlayerEntity) {
      PlayerEntity player = (PlayerEntity) event.getEntity();
      CDPlayerStatus.get(player).ifPresent(status -> {
        String mode = status.isPrivateRegisterMode() ? "Private Register" : "Normal";
        String s = String
            .format("ChainDestruction Info Mode:%s, TreeMode:%b, Range:%d", mode,
                status.isTreeMode(),
                status.getMaxDestroyedBlock());
        player.sendMessage(new StringTextComponent(s), Util.NIL_UUID);
      });
    }
  }
}