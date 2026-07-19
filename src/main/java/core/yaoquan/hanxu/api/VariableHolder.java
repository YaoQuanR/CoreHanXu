package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.define.FilePath;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Variable System API
 * @since 0.5.0 (Internal Development)
 */
public class VariableHolder {
    private static final Set<String> registeredVariables = new HashSet<>();
    private static final Map<String, String> stringVariables = new HashMap<>();
    private static final Map<String, Integer> integerVariables = new HashMap<>();
    private static final Map<String, Boolean> booleanVariables = new HashMap<>();
    private static final Map<String, Float> floatVariables = new HashMap<>();
    private static final Map<String, Double> doubleVariables = new HashMap<>();
    private static final Map<String, Long> longVariables = new HashMap<>();

    public static Set<String> getAllRegisteredVariables() {
        return registeredVariables;
    }

    public static Map<String, String> getStringVariables() {
        return stringVariables;
    }

    public static Map<String, Integer> getIntegerVariables() {
        return integerVariables;
    }

    public static Map<String, Boolean> getBooleanVariables() {
        return booleanVariables;
    }

    public static Map<String, Float> getFloatVariables() {
        return floatVariables;
    }

    public static Map<String, Double> getDoubleVariables() {
        return doubleVariables;
    }

    public static Map<String, Long> getLongVariables() {
        return longVariables;
    }

    /**
     * Create a new variable.
     * @param variableName          Defined id of this variable.
     * @param variableType          Valid type (Same as Java data class) for variable.
     * @param variableValue         Defined value of this variable.
     * @param override              Rewrite value when set to true.
     * @return                      Does the creation success: Boolean.
     */
    public static boolean createVariable(String variableName, String variableType, String variableValue, boolean override) {
        if (registeredVariables.contains(variableName) && !override) {
            CoreHanXu.LOGGER.warn("[HX] Rejected override variable: {}", variableName);
            return false;
        }

        CoreHanXu.LOGGER.info("[HX] Creating variable: {} in type: {}", variableName, variableType);

        switch (variableType) {
            case "string", "str", "String" -> {
                registeredVariables.add(variableName);
                stringVariables.put(variableName, variableValue);
            }
            case "integer", "int", "Integer" -> {
                try {
                    int value = Integer.parseInt(variableValue);
                    registeredVariables.add(variableName);
                    integerVariables.put(variableName, value);
                }
                catch (NumberFormatException e) {
                    CoreHanXu.LOGGER.warn("[HX] Invalid integer variable value: {}", variableValue);
                    return false;
                }
            }
            case "boolean", "bool", "Boolean" -> {
                try {
                    boolean value;
                    if (variableValue.equalsIgnoreCase("true") || variableValue.equalsIgnoreCase("false")) {
                        value = variableValue.equalsIgnoreCase("true");
                    }
                    else if (variableValue.equals("1") || variableValue.equals("0")) {
                        value = variableValue.equalsIgnoreCase("1");
                    }
                    else {
                        return false;
                    }

                    registeredVariables.add(variableName);
                    booleanVariables.put(variableName, value);
                }
                catch (NumberFormatException e) {
                    CoreHanXu.LOGGER.warn("[HX] Invalid boolean variable value: {}", variableValue);
                    return false;
                }
            }
            case "float", "Float" -> {
                try {
                    float value = Float.parseFloat(variableValue);
                    registeredVariables.add(variableName);
                    floatVariables.put(variableName, value);
                }
                catch (NumberFormatException e) {
                    CoreHanXu.LOGGER.warn("[HX] Invalid float variable value: {}", variableValue);
                    return false;
                }
            }
            case "double", "Double" -> {
                try {
                    double value = Double.parseDouble(variableValue);
                    registeredVariables.add(variableName);
                    doubleVariables.put(variableName, value);
                }
                catch (NumberFormatException e) {
                    CoreHanXu.LOGGER.warn("[HX] Invalid double variable value: {}", variableValue);
                    return false;
                }
            }
            case "long", "Long" -> {
                try {
                    long value = Long.parseLong(variableValue);
                    registeredVariables.add(variableName);
                    longVariables.put(variableName, value);
                }
                catch (NumberFormatException e) {
                    CoreHanXu.LOGGER.warn("[HX] Invalid long variable value: {}", variableValue);
                    return false;
                }
            }
            default -> {
                return false;
            }
        }

        return true;
    }

    /**
     * Delete variable from data.
     * @param variableName          Defined id of this variable.
     * @return                      Does the deletion success: Boolean.
     */
    public static boolean deleteVariable(String variableName) {
        if (!registeredVariables.contains(variableName)) {
            return false;
        }

        registeredVariables.remove(variableName);
        stringVariables.remove(variableName);
        integerVariables.remove(variableName);
        booleanVariables.remove(variableName);
        floatVariables.remove(variableName);
        doubleVariables.remove(variableName);
        longVariables.remove(variableName);
        return true;
    }

    /**
     * Clean out all variables.
     */
    public static void deleteAllVariables() {
        registeredVariables.clear();
        stringVariables.clear();
        integerVariables.clear();
        booleanVariables.clear();
        floatVariables.clear();
        doubleVariables.clear();
        longVariables.clear();
    }

    public static Map<String, String> getAllVariableAsString() {
        Map<String, String> allVariableAsString = new HashMap<>(stringVariables);

        for (Map.Entry<String, Integer> entry : integerVariables.entrySet()) {
            allVariableAsString.put(entry.getKey(), entry.getValue().toString());
        }

        for (Map.Entry<String, Boolean> entry : booleanVariables.entrySet()) {
            allVariableAsString.put(entry.getKey(), entry.getValue().toString());
        }

        for (Map.Entry<String, Float> entry : floatVariables.entrySet()) {
            allVariableAsString.put(entry.getKey(), entry.getValue().toString());
        }

        for (Map.Entry<String, Double> entry : doubleVariables.entrySet()) {
            allVariableAsString.put(entry.getKey(), entry.getValue().toString());
        }

        for (Map.Entry<String, Long> entry : longVariables.entrySet()) {
            allVariableAsString.put(entry.getKey(), entry.getValue().toString());
        }

        return allVariableAsString;
    }

    public static String getType(String variableName) {
        if (stringVariables.containsKey(variableName)) {
            return "string";
        }
        if (integerVariables.containsKey(variableName)) {
            return "integer";
        }
        if (booleanVariables.containsKey(variableName)) {
            return "boolean";
        }
        if (floatVariables.containsKey(variableName)) {
            return "float";
        }
        if (doubleVariables.containsKey(variableName)) {
            return "double";
        }
        if (longVariables.containsKey(variableName)) {
            return "long";
        }
        return null;
    }

    public static String getStringFrom(String variableName) {
        if (stringVariables.containsKey(variableName)) {
            return stringVariables.get(variableName);
        }
        if (integerVariables.containsKey(variableName)) {
            return Integer.toString(integerVariables.get(variableName));
        }
        if (booleanVariables.containsKey(variableName)) {
            return Boolean.toString(booleanVariables.get(variableName));
        }
        if (floatVariables.containsKey(variableName)) {
            return Float.toString(floatVariables.get(variableName));
        }
        if (doubleVariables.containsKey(variableName)) {
            return Double.toString(doubleVariables.get(variableName));
        }
        if (longVariables.containsKey(variableName)) {
            return Long.toString(longVariables.get(variableName));
        }
        return null;
    }

    public static Object getObjectFrom(String variableName, String variableType) {
        switch (variableType) {
            case "string", "str", "String" -> {
                return stringVariables.get(variableName);
            }
            case "integer", "int", "Integer" -> {
                return integerVariables.get(variableName);
            }
            case "boolean", "bool", "Boolean" -> {
                return booleanVariables.get(variableName);
            }
            case "float", "Float" -> {
                return floatVariables.get(variableName);
            }
            case "double", "Double" -> {
                return doubleVariables.get(variableName);
            }
            case "long", "Long" -> {
                return longVariables.get(variableName);
            }
            default -> {
                return null;
            }
        }
    }

    public static boolean doesExists(String variableName) {
        return registeredVariables.contains(variableName);
    }

    public static boolean doesInstanceof(String variableName, String compareType) throws NumberFormatException {
        switch (compareType) {
            case "string", "str", "String" -> {
                return stringVariables.containsKey(variableName);
            }
            case "integer", "int", "Integer" -> {
                return integerVariables.containsKey(variableName);
            }
            case "boolean", "bool", "Boolean" -> {
                return booleanVariables.containsKey(variableName);
            }
            case "float", "Float" -> {
                return floatVariables.containsKey(variableName);
            }
            case "double", "Double" -> {
                return doubleVariables.containsKey(variableName);
            }
            case "long", "Long" -> {
                return longVariables.containsKey(variableName);
            }
            default -> {
                throw new NumberFormatException();
            }
        }
    }

    public static boolean doesContains(String variableName, String compareValue) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        if (variableType == null) {
            throw new NumberFormatException();
        }

        String value = getStringFrom(variableName);
        if (value == null) {
            throw new NullPointerException();
        }

        return value.contains(compareValue);
    }

    public static boolean doesLengthEquals(String variableName, int length) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'length' for unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);

        switch (variableType) {
            case "string" -> {
                return stringVariables.get(variableName).length() == length;
            }
            case "integer" -> {
                return Integer.toString(integerVariables.get(variableName)).length() == length;
            }
            case "float" -> {
                return Float.toString(floatVariables.get(variableName)).length() == length;
            }
            case "double" -> {
                return Double.toString(doubleVariables.get(variableName)).length() == length;
            }
            case "long" -> {
                return Long.toString(longVariables.get(variableName)).length() == length;
            }
            case null, default -> {
                throw new NumberFormatException();
            }
        }
    }

    public static boolean doesEquals(String variableName, String compareValue) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'equals' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);

        switch (variableType) {
            case "string" -> {
                return stringVariables.get(variableName).equals(compareValue);
            }
            case "integer" -> {
                return integerVariables.get(variableName) == Integer.parseInt(compareValue);
            }
            case "boolean" -> {
                return booleanVariables.get(variableName) == Boolean.parseBoolean(compareValue);
            }
            case "float" -> {
                return floatVariables.get(variableName) == Float.parseFloat(compareValue);
            }
            case "double" -> {
                return doubleVariables.get(variableName) == Double.parseDouble(compareValue);
            }
            case "long" -> {
                return longVariables.get(variableName) == Long.parseLong(compareValue);
            }
            case null, default -> throw new NumberFormatException();
        }
    }

    public static boolean doesFloatApproximateEquals(String variableName, float compareValue, float bias) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'approximate equals' unexist float variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        if (variableType == null) {
            throw new NumberFormatException();
        }

        if (variableType.equals("float")) {
            return Math.max(floatVariables.get(variableName), compareValue) - Math.min(floatVariables.get(variableName), compareValue) < bias;
        }
        else {
            throw new NumberFormatException();
        }
    }

    public static boolean doesDoubleApproximateEquals(String variableName, double compareValue, double bias) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'approximate equals' unexist double variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        if (variableType == null) {
            throw new NumberFormatException();
        }

        if (variableType.equals("double")) {
            return Math.max(doubleVariables.get(variableName), compareValue) - Math.min(doubleVariables.get(variableName), compareValue) < bias;
        }
        else {
            throw new NumberFormatException();
        }
    }

    public static boolean doesGreaterThanExisting(String variableName, String compareValue, boolean includedEqual) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'greater than' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);

        switch (variableType) {
            case "integer" -> {
                if (includedEqual) {
                    return Integer.parseInt(compareValue) >= integerVariables.get(variableName);
                }
                else {
                    return Integer.parseInt(compareValue) > integerVariables.get(variableName);
                }
            }
            case "float" -> {
                if (includedEqual) {
                    return Float.parseFloat(compareValue) >= floatVariables.get(variableName);
                }
                else {
                    return Float.parseFloat(compareValue) > floatVariables.get(variableName);
                }
            }
            case "double" -> {
                if (includedEqual) {
                    return Double.parseDouble(compareValue) >= doubleVariables.get(variableName);
                }
                else {
                    return Double.parseDouble(compareValue) > doubleVariables.get(variableName);
                }
            }
            case "long" -> {
                if (includedEqual) {
                    return Long.parseLong(compareValue) >= longVariables.get(variableName);
                }
                else {
                    return Long.parseLong(compareValue) > longVariables.get(variableName);
                }
            }
            case null, default -> throw new NumberFormatException();
        }
    }

    public static boolean doesSmallerThanExisting(String variableName, String compareValue, boolean includedEqual) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'smaller than' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);

        switch (variableType) {
            case "integer" -> {
                if (includedEqual) {
                    return Integer.parseInt(compareValue) <= integerVariables.get(variableName);
                }
                else {
                    return Integer.parseInt(compareValue) < integerVariables.get(variableName);
                }
            }
            case "float" -> {
                if (includedEqual) {
                    return Float.parseFloat(compareValue) <= floatVariables.get(variableName);
                }
                else {
                    return Float.parseFloat(compareValue) < floatVariables.get(variableName);
                }
            }
            case "double" -> {
                if (includedEqual) {
                    return Double.parseDouble(compareValue) <= doubleVariables.get(variableName);
                }
                else {
                    return Double.parseDouble(compareValue) < doubleVariables.get(variableName);
                }
            }
            case "long" -> {
                if (includedEqual) {
                    return Long.parseLong(compareValue) <= longVariables.get(variableName);
                }
                else {
                    return Long.parseLong(compareValue) < longVariables.get(variableName);
                }
            }
            case null, default -> throw new NumberFormatException();
        }
    }

    public static boolean doesGreaterOrEqualThanExisting(String variableName, String compareValue) throws NullPointerException, NumberFormatException {
        return doesGreaterThanExisting(variableName, compareValue, true);
    }

    public static boolean doesSmallerOrEqualThanExisting(String variableName, String compareValue) throws NullPointerException, NumberFormatException {
        return doesSmallerThanExisting(variableName, compareValue, true);
    }

    public static boolean doesNotEquals(String variableName, String compareValue) throws NullPointerException, NumberFormatException {
        return !doesEquals(variableName, compareValue);
    }

    public static boolean doesMarginEquals(String variableName, String marginValue, String compareValue) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'margin equals' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);

        switch (variableType) {
            case "integer" -> {
                return integerVariables.get(variableName) % Integer.parseInt(marginValue) == Integer.parseInt(compareValue);
            }
            case "float" -> {
                float biasMargin = floatVariables.get(variableName) % Float.parseFloat(marginValue);
                return Math.max(biasMargin, Float.parseFloat(compareValue)) - Math.min(biasMargin, Float.parseFloat(compareValue)) < 0.0001f;
            }
            case "double" -> {
                double biasMargin = doubleVariables.get(variableName) % Double.parseDouble(marginValue);
                return Math.max(biasMargin, Double.parseDouble(compareValue)) - Math.min(biasMargin, Double.parseDouble(compareValue)) < 0.0001d;
            }
            case "long" -> {
                return longVariables.get(variableName) % Long.parseLong(marginValue) == Long.parseLong(compareValue);
            }
            case null, default -> throw new NumberFormatException();
        }
    }

    public static boolean doesStartsWith(String variableName, String startsWithValue) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'starts with' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);

        switch (variableType) {
            case "string" -> {
                return stringVariables.get(variableName) != null && stringVariables.get(variableName).startsWith(startsWithValue);
            }
            case "integer" -> {
                return integerVariables.get(variableName) != null && String.valueOf(integerVariables.get(variableName)).startsWith(startsWithValue);
            }
            case "float" -> {
                return floatVariables.get(variableName) != null && String.valueOf(floatVariables.get(variableName)).startsWith(startsWithValue);
            }
            case "double" -> {
                return doubleVariables.get(variableName) != null && String.valueOf(doubleVariables.get(variableName)).startsWith(startsWithValue);
            }
            case "long" -> {
                return longVariables.get(variableName) != null && String.valueOf(longVariables.get(variableName)).startsWith(startsWithValue);
            }
            case null, default -> throw new NumberFormatException();
        }
    }

    public static boolean doesEndsWith(String variableName, String endsWithValue) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'ends with' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        switch (variableType) {
            case "string" -> {
                return stringVariables.get(variableName) != null && stringVariables.get(variableName).endsWith(endsWithValue);
            }
            case "integer" -> {
                return integerVariables.get(variableName) != null && String.valueOf(integerVariables.get(variableName)).endsWith(endsWithValue);
            }
            case "float" -> {
                return floatVariables.get(variableName) != null && String.valueOf(floatVariables.get(variableName)).endsWith(endsWithValue);
            }
            case "double" -> {
                return doubleVariables.get(variableName) != null && String.valueOf(doubleVariables.get(variableName)).endsWith(endsWithValue);
            }
            case "long" -> {
                return longVariables.get(variableName) != null && String.valueOf(longVariables.get(variableName)).endsWith(endsWithValue);
            }
            case null, default -> throw new NumberFormatException();
        }
    }

    public static void modifyVariable(String variableName, String newValue) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'modify' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        if (variableType == null) {
            throw new NumberFormatException();
        }

        switch (variableType) {
            case "string" -> stringVariables.put(variableName, newValue);
            case "integer" -> integerVariables.put(variableName, Integer.parseInt(newValue));
            case "boolean" -> {
                boolean value;
                if (newValue.equals("true") || newValue.equals("false")) {
                    value = newValue.equals("true");
                }
                else if (newValue.equals("1") || newValue.equals("0")) {
                    value = newValue.equals("1");
                }
                else {
                    throw new NumberFormatException();
                }

                booleanVariables.put(variableName, value);
            }
            case "float" -> floatVariables.put(variableName, Float.parseFloat(newValue));
            case "double" -> doubleVariables.put(variableName, Double.parseDouble(newValue));
            case "long" -> longVariables.put(variableName, Long.parseLong(newValue));
        }
    }

    public static void addNumber(String variableName, String value) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'increase' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        if (variableType == null || variableType.equals("string") || variableType.equals("boolean")) {
            throw new NumberFormatException();
        }

        switch (variableType) {
            case "integer" -> {
                int newValue = integerVariables.get(variableName) + Integer.parseInt(value);
                integerVariables.put(variableName, newValue);
            }
            case "float" -> {
                float newValue = floatVariables.get(variableName) + Float.parseFloat(value);
                floatVariables.put(variableName, newValue);
            }
            case "double" -> {
                double newValue = doubleVariables.get(variableName) + Double.parseDouble(value);
                doubleVariables.put(variableName, newValue);
            }
            case "long" -> {
                long newValue = longVariables.get(variableName) + Long.parseLong(value);
                longVariables.put(variableName, newValue);
            }
        }
    }

    public static void reduceNumber(String variableName, String value) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'decrease' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        if (variableType == null || variableType.equals("string") || variableType.equals("boolean")) {
            throw new NumberFormatException();
        }

        switch (variableType) {
            case "integer" -> {
                int newValue = integerVariables.get(variableName) - Math.abs(Integer.parseInt(value));
                integerVariables.put(variableName, newValue);
            }
            case "float" -> {
                float newValue = floatVariables.get(variableName) - Math.abs(Float.parseFloat(value));
                floatVariables.put(variableName, newValue);
            }
            case "double" -> {
                double newValue = doubleVariables.get(variableName) - Math.abs(Double.parseDouble(value));
                doubleVariables.put(variableName, newValue);
            }
            case "long" -> {
                long newValue = longVariables.get(variableName) - Math.abs(Long.parseLong(value));
                longVariables.put(variableName, newValue);
            }
        }
    }

    public static void copyVariableFromScore(String variableName, String playerName, String scoreName) throws NullPointerException, NumberFormatException, IllegalStateException, IllegalArgumentException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'copy to' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException();
        }

        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(scoreName);
        if (objective == null) {
            throw new IllegalArgumentException();
        }

        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(playerName);

        int scoreValue = scoreboard.getOrCreatePlayerScore(scoreHolder, objective).get();

        String variableType = getType(variableName);
        if (variableType == null) {
            throw new NumberFormatException();
        }

        switch (variableType) {
            case "string" -> {
                registeredVariables.add(variableName);
                stringVariables.put(variableName, String.valueOf(scoreValue));
            }
            case "integer" -> {
                registeredVariables.add(variableName);
                integerVariables.put(variableName, scoreValue);
            }
            case "float" -> {
                registeredVariables.add(variableName);
                floatVariables.put(variableName, (float) scoreValue);
            }
            case "double" -> {
                registeredVariables.add(variableName);
                doubleVariables.put(variableName, (double) scoreValue);
            }
            case "long" -> {
                registeredVariables.add(variableName);
                longVariables.put(variableName, (long) scoreValue);
            }
            case "boolean" -> {
                if (scoreValue == 0 || scoreValue == 1) {
                    registeredVariables.add(variableName);
                    booleanVariables.put(variableName, scoreValue == 1);
                }
                else {
                    throw new NumberFormatException();
                }
            }
            default -> throw new NumberFormatException();
        }
    }

    public static void copyScoreFromVariable(String variableName, String playerName, String scoreName) throws NullPointerException, NumberFormatException, IllegalStateException, IllegalArgumentException {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'copy from' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            throw new IllegalStateException();
        }

        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(scoreName);
        if (objective == null) {
            throw new IllegalArgumentException();
        }

        String variableType = getType(variableName);
        if (variableType == null) {
            throw new NumberFormatException();
        }

        int scoreValue;
        switch (variableType) {
            case "integer" -> scoreValue = integerVariables.get(variableName);
            case "float" -> scoreValue = Math.round(floatVariables.get(variableName));
            case "double" -> scoreValue = (int) Math.round(doubleVariables.get(variableName));
            case "long" -> scoreValue = (int) (long) longVariables.get(variableName);
            case "boolean" -> scoreValue = booleanVariables.get(variableName)? 1 : 0;
            default -> throw new NumberFormatException();
        }

        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(playerName);
        scoreboard.getOrCreatePlayerScore(scoreHolder, objective).set(scoreValue);
    }

    public static void toSameValue(String variableName, String referenceName) throws NullPointerException, NumberFormatException {
        if (!registeredVariables.contains(variableName) || !registeredVariables.contains(referenceName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'same' unexist variable: {}", variableName);
            throw new NullPointerException();
        }

        String variableType = getType(variableName);
        String referenceType = getType(referenceName);

        if (variableType == null || referenceType == null) {
            throw new NumberFormatException();
        }
        else if (!variableType.equals(referenceType)) {
            throw new NumberFormatException();
        }

        String referenceValue = getStringFrom(referenceName);
        if (referenceValue == null) {
            throw new NumberFormatException();
        }

        switch (variableType) {
            case "string" -> stringVariables.put(variableName, referenceValue);
            case "integer" -> integerVariables.put(variableName, Integer.parseInt(referenceValue));
            case "boolean" -> booleanVariables.put(variableName, Boolean.parseBoolean(referenceValue));
            case "float" -> floatVariables.put(variableName, Float.parseFloat(referenceValue));
            case "double" -> doubleVariables.put(variableName, Double.parseDouble(referenceValue));
            case "long" -> longVariables.put(variableName, Long.parseLong(referenceValue));
            default -> throw new NumberFormatException();
        }
    }

    public static void stringToLowerCase(String variableName) throws NullPointerException {
        if (!stringVariables.containsKey(variableName)) {
            throw new NullPointerException();
        }

        String newString = stringVariables.get(variableName).toLowerCase();

        stringVariables.put(variableName, newString);
    }

    public static void stringToUpperCase(String variableName) throws NullPointerException {
        if (!stringVariables.containsKey(variableName)) {
            throw new NullPointerException();
        }

        String newString = stringVariables.get(variableName).toUpperCase();

        stringVariables.put(variableName, newString);
    }

    public static void saveAllVariables(ServerLevel level) {
        String headKey = "core.yaoquan.hanxu.variables";
        CompoundTag root = new CompoundTag();
        CompoundTag variableTag = new CompoundTag();

        for (String variableName : registeredVariables) {
            CompoundTag variable = new CompoundTag();
            String variableType = getType(variableName);
            if (variableType == null) {
                continue;
            }

            variable.putString("type", variableType);
            switch (variableType) {
                case "string" -> variable.putString("value", stringVariables.get(variableName));
                case "integer" -> variable.putInt("value", integerVariables.get(variableName));
                case "boolean" -> variable.putBoolean("value", booleanVariables.get(variableName));
                case "float" -> variable.putFloat("value", floatVariables.get(variableName));
                case "double" -> variable.putDouble("value", doubleVariables.get(variableName));
                case "long" -> variable.putLong("value", longVariables.get(variableName));
            }

            variableTag.put(variableName, variable);
        }

        root.put(headKey, variableTag);

        Path file = FilePath.getModDataPath(level);
        try {
            NbtIo.writeCompressed(root, file.toFile().toPath());
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to save variables", e);
        }
    }

    public static void loadAllVariables(ServerLevel level) {
        String headKey = "core.yaoquan.hanxu.variables";
        Path file = FilePath.getModDataPath(level);

        if (!file.toFile().exists()) {
            return;
        }

        CompoundTag root;
        try {
            NbtAccounter accounter = new NbtAccounter(32L * 1024 * 1024, 128);
            root = NbtIo.readCompressed(file, accounter);
        }
        catch (IOException e) {
            CoreHanXu.LOGGER.warn("[HX] Failed to load variables", e);
            return;
        }

        CompoundTag variableTag = root.getCompound(headKey).orElse(new CompoundTag());

        for (String variableName : variableTag.keySet()) {
            CompoundTag variable = variableTag.getCompound(variableName).orElse(new CompoundTag());
            String variableType = variable.getString("type").orElse(null);

            if (variableType == null) {
                CoreHanXu.LOGGER.warn("[HX] Missing variable type for variable: {}", variableName);
                continue;
            }

            switch (variableType) {
                case "string" -> {
                    String value = variable.getStringOr("value", "");
                    registeredVariables.add(variableName);
                    stringVariables.put(variableName, value);
                }
                case "integer" -> {
                    int value = variable.getIntOr("value", 0);
                    registeredVariables.add(variableName);
                    integerVariables.put(variableName, value);
                }
                case "boolean" -> {
                    boolean value = variable.getBooleanOr("value", false);
                    registeredVariables.add(variableName);
                    booleanVariables.put(variableName, value);
                }
                case "float" -> {
                    float value = variable.getFloatOr("value", 0f);
                    registeredVariables.add(variableName);
                    floatVariables.put(variableName, value);
                }
                case "double" -> {
                    double value = variable.getDoubleOr("value", 0d);
                    registeredVariables.add(variableName);
                    doubleVariables.put(variableName, value);
                }
                case "long" -> {
                    long value = variable.getLongOr("value", 0L);
                    registeredVariables.add(variableName);
                    longVariables.put(variableName, value);
                }
                case null, default -> throw new NumberFormatException();
            }
        }
    }
}
