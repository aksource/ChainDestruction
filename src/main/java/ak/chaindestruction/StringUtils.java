package ak.chaindestruction;

import com.google.common.base.Joiner;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by A.K. on 2018/10/14.
 */
public class StringUtils {
    static final Map<Block, Block> ALTERNATE_BLOCK_MAP = new HashMap<>();
    private static final Joiner AT_JOINER = Joiner.on('@');
    private static Function<Map.Entry<IProperty<?>, Comparable<?>>, String> functionBlockStateBase = ObfuscationReflectionHelper.getPrivateValue(BlockStateBase.class, null, 1);


    static {
        ALTERNATE_BLOCK_MAP.put(Blocks.LIT_REDSTONE_ORE, Blocks.REDSTONE_ORE);
        ALTERNATE_BLOCK_MAP.put(Blocks.LIT_FURNACE, Blocks.FURNACE);
    }

    /**
     * {@code ResourceLocation}から固有文字列を取得
     *
     * @param resourceLocation ResourceLocation
     * @return 固有文字列
     */
    @Nonnull
    public static String getUniqueString(@Nullable ResourceLocation resourceLocation) {
        return resourceLocation != null ? resourceLocation.toString() : "";
    }

    public static List<String> makeStringDataFromBlockState(IBlockState state) {
        Block block = state.getBlock();
        if (ALTERNATE_BLOCK_MAP.containsKey(block)) {
            block = ALTERNATE_BLOCK_MAP.get(block);
        }
        ItemStack itemStack = new ItemStack(block, 1, block.damageDropped(state));
        if (itemStack.getItem() == Items.AIR) return Collections.singletonList(makeString(state));
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
        StringBuilder stringBuilder = new StringBuilder();
        if (state.getBlock().getRegistryName() != null) {
            stringBuilder.append(state.getBlock().getRegistryName().toString());
        }

        if (!state.getProperties().isEmpty()) {
            stringBuilder.append("[");
            AT_JOINER.appendTo(stringBuilder, state.getProperties().entrySet().stream().map(functionBlockStateBase).collect(Collectors.toList()));
            stringBuilder.append("]");
        }

        return stringBuilder.toString();
    }

    /**
     * 破壊対象ブロック名集合内に固有文字列が含まれているかどうか
     *
     * @param set     破壊対象ブロック名集合
     * @param uid     ブロックの固有文字列
     * @param uidMeta メタ付きブロックの固有文字列　[固有文字列]:[meta]
     * @return 含まれていたらtrue
     */
    private static boolean matchBlockMetaNames(Set<String> set, String uid, String uidMeta) {
        return set.contains(uid) || set.contains(uidMeta);
    }

    /**
     * 鉱石辞書名リスト内の要素と破壊対象ブロック名集合の要素で一致するものがあるかどうか
     *
     * @param set      破壊対象ブロック名集合
     * @param oreNames 鉱石辞書名リスト
     * @return 一致する要素があるならtrue
     */
    static boolean matchOreNames(Set<String> set, List<String> oreNames) {
        for (String string : oreNames) {
            if (set.contains(string)) return true;
        }
        return false;
    }

    /**
     * 破壊対象ブロック名集合内に引数のIBlockStateが表すブロックが含まれるかどうか
     *
     * @param set   破壊対象ブロック名集合
     * @param state 破壊対象判定IBlockState
     * @return 含まれていたらtrue
     */
    static boolean match(Set<String> set, IBlockState state) {
        Block block = state.getBlock();
        String uidStr = getUniqueString(block.getRegistryName());
        String uidMetaStr = state.toString();
        List<String> oreNames = makeStringDataFromBlockState(state);
        return matchOreNames(set, oreNames) || matchBlockMetaNames(set, uidStr, uidMetaStr);
    }
}
