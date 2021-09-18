package ak.mcmod.chaindestruction.util;

import ak.mcmod.chaindestruction.ChainDestruction;
import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig.Loading;
import org.apache.logging.log4j.LogManager;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Predicate;

import static ak.mcmod.chaindestruction.ChainDestruction.digTaskEvent;

/**
 * Created by A.K. on 2019/03/25.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ConfigUtils {

  public static final Common COMMON;

  public static final ForgeConfigSpec CONFIG_SPEC;

  static {
    Builder builder = new ForgeConfigSpec.Builder();
    COMMON = new Common(builder);
    CONFIG_SPEC = builder.build();
  }

  @SubscribeEvent
  public static void configLoading(final Loading event) {
    LogManager.getLogger().debug("Loaded ChainDestruction config file {}",
            event.getConfig().getFileName());
    COMMON.maxYforTreeMode = COMMON.maxYforTreeModeConfigValue.get();
    COMMON.destroyingSequentially = COMMON.destroyingSequentiallyConfigValue.get();
    COMMON.digTaskMaxCounter = COMMON.digTaskMaxCounterConfigValue.get();
    COMMON.notToDestroyItem = COMMON.notToDestroyItemConfigValue.get();
    COMMON.excludeRegisterItemList = Lists.newArrayList(COMMON.excludeRegisterItemsConfigValue.get().split(","));
    if (COMMON.destroyingSequentially) {
      MinecraftForge.EVENT_BUS.register(digTaskEvent);
    }
    COMMON.excludeItemPredicate = (resourceLocation) -> {
      if (resourceLocation == null) {
        return true;
      }
      return COMMON.excludeRegisterItemList.stream().anyMatch(s -> resourceLocation.toString().matches(s));
    };
  }

  public static class Common {

    public int maxYforTreeMode;
    public boolean destroyingSequentially;
    public int digTaskMaxCounter;
    public boolean notToDestroyItem;
    @SuppressWarnings("unused")
    public boolean dropOnPlayer = true;
    public List<String> excludeRegisterItemList;
    public Predicate<ResourceLocation> excludeItemPredicate;
    private final IntValue maxYforTreeModeConfigValue;
    private final BooleanValue destroyingSequentiallyConfigValue;
    private final IntValue digTaskMaxCounterConfigValue;
    private final BooleanValue notToDestroyItemConfigValue;
    private final ConfigValue<String> excludeRegisterItemsConfigValue;

    Common(Builder builder) {
      builder.comment("Common settings")
              .push(ChainDestruction.MOD_ID);
      maxYforTreeModeConfigValue = builder
              .comment("Max Height of destroyed block for tree mode. Be careful to set over 200.")
              .defineInRange("maxYforTreeMode", 255, 0, 255);
      destroyingSequentiallyConfigValue = builder.comment("Destroy blocks sequentially")
              .define("destroyingSequentiallyMode",
                      false);
      digTaskMaxCounterConfigValue = builder.comment("Tick Rate on destroying Sequentially Mode")
              .defineInRange("digTaskMaxCounter", 5,
                      1, 100);
      notToDestroyItemConfigValue = builder.comment("Stop Destruction not to destroy item")
              .define("notToDestroyItem", false);
      excludeRegisterItemsConfigValue = builder
              .comment("Exclude Item to register chain destruction.")
              .define("excludeRegisterItem", "");
      builder.pop();
    }

  }
}
