package core.yaoquan.hanxu.api.define;

public class Version {
    private static final String CORE_VERSION = "0.4.id4";
    private static final String TIMER_SYSTEM_VERSION = "1";
    private static final String SCENE_SYSTEM_VERSION = "1";

    public static String getCoreVersion() {
        return CORE_VERSION;
    }

    public static String getTimerSystemVersion() {
        return TIMER_SYSTEM_VERSION;
    }

    public static String getSceneSystemVersion() {
        return SCENE_SYSTEM_VERSION;
    }
}
