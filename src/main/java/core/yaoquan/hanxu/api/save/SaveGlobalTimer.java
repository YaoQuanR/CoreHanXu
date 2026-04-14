package core.yaoquan.hanxu.api.save;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SaveGlobalTimer extends SavedData {
    private final Map<String, TimerSaveData> savedData = new ConcurrentHashMap<>();

    public record TimerSaveData(int remainingTicks, int initialTicks, String endBehavior, String behaviorContent, String masterGroup) {}

    public void put(String timerId, TimerSaveData timerSaveData) {
        savedData.put(timerId, timerSaveData);
        setDirty();
    }

    public void remove(String timerId) {
        savedData.remove(timerId);
        setDirty();
    }

    public TimerSaveData get(String timerId) {
        return savedData.get(timerId);
    }

    public Map<String, TimerSaveData> getSavedData() {
        return savedData;
    }

    /* Use global nbt (SavedData) for storage (Player save methods are similar).
    *   "core.yaoquan.hanxu.global_instance_timers: {
    *       [single_timer]: {
    *           remaining_ticks: [ticks],
    *           initial_ticks: [ticks],
    *           ...
    *       }
    *   }
    */
    public CompoundTag save(CompoundTag finalTag) {
        CompoundTag allTimersTag = new CompoundTag();

        for (var entry : savedData.entrySet()) {
            CompoundTag timerTag = new CompoundTag();

            timerTag.putInt("remaining_ticks", entry.getValue().remainingTicks);
            timerTag.putInt("initial_ticks", entry.getValue().initialTicks);
            timerTag.putString("end_behavior", entry.getValue().endBehavior);
            if (entry.getValue().behaviorContent != null) {
                timerTag.putString("behavior_content", entry.getValue().behaviorContent);
            }
            timerTag.putString("master_group", entry.getValue().masterGroup);

            allTimersTag.put(entry.getKey(), timerTag);
        }

        finalTag.put("core.yaoquan.hanxu.global_instance_timers", allTimersTag);

        return finalTag;
    }

    public static SaveGlobalTimer load(CompoundTag finalTag) {
        SaveGlobalTimer saveGlobalTimer = new SaveGlobalTimer();
        CompoundTag allTimersTag = finalTag.getCompound("core.yaoquan.hanxu.global_instance_timers").orElse(new CompoundTag());

        for (String timerId : allTimersTag.keySet()) {
            CompoundTag timerTag = allTimersTag.getCompound(timerId).orElse(new CompoundTag());

            TimerSaveData timerSaveData = new TimerSaveData(
                    timerTag.getIntOr("remaining_ticks", 0),
                    timerTag.getIntOr("initial_ticks", 0),
                    timerTag.getStringOr("end_behavior", "null"),
                    timerTag.getStringOr("behavior_content", ""),
                    timerTag.getStringOr("master_group", "core_hanxu-command")
            );

            saveGlobalTimer.savedData.put(timerId, timerSaveData);
        }

        return saveGlobalTimer;
    }

    public static SaveGlobalTimer create() {
        return new SaveGlobalTimer();
    }
}
