package android;

import java.lang.reflect.Method;

public class HardwareProps {

    public static String getProp(String prop) {
        String value = "";

        try {
            final Class<?> sp = Class.forName("android.os.SystemProperties");
            final Method get = sp.getMethod("get", String.class);
            value = (String) get.invoke(null, prop);
        } catch (Exception ignored) {

        }
        return value;
    }

    // setProp will be here!

    public static boolean deviceIsAB() {
        if (getProp("ro.virtual_ab.enabled") == "true"
                && getProp("ro.virtual_ab.retrofit") == "false") {
            return true;
        }

        /** Checks if the device supports the conventional A/B partition */
        if (!getProp("ro.boot.slot_suffix").isEmpty() || getProp("ro.build.ab_update") == "true") {
            return false;
        }

        return false;
    }
}
