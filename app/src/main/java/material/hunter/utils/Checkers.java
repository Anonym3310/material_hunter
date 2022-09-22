package material.hunter.utils;

public class Checkers {

    public static boolean isRoot() {
        ShellExecuter exe = new ShellExecuter();
        if (exe.RunAsRootOutput("su -c id").isEmpty()) {
            return false;
        }
        return true;
    }

    public static boolean isEnforcing() {
        ShellExecuter exe = new ShellExecuter();
        if (exe.RunAsRootOutput("su -c getenforce").equals("Enforcing")) {
            return true;
        }
        return false;
    }
}