package ak.mcmod.chaindestruction;

import ak.mcmod.chaindestruction.capability.CapabilityCDItemStackStatusHandler;
import ak.mcmod.chaindestruction.capability.CapabilityCDPlayerStatusHandler;
import ak.mcmod.chaindestruction.command.CommandCopyRtoLCDStatus;
import ak.mcmod.chaindestruction.command.CommandResetCDPlayerStatus;
import ak.mcmod.chaindestruction.command.CommandShowItemCDStatus;
import ak.mcmod.chaindestruction.command.CommandShowPlayerCDStatus;
import ak.mcmod.chaindestruction.event.BlockEvents;
import ak.mcmod.chaindestruction.event.CapabilityEvents;
import ak.mcmod.chaindestruction.event.DigTaskEvents;
import ak.mcmod.chaindestruction.event.EntityEvents;
import ak.mcmod.chaindestruction.network.PacketHandler;
import com.google.common.collect.Sets;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

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
    public static final String MOD_DEPENDENCIES = "required-after:forge@[14.23,)";
    public static final String MOD_MC_VERSION = "[1.12,1.12.2]";
    @SidedProxy(clientSide = "ak.mcmod.chaindestruction.client.ClientProxy", serverSide = "ak.mcmod.chaindestruction.CommonProxy")
    public static CommonProxy proxy;
    @SuppressWarnings("unused")
    public static Logger logger = Logger.getLogger(MOD_ID);
    public static int maxYforTreeMode = 255;
    public static int digTaskMaxCounter = 5;
    @SuppressWarnings("unused")
    public static boolean dropOnPlayer = true;
    public static boolean destroyingSequentially = false;
    public static boolean notToDestroyItem = false;
    public static final Set<String> excludeRegisterItemSet = Sets.newHashSet();
    public static Predicate<ResourceLocation> excludeItemPredicate;
    public static final DigTaskEvents digTaskEvent = new DigTaskEvents();
    private static String[] excludeRegisterItem = new String[]{};

    public Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        maxYforTreeMode = config.get(Configuration.CATEGORY_GENERAL, "maxYforTreeMode", maxYforTreeMode, "Max Height of destroyed block for tree mode. Be careful to set over 200.", 0, 255).getInt();
        destroyingSequentially = config.get(Configuration.CATEGORY_GENERAL, "destroyingSequentially Mode", destroyingSequentially, "destroy blocks sequentially").getBoolean();
        digTaskMaxCounter = config.get(Configuration.CATEGORY_GENERAL, "digTaskMaxCounter", digTaskMaxCounter, "Tick Rate on destroying Sequentially Mode", 1, 100).getInt();
        notToDestroyItem = config.get(Configuration.CATEGORY_GENERAL, "notToDestroyItem", notToDestroyItem, "Stop Destruction not to destroy item").getBoolean();
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
        PacketHandler.init();
        CapabilityCDPlayerStatusHandler.register();
        CapabilityCDItemStackStatusHandler.register();
        MinecraftForge.EVENT_BUS.register(CapabilityEvents.class);
    }

    @SuppressWarnings("unused")
    @Mod.EventHandler
    public void load(FMLInitializationEvent event) {
        proxy.registerClientInfo();
        MinecraftForge.EVENT_BUS.register(BlockEvents.class);
        MinecraftForge.EVENT_BUS.register(EntityEvents.class);
        MinecraftForge.EVENT_BUS.register(this);
        if (destroyingSequentially) {
            MinecraftForge.EVENT_BUS.register(digTaskEvent);
        }
    }

    @Mod.EventHandler
    public void serverStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandResetCDPlayerStatus());
        event.registerServerCommand(new CommandCopyRtoLCDStatus());
        event.registerServerCommand(new CommandShowPlayerCDStatus());
        event.registerServerCommand(new CommandShowItemCDStatus());
    }
}