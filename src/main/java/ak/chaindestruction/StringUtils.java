package ak.chaindestruction;

import com.google.common.base.Joiner;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateHolder;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by A.K. on 2018/10/14.
 */
public class StringUtils {

  private static final Joiner AT_JOINER = Joiner.on('@');
  private static Function<Entry<IProperty<?>, Comparable<?>>, String> functionBlockStateBase = ObfuscationReflectionHelper
      .getPrivateValue(
          StateHolder.class, null, 0);

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

  public static List<String> makeStringDataFromBlockState(BlockState state) {
    Block block = state.getBlock();
    ItemStack itemStack = new ItemStack(block, 1);
    if (itemStack.getItem() == Items.AIR) {
      return Collections.singletonList(makeString(state));
    }
    Collection<ResourceLocation> owningTags = BlockTags.getCollection().getOwningTags(block);
    if (!owningTags.isEmpty()) {
      return owningTags.stream().map(ResourceLocation::toString).collect(Collectors.toList());
    } else {
      String s = makeString(state);
      return Collections.singletonList(s);
    }
  }

  private static String makeString(BlockState state) {
    StringBuilder stringBuilder = new StringBuilder();
    if (state.getBlock().getRegistryName() != null) {
      stringBuilder.append(state.getBlock().getRegistryName().toString());
    }

    if (!state.getProperties().isEmpty()) {
      stringBuilder.append("[");
      AT_JOINER.appendTo(stringBuilder,
          state.getValues().entrySet().stream().map(functionBlockStateBase)
              .collect(Collectors.toList()));
      stringBuilder.append("]");
    }

    return stringBuilder.toString();
  }

  /**
   * 破壊対象ブロック名集合内に固有文字列が含まれているかどうか
   *
   * @param set 破壊対象ブロック名集合
   * @param uid ブロックの固有文字列
   * @param uidMeta メタ付きブロックの固有文字列　[固有文字列]:[meta]
   * @return 含まれていたらtrue
   */
  private static boolean matchBlockMetaNames(Set<String> set, String uid, String uidMeta) {
    return set.contains(uid) || set.contains(uidMeta);
  }

  /**
   * 鉱石辞書名リスト内の要素と破壊対象ブロック名集合の要素で一致するものがあるかどうか
   *
   * @param set 破壊対象ブロック名集合
   * @param oreNames 鉱石辞書名リスト
   * @return 一致する要素があるならtrue
   */
  static boolean matchOreNames(Set<String> set, List<String> oreNames) {
    return oreNames.stream().anyMatch(set::contains);
  }

  /**
   * 破壊対象ブロック名集合内に引数のIBlockStateが表すブロックが含まれるかどうか
   *
   * @param set 破壊対象ブロック名集合
   * @param state 破壊対象判定IBlockState
   * @return 含まれていたらtrue
   */
  static boolean match(Set<String> set, BlockState state) {
    Block block = state.getBlock();
    String uidStr = getUniqueString(block.getRegistryName());
    String uidMetaStr = state.toString();
    List<String> oreNames = makeStringDataFromBlockState(state);
    return matchOreNames(set, oreNames) || matchBlockMetaNames(set, uidStr, uidMetaStr);
  }
}
