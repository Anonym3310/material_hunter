package rajankur;

import java.lang.reflect.Method;

public class AndroidHardwareCodename {

    public String getCodename() {
        String value = "undefined codename";

        try {
            final Class<?> sp = Class.forName("android.os.SystemProperties");
            final Method get = sp.getMethod("get", String.class);
            value = (String) get.invoke(null, "ro.build.product");
        } catch (Exception ignored) {

        }
        return value;
    }
}