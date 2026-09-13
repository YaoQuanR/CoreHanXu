package core.yaoquan.hanxu.api;

import core.yaoquan.hanxu.CoreHanXu;
import core.yaoquan.hanxu.api.define.FilePath;
import core.yaoquan.hanxu.api.define.General;
import core.yaoquan.hanxu.api.define.SaveDat;
import core.yaoquan.hanxu.util.Exceptionable;
import core.yaoquan.hanxu.util.MethodResult;
import core.yaoquan.hanxu.util.NullableValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.CheckReturnValue;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p><h3>
 *     Variable System API
 * </b></h3>
 * <p>
 *     Variable system is a light weighted variable storage system for cross system work.
 *     It contains variable type (As similar to Java class) and variable value.
 *     You can compare, modify, and operates with scoreboard in Minecraft.
 * </p>
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
     * @param variableType          Valid type (Same as Java data class) for variable,
     *                              which contains <b>string, integer, boolean, float, double, long</b>.
     * @param variableValue         Defined value of this variable.
     * @param override              Rewrite value when set to true.
     * @return                      Success of failure when:
     *                              <li>- Using duplicated variable without force override -> "duplicated", variableName.</li>
     *                              <li>- Failed to casting the inputted string to a valid type -> "invalidCasting", variableValue.</li>
     *                              <li>- Casting to invalid type -> "invalidType", variableType.</li>
     */
    public static @NotNull MethodResult createVariable(String variableName, String variableType, String variableValue, boolean override) {
        if (registeredVariables.contains(variableName) && !override) {
            CoreHanXu.LOGGER.warn("[HX] Rejected override variable: {}", variableName);
            return MethodResult.failure("duplicated", variableName);
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
                    return MethodResult.failure("invalidCasting", variableValue);
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
                        return MethodResult.failure("invalidCasting", variableValue);
                    }

                    registeredVariables.add(variableName);
                    booleanVariables.put(variableName, value);
                }
                catch (NumberFormatException e) {
                    CoreHanXu.LOGGER.warn("[HX] Invalid boolean variable value: {}", variableValue);
                    return MethodResult.failure("invalidCasting", variableValue);
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
                    return MethodResult.failure("invalidCasting", variableValue);
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
                    return MethodResult.failure("invalidCasting", variableValue);
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
                    return MethodResult.failure("invalidCasting", variableValue);
                }
            }
            default -> {
                return MethodResult.failure("invalidType", variableType);
            }
        }

        return MethodResult.success();
    }

    /**
     * Delete variable from data.
     * @param variableName          Defined id of this variable.
     * @return                      Does the deletion success: boolean.
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

    /**
     * Receive a map that storage cast string of all variable.
     * @return                      Map of all variable.
     */
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

    /**
     * Receive a variable type string for operations.
     * @param variableName          Defined id of this variable.
     * @return                      A nullable value that return when:
     *                              <li>- Registered variable with valid type -> Contains a presented string.</li>
     *                              <li>- Variable not found in valid type -> None.</li>
     */
    public static @NotNull NullableValue<String> getType(String variableName) {
        if (stringVariables.containsKey(variableName)) {
            return NullableValue.ofNotNull("string");
        }
        if (integerVariables.containsKey(variableName)) {
            return NullableValue.ofNotNull("integer");
        }
        if (booleanVariables.containsKey(variableName)) {
            return NullableValue.ofNotNull("boolean");
        }
        if (floatVariables.containsKey(variableName)) {
            return NullableValue.ofNotNull("float");
        }
        if (doubleVariables.containsKey(variableName)) {
            return NullableValue.ofNotNull("double");
        }
        if (longVariables.containsKey(variableName)) {
            return NullableValue.ofNotNull("long");
        }
        return NullableValue.none();
    }

    /**
     * Receive a variable value as string.
     * @param variableName          Define id of this variable.
     * @return                      A nullable value that return when:
     *                              <li>- Registered variable with valid type -> Contains a presented string.</li>
     *                              <li>- Variable not found in valid type -> None.</li>
     */
    public static @NotNull NullableValue<String> getStringFrom(String variableName) {
        if (stringVariables.containsKey(variableName)) {
            return NullableValue.ofNullable(stringVariables.get(variableName));
        }
        if (integerVariables.containsKey(variableName)) {
            return NullableValue.ofNullable(Integer.toString(integerVariables.get(variableName)));
        }
        if (booleanVariables.containsKey(variableName)) {
            return NullableValue.ofNullable(Boolean.toString(booleanVariables.get(variableName)));
        }
        if (floatVariables.containsKey(variableName)) {
            return NullableValue.ofNullable(Float.toString(floatVariables.get(variableName)));
        }
        if (doubleVariables.containsKey(variableName)) {
            return NullableValue.ofNullable(Double.toString(doubleVariables.get(variableName)));
        }
        if (longVariables.containsKey(variableName)) {
            return NullableValue.ofNullable(Long.toString(longVariables.get(variableName)));
        }
        return NullableValue.none();
    }

    /**
     * Receive a variable value by name and type.
     * @param variableName          Define id of this variable.
     * @param variableType          Valid type (Same as Java data class) for variable,
     *                              which contains <b>string, integer, boolean, float, double, long</b>.
     * @return                      A nullable value that return when:
     *                              <li>- Registered variable with valid type -> Contains a presented object.</li>
     *                              <li>- Variable not found in valid type -> None.</li>
     */
    public static @NotNull NullableValue<Object> getObjectFrom(String variableName, String variableType) {
        switch (variableType) {
            case "string", "str", "String" -> {
                return NullableValue.ofNullable(stringVariables.get(variableName));
            }
            case "integer", "int", "Integer" -> {
                return NullableValue.ofNullable(integerVariables.get(variableName));
            }
            case "boolean", "bool", "Boolean" -> {
                return NullableValue.ofNullable(booleanVariables.get(variableName));
            }
            case "float", "Float" -> {
                return NullableValue.ofNullable(floatVariables.get(variableName));
            }
            case "double", "Double" -> {
                return NullableValue.ofNullable(doubleVariables.get(variableName));
            }
            case "long", "Long" -> {
                return NullableValue.ofNullable(longVariables.get(variableName));
            }
            case null, default -> {
                return NullableValue.none();
            }
        }
    }

    /**
     * Receive a variable value by name and type.
     * @param variableName          Define id of this variable.
     * @return                      A nullable value that return when:
     *                              <li>- Registered variable with valid type -> Contains a presented object.</li>
     *                              <li>- Variable not found in valid type -> None.</li>
     */
    public static @NotNull NullableValue<Object> getObjectFrom(String variableName) {
        if (!registeredVariables.contains(variableName)) {
            return NullableValue.none();
        }
        if (stringVariables.containsKey(variableName)) {
            return getObjectFrom(variableName, "string");
        }
        if (integerVariables.containsKey(variableName)) {
            return getObjectFrom(variableName, "integer");
        }
        if (booleanVariables.containsKey(variableName)) {
            return getObjectFrom(variableName, "boolean");
        }
        if (floatVariables.containsKey(variableName)) {
            return getObjectFrom(variableName, "float");
        }
        if (doubleVariables.containsKey(variableName)) {
            return getObjectFrom(variableName, "double");
        }
        if (longVariables.containsKey(variableName)) {
            return getObjectFrom(variableName, "long");
        }
        return NullableValue.none();
    }

    /**
     * Determine if the variable is registered.
     * @param variableName          Define id of this variable.
     * @return                      Does it exist: boolean.
     */
    public static boolean doesExists(String variableName) {
        return registeredVariables.contains(variableName);
    }

    /**
     * Compare the variable if an instance of the specific type.
     * @param variableName          Define id of this variable.
     * @param compareType           Valid type (Same as Java data class) for variable,
     *                              which contains <b>string, integer, boolean, float, double, long</b>.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesInstanceof(String variableName, String compareType) {
        if (!registeredVariables.contains(variableName)) {
            return Exceptionable.exception("notExist", variableName);
        }

        return switch (compareType) {
            case "string", "str", "String" ->
                    Exceptionable.usual(stringVariables.containsKey(variableName));
            case "integer", "int", "Integer" ->
                    Exceptionable.usual(integerVariables.containsKey(variableName));
            case "boolean", "bool", "Boolean" ->
                    Exceptionable.usual(booleanVariables.containsKey(variableName));
            case "float", "Float" ->
                    Exceptionable.usual(floatVariables.containsKey(variableName));
            case "double", "Double" ->
                    Exceptionable.usual(doubleVariables.containsKey(variableName));
            case "long", "Long" ->
                    Exceptionable.usual(longVariables.containsKey(variableName));
            default ->
                    Exceptionable.exception("invalidType", compareType);
        };
    }

    /**
     * Compare the variable if containing a specific string content.
     * @param variableName          Define id of this variable.
     * @param compareValue          The content of the reference for the string part matching.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesContains(String variableName, String compareValue) {
        if (!registeredVariables.contains(variableName)) {
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }

        return getStringFrom(variableName).matching(
                value -> Exceptionable.usual(value.contains(compareValue)),
                () -> Exceptionable.exception("notExist", variableName)
        );
    }

    /**
     * Compare the variable if reaches the specific length.
     * @param variableName          Define id of this variable.
     * @param length                Integer value for length compilation.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesLengthEquals(String variableName, int length) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'length' for unexist variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        return switch (variableType) {
            case "string" ->
                    Exceptionable.usual(stringVariables.get(variableName).length() == length);
            case "integer" ->
                    Exceptionable.usual(Integer.toString(integerVariables.get(variableName)).length() == length);
            case "float" ->
                    Exceptionable.usual(Float.toString(floatVariables.get(variableName)).length() == length);
            case "double" ->
                    Exceptionable.usual(Double.toString(doubleVariables.get(variableName)).length() == length);
            case "long" ->
                    Exceptionable.usual(Long.toString(longVariables.get(variableName)).length() == length);
            default ->
                    Exceptionable.exception("invalidType", variableName);
        };
    }

    /**
     * Compare the variable if equals to referenced variable.
     * It will cast the compare value to exactly same type of variable for compilation.
     * @param variableName          Define id of this variable.
     * @param compareValue          The content of the reference to compare if completely equals.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- This compare value is invalid type format for compilation -> Exception: "invalidCasting", compareValue.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesEquals(String variableName, String compareValue) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'equals' unexist variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        try {
            return switch (variableType) {
                case "string" ->
                        Exceptionable.usual(stringVariables.get(variableName).equals(compareValue));
                case "integer" ->
                        Exceptionable.usual(integerVariables.get(variableName) == Integer.parseInt(compareValue));
                case "boolean" ->
                        Exceptionable.usual(booleanVariables.get(variableName) == Boolean.parseBoolean(compareValue));
                case "float" ->
                        Exceptionable.usual(floatVariables.get(variableName) == Float.parseFloat(compareValue));
                case "double" ->
                        Exceptionable.usual(doubleVariables.get(variableName) == Double.parseDouble(compareValue));
                case "long" ->
                        Exceptionable.usual(longVariables.get(variableName) == Long.parseLong(compareValue));
                default ->
                        Exceptionable.exception("invalidType", variableName);
            };
        }
        catch (NumberFormatException e) {
            return Exceptionable.exception("invalidCasting", compareValue);
        }
    }

    /**
     * Compare the float value if they are equals within the bias of floating value.
     * @param variableName          Define id of this variable.
     * @param compareValue          Float value to compare.
     * @param bias                  Bias that considers equals when within the bias different.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesApproximateEquals(String variableName, float compareValue, float bias) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'approximate equals' unexist float variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        if (variableType.equals("float")) {
            return Exceptionable.usual(Math.max(floatVariables.get(variableName), compareValue) - Math.min(floatVariables.get(variableName), compareValue) < bias);
        }
        else {
            return Exceptionable.exception("invalidType", variableName);
        }
    }

    /**
     * Compare the double value if they are equals within the bias of floating value.
     * @param variableName          Define id of this variable.
     * @param compareValue          Double value to compare.
     * @param bias                  Bias that considers equals when within the bias different.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesApproximateEquals(String variableName, double compareValue, double bias) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'approximate equals' unexist double variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        if (variableType.equals("double")) {
            return Exceptionable.usual(Math.max(doubleVariables.get(variableName), compareValue) - Math.min(doubleVariables.get(variableName), compareValue) < bias);
        }
        else {
            return Exceptionable.exception("invalidType", variableName);
        }
    }

    /**
     * Compare the variable if it is smaller than compare value.
     * @param variableName          Define id of this variable.
     * @param compareValue          Number to compare.
     * @param includedEqual         Determine if considers equal situation.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- The compare value is not a number for cast for compilation -> Exception: "invalidCasting", compareValue.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesVariableSmaller(String variableName, String compareValue, boolean includedEqual) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'greater than' unexist variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        try {
            switch (variableType) {
                case "integer" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Integer.parseInt(compareValue) >= integerVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Integer.parseInt(compareValue) > integerVariables.get(variableName));
                    }
                }
                case "float" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Float.parseFloat(compareValue) >= floatVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Float.parseFloat(compareValue) > floatVariables.get(variableName));
                    }
                }
                case "double" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Double.parseDouble(compareValue) >= doubleVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Double.parseDouble(compareValue) > doubleVariables.get(variableName));
                    }
                }
                case "long" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Long.parseLong(compareValue) >= longVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Long.parseLong(compareValue) > longVariables.get(variableName));
                    }
                }
                default -> {
                    return Exceptionable.exception("invalidType", variableName);
                }
            }
        }
        catch (NumberFormatException e) {
            return Exceptionable.exception("invalidCasting", compareValue);
        }
    }

    /**
     * Compare the variable if it is greater than compare value.
     * @param variableName          Define id of this variable.
     * @param compareValue          Number to compare.
     * @param includedEqual         Determine if considers equal situation.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- The compare value is not a number for cast for compilation -> Exception: "invalidCasting", compareValue.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesVariableGreater(String variableName, String compareValue, boolean includedEqual) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'smaller than' unexist variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        try {
            switch (variableType) {
                case "integer" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Integer.parseInt(compareValue) <= integerVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Integer.parseInt(compareValue) < integerVariables.get(variableName));
                    }
                }
                case "float" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Float.parseFloat(compareValue) <= floatVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Float.parseFloat(compareValue) < floatVariables.get(variableName));
                    }
                }
                case "double" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Double.parseDouble(compareValue) <= doubleVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Double.parseDouble(compareValue) < doubleVariables.get(variableName));
                    }
                }
                case "long" -> {
                    if (includedEqual) {
                        return Exceptionable.usual(Long.parseLong(compareValue) <= longVariables.get(variableName));
                    } else {
                        return Exceptionable.usual(Long.parseLong(compareValue) < longVariables.get(variableName));
                    }
                }
                default -> {
                    return Exceptionable.exception("invalidType", variableName);
                }
            }
        }
        catch (NumberFormatException e) {
            return Exceptionable.exception("invalidCasting", compareValue);
        }
    }

    /**
     * Compare the variable if it is smaller than compare value.
     * Which is considers the equal situation.
     * @param variableName          Define id of this variable.
     * @param compareValue          Number to compare.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- The compare value is not a number for cast for compilation -> Exception: "invalidCasting", compareValue.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesVariableAtMost(String variableName, String compareValue) {
        return doesVariableSmaller(variableName, compareValue, true);
    }

    /**
     * Compare the variable if it is greater than compare value.
     * Which is considers the equal situation.
     * @param variableName          Define id of this variable.
     * @param compareValue          Number to compare.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- The compare value is not a number for cast for compilation -> Exception: "invalidCasting", compareValue.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesVariableAtLeast(String variableName, String compareValue) {
        return doesVariableGreater(variableName, compareValue, true);
    }

    /**
     * Compare the variable in reverse result of {@link #doesEquals(String, String)}.
     * It will cast the compare value to exactly same type of variable for compilation.
     * @param variableName          Define id of this variable.
     * @param compareValue          The content of the reference to compare if completely equals.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- This compare value is invalid type format for compilation -> Exception: "invalidCasting", compareValue.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesDifference(String variableName, String compareValue) {
        return doesEquals(variableName, compareValue).matching(
                result -> Exceptionable.usual(!result),
                Exceptionable::exception
        );
    }

    /**
     * Compare the variable if its remainder from divide is equals to the compare value.
     * @param variableName          Define id of this variable.
     * @param marginValue           Number that for divide the variable value.
     * @param compareValue          Compare value that compare by the remainder.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- This margin/compare value is invalid type format for compilation -> Exception: "invalidCasting", "{@code marginValue} or {@code compareValue}".</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesMarginEquals(String variableName, String marginValue, String compareValue) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'margin equals' unexist variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        try {
            switch (variableType) {
                case "integer" -> {
                    return Exceptionable.usual(integerVariables.get(variableName) % Integer.parseInt(marginValue) == Integer.parseInt(compareValue));
                }
                case "float" -> {
                    float biasMargin = floatVariables.get(variableName) % Float.parseFloat(marginValue);
                    return Exceptionable.usual(Math.max(biasMargin, Float.parseFloat(compareValue)) - Math.min(biasMargin, Float.parseFloat(compareValue)) < 0.0001f);
                }
                case "double" -> {
                    double biasMargin = doubleVariables.get(variableName) % Double.parseDouble(marginValue);
                    return Exceptionable.usual(Math.max(biasMargin, Double.parseDouble(compareValue)) - Math.min(biasMargin, Double.parseDouble(compareValue)) < 0.0001d);
                }
                case "long" -> {
                    return Exceptionable.usual(longVariables.get(variableName) % Long.parseLong(marginValue) == Long.parseLong(compareValue));
                }
                default -> {
                    return Exceptionable.exception("invalidType", variableName);
                }
            }
        }
        catch (NumberFormatException e) {
            return Exceptionable.exception("invalidCasting", marginValue + " or " + compareValue);
        }
    }

    /**
     * Determine the variable if starts with the specific part.
     * @param variableName          Define id of this variable.
     * @param startsWithValue       Reference to determine if contains in starting.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesStartsWith(String variableName, String startsWithValue) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'starts with' unexist variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        return switch (variableType) {
            case "string" ->
                    Exceptionable.usual(stringVariables.get(variableName) != null && stringVariables.get(variableName).startsWith(startsWithValue));
            case "integer" ->
                    Exceptionable.usual(integerVariables.get(variableName) != null && String.valueOf(integerVariables.get(variableName)).startsWith(startsWithValue));
            case "float" ->
                    Exceptionable.usual(floatVariables.get(variableName) != null && String.valueOf(floatVariables.get(variableName)).startsWith(startsWithValue));
            case "double" ->
                    Exceptionable.usual(doubleVariables.get(variableName) != null && String.valueOf(doubleVariables.get(variableName)).startsWith(startsWithValue));
            case "long" ->
                    Exceptionable.usual(longVariables.get(variableName) != null && String.valueOf(longVariables.get(variableName)).startsWith(startsWithValue));
            default ->
                    Exceptionable.exception("invalidType", variableName);
        };
    }

    /**
     * Determine the variable if ends with the specific part.
     * @param variableName          Define id of this variable.
     * @param endsWithValue         Reference to determine if contains in ending.
     * @return                      A result that maybe failure:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> Exception: "invalidType", variableName.</li>
     *                              <li>- This compare type is not a valid type -> Exception: "invalidType", compareType.</li>
     *                              <li>- Compare for a result within safety -> Usual: boolean.</li>
     */
    @CheckReturnValue
    public static @NotNull Exceptionable<Boolean> doesEndsWith(String variableName, String endsWithValue) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to compare 'ends with' unexist variable: {}", variableName);
            return Exceptionable.exception("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return Exceptionable.exception("invalidType", variableName);
        }
        String variableType = nullableType.get();

        return switch (variableType) {
            case "string" ->
                    Exceptionable.usual(stringVariables.get(variableName) != null && stringVariables.get(variableName).endsWith(endsWithValue));
            case "integer" ->
                    Exceptionable.usual(integerVariables.get(variableName) != null && String.valueOf(integerVariables.get(variableName)).endsWith(endsWithValue));
            case "float" ->
                    Exceptionable.usual(floatVariables.get(variableName) != null && String.valueOf(floatVariables.get(variableName)).endsWith(endsWithValue));
            case "double" ->
                    Exceptionable.usual(doubleVariables.get(variableName) != null && String.valueOf(doubleVariables.get(variableName)).endsWith(endsWithValue));
            case "long" ->
                    Exceptionable.usual(longVariables.get(variableName) != null && String.valueOf(longVariables.get(variableName)).endsWith(endsWithValue));
            default ->
                    Exceptionable.exception("invalidType", variableName);
        };
    }

    /**
     * Modify the variable value by exactly same type new value.
     * @param variableName          Define id of this variable.
     * @param newValue              The new value that must within same type to old value,
     *                              which contains <b>string, integer, boolean, float, double, long</b>.
     * @return                      Success of failure when:
     *                              <li>- This variable are not registered -> "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> "invalidType", variableName.</li>
     *                              <li>- Invalid type from new value -> "invalidType", variableType.</li>
     *                              <li>- Using invalid format type to modify -> "invalidCasting", newValue.</li>
     */
    public static @NotNull MethodResult modifyVariable(String variableName, String newValue) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'modify' unexist variable: {}", variableName);
            return MethodResult.failure("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return MethodResult.failure("invalidType", variableName);
        }
        String variableType = nullableType.get();

        try {
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
                        return MethodResult.failure("invalidCasting", newValue);
                    }

                    booleanVariables.put(variableName, value);
                }
                case "float" -> floatVariables.put(variableName, Float.parseFloat(newValue));
                case "double" -> doubleVariables.put(variableName, Double.parseDouble(newValue));
                case "long" -> longVariables.put(variableName, Long.parseLong(newValue));
                default -> {
                    return MethodResult.failure("invalidType", variableType);
                }
            }
        }
        catch (NumberFormatException e) {
            return MethodResult.failure("invalidCasting", newValue);
        }

        return MethodResult.success();
    }

    /**
     * Add a number to the old variable value.
     * @param variableName          Define id of this variable.
     * @param value                 The new value that must within same type to old value,
     *                              which contains <b>integer, float, double, long</b>.
     * @return                      Success of failure when:
     *                              <li>- This variable are not registered -> "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> "invalidType", variableName.</li>
     *                              <li>- Invalid type from new value -> "invalidType", variableType.</li>
     *                              <li>- Using invalid number format to modify -> "invalidCasting", newValue.</li>
     */
    public static @NotNull MethodResult addNumber(String variableName, String value) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'increase' unexist variable: {}", variableName);
            return MethodResult.failure("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return MethodResult.failure("invalidType", variableName);
        }
        String variableType = nullableType.get();
        if (variableType.equals("string") || variableType.equals("boolean")) {
            return MethodResult.failure("invalidCasting", variableName);
        }

        try {
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
                default -> {
                    return MethodResult.failure("invalidType", variableName);
                }
            }
        }
        catch (NumberFormatException e) {
            return MethodResult.failure("invalidCasting", value);
        }

        return MethodResult.success();
    }

    /**
     * Reduce a number to the old variable value.
     * @param variableName          Define id of this variable.
     * @param value                 The absolute (positive) new value for reduce old value by same type,
     *                              which contains <b>integer, float, double, long</b>.
     * @return                      Success of failure when:
     *                              <li>- This variable are not registered -> "notExist", variableName.</li>
     *                              <li>- Invalid type from variable -> "invalidType", variableName.</li>
     *                              <li>- Invalid type from new value -> "invalidType", variableType.</li>
     *                              <li>- Using invalid number format to modify -> "invalidCasting", newValue.</li>
     */
    public static @NotNull MethodResult reduceNumber(String variableName, String value) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'decrease' unexist variable: {}", variableName);
            return MethodResult.failure("notExist", variableName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return MethodResult.failure("invalidType", variableName);
        }
        String variableType = nullableType.get();
        if (variableType.equals("string") || variableType.equals("boolean")) {
            return MethodResult.failure("invalidCasting", value);
        }

        try {
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
                default -> {
                    return MethodResult.failure("invalidType", variableName);
                }
            }
        }
        catch (NumberFormatException e) {
            return MethodResult.failure("invalidCasting", value);
        }

        return MethodResult.success();
    }

    /**
     * Copy the variable to the {@link Scoreboard} system.
     * @param variableName          Define id of this variable.
     * @param playerName            Player name for redirecting to the player by {@link ScoreHolder}.
     * @param scoreName             Score name from {@link Scoreboard}.
     * @return                      Success of failure when:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Server offline -> "serverOffline", variableName.</li>
     *                              <li>- Received null score objective from score name -> "unknownScoreObjective", scoreName.</li>
     *                              <li>- Invalid type from variable -> "invalidType", variableName.</li>
     *                              <li>- Unable to cast score value to boolean -> "invalidScoreCasting", variableName.</li>
     */
    public static @NotNull MethodResult copyVariableFromScore(String variableName, String playerName, String scoreName) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'copy to' unexist variable: {}", variableName);
            return MethodResult.failure("notExist", variableName);
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return MethodResult.failure("serverOffline", variableName);
        }

        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(scoreName);
        if (objective == null) {
            return MethodResult.failure("unknownScoreObjective", scoreName);
        }

        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(playerName);

        int scoreValue = scoreboard.getOrCreatePlayerScore(scoreHolder, objective).get();

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return MethodResult.failure("invalidType", variableName);
        }
        String variableType = nullableType.get();

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
                    return MethodResult.failure("invalidScoreCasting", variableName);
                }
            }
            default -> {
                return MethodResult.failure("invalidType", variableName);
            }
        }

        return MethodResult.success();
    }

    /**
     * Copy the score in {@link Scoreboard} from variable system.
     * @param variableName          Define id of this variable.
     * @param playerName            Player name for redirecting to the player by {@link ScoreHolder}.
     * @param scoreName             Score name from {@link Scoreboard}.
     * @return                      Success of failure when:
     *                              <li>- This variable are not registered -> Exception: "notExist", variableName.</li>
     *                              <li>- Server offline -> "serverOffline", variableName.</li>
     *                              <li>- Received null score objective from score name -> "unknownScoreObjective", scoreName.</li>
     *                              <li>- Invalid type from variable -> "invalidType", variableName.</li>
     */
    public static @NotNull MethodResult copyScoreFromVariable(String variableName, String playerName, String scoreName) {
        if (!registeredVariables.contains(variableName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'copy from' unexist variable: {}", variableName);
            return MethodResult.failure("notExist", variableName);
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return MethodResult.failure("serverOffline", variableName);
        }

        ServerScoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(scoreName);
        if (objective == null) {
            return MethodResult.failure("unknownScoreObjective", scoreName);
        }

        NullableValue<String> nullableType = getType(variableName);
        if (nullableType.isNull()) {
            return MethodResult.failure("invalidType", variableName);
        }
        String variableType = nullableType.get();

        int scoreValue;
        switch (variableType) {
            case "integer" -> scoreValue = integerVariables.get(variableName);
            case "float" -> scoreValue = Math.round(floatVariables.get(variableName));
            case "double" -> scoreValue = (int) Math.round(doubleVariables.get(variableName));
            case "long" -> scoreValue = (int) (long) longVariables.get(variableName);
            case "boolean" -> scoreValue = booleanVariables.get(variableName)? 1 : 0;
            default -> {
                return MethodResult.failure("invalidType", variableName);
            }
        }

        ScoreHolder scoreHolder = ScoreHolder.forNameOnly(playerName);
        scoreboard.getOrCreatePlayerScore(scoreHolder, objective).set(scoreValue);

        return MethodResult.success();
    }

    /**
     * Mask an exactly same value from reference to this variable.
     * @param variableName          Define id of this variable.
     * @param referenceName         Define id of the reference variable.
     * @return                      Success of failure when:
     *                              <li>- This variable are not registered -> "notExist", variableName/referenceName.</li>
     *                              <li>- Variable that target or reference is invalid type -> "invalidType", "{@code variableName} or {@code referenceName}".</li>
     *                              <li>- Different type of variable -> "mismatchType", "{@code variableType} <- {@code referenceType}".</li>
     *                              <li>- Invalid variable type -> "invalidType", variableType.</li>
     *                              <li>- Unexcepted casting error -> "invalidCasting", referenceValue.</li>
     */
    public static @NotNull MethodResult maskVariableValue(String variableName, String referenceName) {
        if (!registeredVariables.contains(variableName) || !registeredVariables.contains(referenceName)) {
            CoreHanXu.LOGGER.warn("[HX] Trying to 'same' unexist variable: {}", variableName);
            return MethodResult.failure("notExist", variableName);
        }

        NullableValue<String> nullableVariableType = getType(variableName);
        NullableValue<String> nullableReferenceType = getType(referenceName);
        if (nullableVariableType.isNull() || nullableReferenceType.isNull()) {
            return MethodResult.failure("invalidType", variableName + " or " + referenceName);
        }
        String variableType = nullableVariableType.get();
        String referenceType = nullableReferenceType.get();

        if (!variableType.equals(referenceType)) {
            return MethodResult.failure("mismatchType", variableType + " <- " + referenceType);
        }

        NullableValue<String> nullableReferenceValue = getStringFrom(referenceName);
        if (nullableReferenceValue.isNull()) {
            return MethodResult.failure("notExist", referenceName);
        }
        String referenceValue = nullableReferenceValue.get();

        try {
            switch (variableType) {
                case "string" -> stringVariables.put(variableName, referenceValue);
                case "integer" -> integerVariables.put(variableName, Integer.parseInt(referenceValue));
                case "boolean" -> booleanVariables.put(variableName, Boolean.parseBoolean(referenceValue));
                case "float" -> floatVariables.put(variableName, Float.parseFloat(referenceValue));
                case "double" -> doubleVariables.put(variableName, Double.parseDouble(referenceValue));
                case "long" -> longVariables.put(variableName, Long.parseLong(referenceValue));
                default -> {
                    return MethodResult.failure("invalidType", referenceType);
                }
            }
        }
        catch (NumberFormatException e) {
            return MethodResult.failure("invalidCasting", variableName);
        }

        return MethodResult.success();
    }

    /**
     * Changing a string to lower case.
     * @param variableName          Define id of this variable.
     * @return                      Success or failure when:
     *                              <li>- Not a string variable to operate -> "invalidType", variableName.</li>
     */
    public static @NotNull MethodResult stringToLowerCase(String variableName) {
        if (!stringVariables.containsKey(variableName)) {
            return MethodResult.failure("invalidType", variableName);
        }

        String newString = stringVariables.get(variableName).toLowerCase();

        stringVariables.put(variableName, newString);

        return MethodResult.success();
    }

    /**
     * Changing a string to upper case.
     * @param variableName          Define id of this variable.
     * @return                      Success or failure when:
     *                              <li>- Not a string variable to operate -> "invalidType", variableName.</li>
     */
    public static @NotNull MethodResult stringToUpperCase(String variableName) {
        if (!stringVariables.containsKey(variableName)) {
            return MethodResult.failure("invalidType", variableName);
        }

        String newString = stringVariables.get(variableName).toUpperCase();

        stringVariables.put(variableName, newString);

        return MethodResult.success();
    }

    public static @NotNull NullableValue<CompoundTag> packAllVariables() {
        String headKey = SaveDat.HeadKey.variables.get();
        CompoundTag root = new CompoundTag();
        CompoundTag variableTag = new CompoundTag();

        for (String variableName : registeredVariables) {
            CompoundTag variable = new CompoundTag();

            NullableValue<String> nullableType = getType(variableName);
            if (nullableType.isNull()) {
                continue;
            }
            String variableType = nullableType.get();

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

        return NullableValue.ofNotNull(root);
    }

    public static void loadAllVariables(ServerLevel level) {
        String headKey = SaveDat.HeadKey.variables.get();
        Path file = FilePath.getModDataPath(level);

        if (!file.toFile().exists()) {
            return;
        }

        CompoundTag root;
        try {
            NbtAccounter accounter = General.Standard.newNbtAccounter();
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
                case null -> CoreHanXu.LOGGER.warn("[HX] Missing variable type for loading variable: {}", variableName);
                default -> CoreHanXu.LOGGER.warn("[HX] Skipped unsupported type for loading variable: {}", variableName);
            }
        }
    }
}
