package ak.mcmod.chaindestruction.util;

/**
 * Created by A.K. on 2021/09/18.
 */
public enum ModeType {
  NORMAL,
  TREE,
  BRANCH_MINING,
  WALL_MINING;

  public ModeType getNextModeType() {
    return values()[(ordinal() + 1) % values().length];
  }

  public ModeType getPreviousModeType() {
    return values()[(ordinal() - 1) % values().length];
  }
}
