package material.hunter.utils;

public class CheckForRoot {

  public static boolean isRoot() {
    ShellExecuter exe = new ShellExecuter();
    return exe.Executer("su -c id").isEmpty();
  }

  public static boolean isEnforce() {
    ShellExecuter exe = new ShellExecuter();
    if (exe.Executer("su -c getenforce").equals("Enforcing")) {
      return true;
    }
    return false;
  }
}