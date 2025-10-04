package dev.jsinco.gringotts.utility;

public final class ClassUtil {

    private ClassUtil() {}

    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
