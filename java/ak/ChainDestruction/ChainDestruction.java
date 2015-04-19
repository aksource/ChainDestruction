package ak.ChainDestruction;

import ak.ChainDestruction.network.PacketHandler;
import ak.akapi.ConfigSavable;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;

@Mod(modid="ChainDestruction", name="ChainDestruction", version="@VERSION@", dependencies = "required-after:Forge@[10.12.1.1090,)", useMetadata = true)
public class ChainDestruction
{
	@Instance("ChainDestruction")
	public static ChainDestruction instance;
	@SidedProxy(clientSide = "ak.ChainDestruction.ClientProxy", serverSide = "ak.ChainDestruction.CommonProxy")
	public static CommonProxy proxy;
	public static HashSet<String> enableItems = new HashSet<>();
	public static HashSet<String> enableBlocks = new HashSet<>();
    public static HashSet<String> enableLogBlocks = new HashSet<>();
    public static Map<String, Set<String>> privateItemBlockMap = Maps.newHashMap();
//    public static HashSet<String> dropItemSet = new HashSet<>();

    public static String[] itemsConfig;
	public static String[] blocksConfig;
    public static String[] logBlocksConfig;
    public static String[] privateItemBlockConfig = new String[]{};
	public static boolean digUnder = true;
	public static String[] vanillaTools;
	public static String[] vanillaBlocks;
    public static String[] vanillaLogs;
	public static int maxDestroyedBlock = 5;
    public static int maxYforTreeMode = 255;
    public static int digTaskMaxCounter = 5;
	public static boolean dropOnPlayer = true;
    public static boolean treeMode = false;
    public static boolean privateRegisterMode = false;
    public static boolean destroyingSequentially = false;
    public static boolean notToDestroyItem = false;
	public ConfigSavable config;
	public static InteractBlockHook interactblockhook = new InteractBlockHook();
    public static DigTaskEvent digTaskEvent = new DigTaskEvent();
	public static boolean loadMTH = false;
    private static final Map<Block, Block> ALTERNATE_BLOCK_MAP = new HashMap<>();

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		config = new ConfigSavable(event.getSuggestedConfigurationFile());
		config.load();
		maxDestroyedBlock = config.get(Configuration.CATEGORY_GENERAL, "maxDestroyedBlock", maxDestroyedBlock, "Maximum Destroyed Block Counts. range is 2 * max + 1").getInt();
        maxYforTreeMode = config.get(Configuration.CATEGORY_GENERAL, "maxYforTreeMode", maxYforTreeMode, "Max Height of destroyed block for tree mode. Be careful to set over 200.").getInt();
		itemsConfig = config.get(Configuration.CATEGORY_GENERAL, "toolItemsId", vanillaTools, "Tool ids that enables chain destruction.").getStringList();
		blocksConfig = config.get(Configuration.CATEGORY_GENERAL, "chainDestroyedBlockIdConfig", vanillaBlocks, "Ids of block that to be chain-destructed.").getStringList();
        logBlocksConfig = config.get(Configuration.CATEGORY_GENERAL, "chainDestroyedLogBlockIdConfig", vanillaLogs, "Ids of block that to be chain-destructed in tree mode.").getStringList();
        privateItemBlockConfig = config.get(Configuration.CATEGORY_GENERAL, "privateItemBlockConfig", privateItemBlockConfig, "Item ID and Block IDs Group. Ex: ItemId@BlockID@BlockID...").getStringList();
        digUnder = config.get(Configuration.CATEGORY_GENERAL, "digUnder", digUnder, "dig blocks under your position.").getBoolean(digUnder);
        privateRegisterMode = config.get(Configuration.CATEGORY_GENERAL, "privateRegisterMode", privateRegisterMode, "register block each item.").getBoolean();
        destroyingSequentially = config.get(Configuration.CATEGORY_GENERAL, "destroyingSequentially Mode", destroyingSequentially, "destroy blocks sequentially").getBoolean();
        digTaskMaxCounter = config.get(Configuration.CATEGORY_GENERAL, "digTaskMaxCounter", digTaskMaxCounter, "Tick Rate on destroying Sequentially Mode").getInt();
        digTaskMaxCounter = (digTaskMaxCounter <= 0)? 1 : digTaskMaxCounter;
        notToDestroyItem = config.get(Configuration.CATEGORY_GENERAL, "notToDestroyItem", notToDestroyItem, "Stop Destruciton not to destroy item").getBoolean();
		config.save();
        interactblockhook.setDigUnder(digUnder);
        interactblockhook.setTreeMode(treeMode);
        interactblockhook.setPrivateRegisterMode(privateRegisterMode);
        PacketHandler.init();
	}
	@Mod.EventHandler
	public void load(FMLInitializationEvent event)
	{
		proxy.registerClientInfo();
		MinecraftForge.EVENT_BUS.register(interactblockhook);
		MinecraftForge.EVENT_BUS.register(this);
	}
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		addItemsAndBlocks();
	    loadMTH = Loader.isModLoaded("MultiToolHolders");
	}

	private void addItemsAndBlocks() {
        enableItems.addAll(Arrays.asList(itemsConfig));
        enableBlocks.addAll(Arrays.asList(blocksConfig));
        changeStringsProperName(enableBlocks);
        enableLogBlocks.addAll(Arrays.asList(logBlocksConfig));
        changeStringsProperName(enableLogBlocks);
        makePrivateRegisterMap(privateItemBlockConfig);
	}

    private void makePrivateRegisterMap(String[] strArray) {
        List<String> splits;
        Set<String> blockSet;
        for (String string : strArray) {
            splits = Arrays.asList(string.split("@"));
            if (splits.size() > 1) {
                blockSet =  Sets.newHashSet();
                blockSet.addAll(splits.subList(1, splits.size()));
                privateItemBlockMap.put(splits.get(0), blockSet);
            }
        }
    }

    private HashSet<String> makePrivateRegisterConfig(Map<String, Set<String>> map) {
        HashSet<String> set = Sets.newHashSet();
        String str;
        for (String key : map.keySet()) {
            str = key + "@" + Joiner.on('@').skipNulls().join(map.get(key));
            set.add(str);
        }
        return set;
    }

    private void changeStringsProperName(Set<String> set) {
        Set<String> subSet = new HashSet<>();
        subSet.addAll(set);
        List<String> oreList = Arrays.asList(OreDictionary.getOreNames());

        for (String str : set) {
            if (str.contains(":") || oreList.contains(str)) subSet.remove(str);
        }

        for (String str : subSet) {
            boolean removeString = false;
            for (String oreName : oreList) {
                if (oreName.contains(str)) {
                    removeString = true;
                    set.add(oreName);
                }
            }
            if (removeString) set.remove(str);
        }
    }

    @SubscribeEvent
    public void joinInWorld(EntityJoinWorldEvent event) {
        if (!event.world.isRemote && event.entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)event.entity;
            String mode = privateRegisterMode?"Private Register":"Normal";
            String s = String.format("ChainDestruction Info Mode:%s, TreeMode:%b, Range:%d", mode, treeMode, maxDestroyedBlock);
            player.addChatMessage(new ChatComponentText(s));
        }
    }

    @SubscribeEvent
    public void WorldSave(Save event)
    {
        config.set(Configuration.CATEGORY_GENERAL, "toolItemsId", enableItems);
        config.set(Configuration.CATEGORY_GENERAL, "chainDestroyedBlockIdConfig", enableBlocks);
        config.set(Configuration.CATEGORY_GENERAL, "chainDestroyedLogBlockIdConfig", enableLogBlocks);
        config.set(Configuration.CATEGORY_GENERAL, "digUnder", digUnder);
        config.set(Configuration.CATEGORY_GENERAL, "maxDestroyedBlock", maxDestroyedBlock);
        config.set(Configuration.CATEGORY_GENERAL, "privateItemBlockConfig", makePrivateRegisterConfig(privateItemBlockMap));
        config.save();
    }

	public static String getUniqueStrings(Object obj)
	{
		UniqueIdentifier uId = null;
		if (obj instanceof ItemStack) {
			obj = ((ItemStack)obj).getItem();
		}
		if (obj instanceof Block) {
			uId = GameRegistry.findUniqueIdentifierFor((Block) obj);
		}
        if (obj instanceof Item){
			uId = GameRegistry.findUniqueIdentifierFor((Item) obj);
		}
		return Optional.fromNullable(uId).or(new UniqueIdentifier("none:dummy")).toString();
	}

    public static List<String> makeStringDataFromBlockAndMeta(BlockMetaPair blockMetaPair) {
        Block block = blockMetaPair.getBlock();
        int meta = blockMetaPair.getMeta();
        if (ALTERNATE_BLOCK_MAP.containsKey(block)) {
            block = ALTERNATE_BLOCK_MAP.get(block);
        }
        String s = String.format("%s:%d", GameRegistry.findUniqueIdentifierFor(block).toString(), meta);
        ItemStack itemStack = new ItemStack(block, 1, meta);
        if (itemStack.getItem() == null) {
            return Arrays.asList(s);
        }
        int[] oreIDs = OreDictionary.getOreIDs(itemStack);
        if (oreIDs.length > 0) {
            List<String> oreNames = new ArrayList<>(oreIDs.length);
            for (int id : oreIDs) {
                oreNames.add(OreDictionary.getOreName(id));
            }
            return oreNames;
        } else {
            return Arrays.asList(s);
        }

    }

	static{
		vanillaTools = new String[]{
				"minecraft:diamond_axe","minecraft:golden_axe","minecraft:iron_axe","minecraft:stone_axe","minecraft:wooden_axe",
				"minecraft:diamond_shovel","minecraft:golden_shovel","minecraft:iron_shovel","minecraft:stone_shovel","minecraft:wooden_shovel",
				"minecraft:diamond_pickaxe","minecraft:golden_pickaxe","minecraft:iron_pickaxe","minecraft:stone_pickaxe","minecraft:wooden_pickaxe"};
		vanillaBlocks = new String[]{getUniqueStrings(Blocks.obsidian), "glowstone", "ore"};
        vanillaLogs = new String[] {"logWood","treeLeaves"};
        ALTERNATE_BLOCK_MAP.put(Blocks.lit_redstone_ore, Blocks.redstone_ore);
        ALTERNATE_BLOCK_MAP.put(Blocks.lit_furnace, Blocks.furnace);
	}
}