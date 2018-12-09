package ak.chaindestruction;

import ak.chaindestruction.capability.*;
import ak.chaindestruction.command.CommandCopyRtoLCDStatus;
import ak.chaindestruction.command.CommandResetCDPlayerStatus;
import ak.chaindestruction.command.CommandShowItemCDStatus;
import ak.chaindestruction.command.CommandShowPlayerCDStatus;
import ak.chaindestruction.network.PacketHandler;
import com.google.common.collect.Sets;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;

@Mod(modid = ChainDestruction.MOD_ID,
        name = ChainDestruction.MOD_NAME,
        version = ChainDestruction.MOD_VERSION,
        dependencies = ChainDestruction.MOD_DEPENDENCIES,
        useMetadata = true,
        acceptedMinecraftVersions = ChainDestruction.MOD_MC_VERSION)
public class ChainDestruction {

    public static final String MOD_ID = "chaindestruction";
    public static final String MOD_NAME = "ChainDestruction";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String MOD_DEPENDENCIES = "required-after:forge@[13.19.1,)";
    public static final String MOD_MC_VERSION = "[1.11,1.19.99]";
    @SidedProxy(clientSide = "ak.chaindestruction.ClientProxy", serverSide = "ak.chaindestruction.CommonProxy")
    public static CommonProxy proxy;
    @SuppressWarnings("unused")
    public static Logger logger = Logger.getLogger(MOD_ID);
    public static int maxYforTreeMode = 255;
    public static int digTaskMaxCounter = 5;
    @SuppressWarnings("unused")
    public static boolean dropOnPlayer = true;
    public static boolean destroyingSequentially = false;
    public static boolean notToDestroyItem = false;
    public static Set<String> excludeRegisterItemSet = Sets.newHashSet();
    public static Predicate<ResourceLocation> excludeItemPredicate;
    public static InteractBlockHook interactblockhook;
    public static DigTaskEvent digTaskEvent = new DigTaskEvent();
    public static boolean loadMTH = false;
    private static String[] excludeRegisterItem = new String[]{};

    public Configuration config;

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        maxYforTreeMode = config.get(Configuration.CATEGORY_GENERAL, "maxYforTreeMode", maxYforTreeMode, "Max Height of destroyed block for tree mode. Be careful to set over 200.", 0, 255).getInt();
        destroyingSequentially = config.get(Configuration.CATEGORY_GENERAL, "destroyingSequentially Mode", destroyingSequentially, "destroy blocks sequentially").getBoolean();
        digTaskMaxCounter = config.get(Configuration.CATEGORY_GENERAL, "digTaskMaxCounter", digTaskMaxCounter, "Tick Rate on destroying Sequentially Mode", 1, 100).getInt();
        notToDestroyItem = config.get(Configuration.CATEGORY_GENERAL, "notToDestroyItem", notToDestroyItem, "Stop Destruciton not to destroy item").getBoolean();
        excludeRegisterItem = config.get(Configuration.CATEGORY_GENERAL, "excludeRegisterItem", excludeRegisterItem, "Exclude Item to register chain destruction.").getStringList();
        excludeRegisterItemSet.addAll(Arrays.asList(excludeRegisterItem));
        excludeItemPredicate = (resourceLocation) -> {
            boolean ret = false;
            if (resourceLocation == null) {
                return true;
            }
            for (String string : excludeRegisterItemSet) {
                if (resourceLocation.toString().matches(string)) {
                    ret = true;
                    break;
                }
            }
            return ret;
        };
        config.save();
        interactblockhook = new InteractBlockHook();
        PacketHandler.init();
        CapabilityCDPlayerStatusHandler.register();
        CapabilityCDItemStackStatusHandler.register();
        MinecraftForge.EVENT_BUS.register(new CapabilityEventHook());
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        proxy.registerClientInfo();
        MinecraftForge.EVENT_BUS.register(interactblockhook);
        MinecraftForge.EVENT_BUS.register(this);
        if (destroyingSequentially) {
            MinecraftForge.EVENT_BUS.register(digTaskEvent);
        }
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        loadMTH = Loader.isModLoaded("MultiToolHolders");
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandResetCDPlayerStatus());
        event.registerServerCommand(new CommandCopyRtoLCDStatus());
        event.registerServerCommand(new CommandShowPlayerCDStatus());
        event.registerServerCommand(new CommandShowItemCDStatus());
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void joinInWorld(EntityJoinWorldEvent event) {
        if (!event.getWorld().isRemote && event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            ICDPlayerStatusHandler status = CDPlayerStatus.get(player);
            String mode = status.isPrivateRegisterMode() ? "Private Register" : "Normal";
            String s = String.format("ChainDestruction Info Mode:%s, TreeMode:%b, Range:%d", mode, status.isTreeMode(), status.getMaxDestroyedBlock());
            player.sendMessage(new TextComponentString(s));
        }
    }
}