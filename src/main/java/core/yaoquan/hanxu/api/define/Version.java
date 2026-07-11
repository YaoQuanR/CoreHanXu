package core.yaoquan.hanxu.api.define;

public class Version {
    private static final String CORE_VERSION = "0.5.id2";
    private static final String TIMER_SYSTEM_VERSION = "1";
    private static final String SCENE_SYSTEM_VERSION = "1";
    private static final String ATTRIBUTE_SYSTEM_VERSION = "1";
    private static final String VARIABLE_SYSTEM_VERSION = "1";

    public static String getCoreVersion() {
        return CORE_VERSION;
    }

    public static String getTimerSystemVersion() {
        return TIMER_SYSTEM_VERSION;
    }

    public static String getSceneSystemVersion() {
        return SCENE_SYSTEM_VERSION;
    }

    public static String getAttributeSystemVersion() {
        return ATTRIBUTE_SYSTEM_VERSION;
    }

    public static String getVariableSystemVersion() {
        return VARIABLE_SYSTEM_VERSION;
    }
}
