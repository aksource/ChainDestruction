package ak.ChainDestruction;

import ak.ChainDestruction.capability.*;
import ak.ChainDestruction.command.CommandCopyRtoLCDStatus;
import ak.ChainDestruction.command.CommandResetCDPlayerStatus;
import ak.ChainDestruction.command.CommandShowItemCDStatus;
import ak.ChainDestruction.command.CommandShowPlayerCDStatus;
import ak.ChainDestruction.network.PacketHandler;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.logging.Logger;
import java.util.function.Predicate;

@Mod(modid = ChainDestruction.MOD_ID,
        name = ChainDestruction.MOD_NAME,
        version = ChainDestruction.MOD_VERSION,
        dependencies = ChainDestruction.MOD_DEPENDENCIES,
        useMetadata = true,
        acceptedMinecraftVersions = ChainDestruction.MOD_MC_VERSION)
public class ChainDestruction {

    public static final String MOD_ID = "ChainDestruction";
    public static final String MOD_NAME = "ChainDestruction";
    public static final String MOD_VERSION = "@VERSION@";
    public static final String MOD_DEPENDENCIES = "required-after:Forge@[12.17.0,)";
    public static final String MOD_MC_VERSION = "[1.9,1.10.99]";
    private static final Map<Block, Block> ALTERNATE_BLOCK_MAP = new HashMap<>();
    private static final Joiner AT_JOINER = Joiner.on('@');
    @SidedProxy(clientSide = "ak.ChainDestruction.ClientProxy", serverSide = "ak.ChainDestruction.CommonProxy")
    public static CommonProxy proxy;
    @SuppressWarnings("unused")
    public static Logger logger = Logger.getLogger(MOD_ID);
    public static int maxYforTreeMode = 255;
    public static int digTaskMaxCounter = 5;
    @SuppressWarnings("unused")
    public static boolean dropOnPlayer = true;
    public static boolean destroyingSequentially = false;
    public static boolean notToDestroyItem = false;
    private static String[] excludeRegisterItem = new String[]{};
    public static Set<String> excludeRegisterItemSet = Sets.newHashSet();
    public static Predicate<ResourceLocation> excludeItemPredicate;
    public static InteractBlockHook interactblockhook;
    public static DigTaskEvent digTaskEvent = new DigTaskEvent();
    public static boolean loadMTH = false;
    private static Function functionBlockStateBase = ObfuscationReflectionHelper.getPrivateValue(BlockStateBase.class, null, 1);

    static {
        ALTERNATE_BLOCK_MAP.put(Blocks.LIT_REDSTONE_ORE, Blocks.REDSTONE_ORE);
        ALTERNATE_BLOCK_MAP.put(Blocks.LIT_FURNACE, Blocks.FURNACE);
    }

    private final String unused = "unused";
    public Configuration config;

    public static List<String> makeStringDataFromBlockState(IBlockState state) {
        Block block = state.getBlock();
        if (ALTERNATE_BLOCK_MAP.containsKey(block)) {
            block = ALTERNATE_BLOCK_MAP.get(block);
        }
        ItemStack itemStack = new ItemStack(block, 1, block.damageDropped(state));
        if (itemStack.getItem() == null) return Collections.singletonList(makeString(state));
        int[] oreIDs = OreDictionary.getOreIDs(itemStack);
        if (oreIDs.length > 0) {
            List<String> oreNames = new ArrayList<>(oreIDs.length);
            for (int id : oreIDs) {
                oreNames.add(OreDictionary.getOreName(id));
            }
            return oreNames;
        } else {
            String s = makeString(state);
            return Collections.singletonList(s);
        }

    }

    private static String makeString(IBlockState state) {
        StringBuilder stringbuilder = new StringBuilder();
        stringbuilder.append(state.getBlock().getRegistryName().toString());

        if (!state.getProperties().isEmpty()) {
            stringbuilder.append("[");
            AT_JOINER.appendTo(stringbuilder, Iterables.transform(state.getProperties().entrySet(), functionBlockStateBase));
            stringbuilder.append("]");
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings(unused)
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
            player.addChatMessage(new TextComponentString(s));
        }
    }
}