package core.yaoquan.hanxu.api.define;

import java.util.UUID;

public class General {
    public static class TargetUUID {
        // UUID constant.
        public static final UUID GLOBAL_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
        public static final UUID TEMPORARY_UUID = UUID.fromString("00000000-0000-0000-0000-00000000000f");
    }

    public static class Color {
        public static final int TITLE = 0xFFD700;
        public static final int CONTENT = 0xFFFACD;
        public static final int SUCCESS = 0x66FF66;
        public static final int WHITE = 0xFFFFFF;
        public static final int TEST = 0xF0FFFF;
        public static final int FAILURE = 0xFF5555;
    }

    public static class Version {
        private static final String CORE_VERSION = "0.6.id4";

        public static String getCoreVersion() {
            return CORE_VERSION;
        }
    }
}
