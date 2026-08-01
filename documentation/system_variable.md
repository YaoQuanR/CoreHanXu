
System: Variable
=

### Introduction

Variable system is a light weighted variable storage system for cross system work.
It contains variable type (As similar to Java class) and variable value.
You can compare, modify, and operates with scoreboard in Minecraft.

Variable will storage at world NBT ([save]/data/core_hanxu.dat: core.yaoquan.hanxu.variables) when server stop.

### Features

It can split into general, API side, and command side guides.

#### General guides

<1> Type

Variable system was inspired by Java class system, which storage by clearly type for operations.

It contains: String, integer, boolean, float, double, long.

If you wish to understand what the type represented, you may check the following list:
- String: A group of characters, it can be made by word/sentence such as "this is a string".
- Integer: Integer that not contains decimal point (Limited to: -2^(31) ~ 2^(31)-1).
- Boolean: A small storage that only storage "true" or "false".
- Float: A 32 bits storage that approximately storage number that with decimal point (IEEE 754).
- Double: A 64 bits storage that more precisely to storage an approximate number with decimal point (IEEE 754).
- Long: Integer that not contains decimal point (Limited to: -2^(63) ~ 2^(63)-1).

Minecraft scoreboard system only storage integer type value.

<2> Compare/Modify

Variable system allows to compare value and return boolean if compare condition satisfied.
Also, variable system also allows to modify value in different situation.

Here is the support list:
- Value compare (<, <=, >, >=, ==, !=):                 Integer, float, double, long.
- Approximate compare (~= (API only)):                  Float, double.
- String compare (contains, starts_with, ends_with):    String, integer, boolean, float, double, long (All cast to string).
- Margin compare (% ==):                                Integer, float, double, long.
- Type compare (instanceof):                            String, integer, boolean, float, double, long.
- Length compare (length):                              String, integer, float, double, long.
- Value modify (set):                                   String, integer, boolean, float, double, long.
- Number modify (add, reduce):                          Integer, float, double, long.

<3> Type alias

It is accepted when specifying type of variable:
- String: string, str, String.
- Integer: integer, int, Integer.
- Boolean: boolean, bool, Boolean.
- Float: float, Float.
- Double: double, Double.
- Long: long, Long.

Variable system only return full lower case name for type varification.

#### API side guides

<1> Create

Ensure you understand what type you should be used.

Then, use this method to create a new variable:
```
// Define the needed information.
String name = "my_var:variable1";
String type = "string";
string value = "a simple string";
boolean override = false;           // Determine should override existed value.

// Then create.
// If the type is wrong or existed (Without override), return false.
boolean success = VariableHolder.createVariable(
    name, type, value, override
);
```

<2> Delete

It will mask all contained name variable.
```
String name = "my_var:variable1";

// Return false if not existed.
boolean deleted = VariableHolder.deleteVariable(name);
```

If you want to delete all variable (High risk),
use this method:
```
VariableHolder.deleteAllVariables();
```

<3> Read value

Receive map:
```
// All variable combined.
Map<String, String> allVariables = VariableHolder.getAllVariableAsString();

// Other single type map.
Map<String, String> stringVariables = VariableHolder.getStringVariables();
Map<String, Integer> integerVariables = VariableHolder.getIntegerVariables();
// ...
```

Receive set:
```
// All variables' name.
Set<String> registeredVariablesName = VariableHolder.getAllRegisteredVariables();
```

Receive single value:
```
// Get variable by string.
String name = "var0";
String stringValue = VariableHolder.getStringFrom(name);

// Get variable by object (Cast by yourself).
// Type determined by your value.
String type = "string";
Object objectValue = VariableHolder.getObjectFrom(name, type);
// Example.
String finalValue = String.valueOf(objectValue);
```

<4> Compare

It primarily purpose is provided for command side, but API is also usable.

Exist: Determine if this value existed.
(String, integer, boolean, float, double, long)
```
// Also depends on your need.
String name = "value";

boolean existed = VariableHolder.doesExists(name);
```

Instanceof: Determine if this value is in the specific type.
(String, integer, boolean, float, double, long)
```
String name "sus";
String compareType = "string";

boolean isInstanceof;
try {
    inInstanceof = VariableHolder.doesInstanceof(name, compareType);
}
catch (NumberFormatException e) {
    // If you provided invalid compare type, it will throw error.
}
```

Contain: Compare that your provided value is match the part of the variable value.
(String, integer, boolean, float, double, long)
```
String name = "unknown_string";
String compareValue = "valid:";

boolean contained;
try {
    // Example: Variable value = "valid:yes" -> compare value = "valid:" -> contains "valid:"? yes -> true.
    contained = VariableHolder.doesContains(name, compareValue);
}
catch (NullPointerException e) {
    // Throw error if this value does not existed or empty value.
}
catch (NumberFormatException e) {
    // Throw error if value not have a type or invalid type.
}
```

Length equal: Compare that if this value's length equals to the compare length.
(String, integer, float, double, long)
```
String name = "long_string";
int length = 10;

boolean equal;
try {
    // Example: Variable value = "100496712" -> length = 9 != compare length = 10 -> false.
    equal = VariableHolder.doesLengthEquals(name, length);
}
// Same to others.
catch (NullPointerException ignored) {}
catch (NumberFormatException ignored) {}
```

Equal: Compare that if this value absolutely equals to variable value.
(String, integer, boolean, float, double, long)
```
String name = "value";
String compareValue = "VALUE";

boolean equal;
try {
    // Example: Variable value = "VALUE" -> exactly equal to "VALUE" -> true.
    equal = VariableHolder.doesEquals(name, compareValue);
}
// Same to others.
catch (NullPointerException ignored) {}
catch (NumberFormatException ignored) {}
```

Approximate equal: Base on the feature of float/double, if you compare them with exactly number,
                   it will be false (Example: 3.9999997 != 4). Therefore, you will need to compare
                   them with approximate bias to find if them equals.
(Float, double)
```
String name = "float_value";
float compareValue = 2.5f;      // If compare with double value, use double.
float bias = 0.001f;            // Determine what bias can be acceptable to consider as equal.

boolean equal;
try {
    equal = VariableHolder.doesFloatApproximateEquals(name, compareValue, bias);
    // Same to:
    // equal = VariableHolder.doesDoubleApproximateEqual("double_value", 2.5d, 0.001d);
}
// Same to others.
catch (NullPointerException ignored) {}
catch (NumberFormatException ignored) {}
```

Greater/Smaller: Determine if **compare value** is greater/smaller than **existing variable value**.
(Integer, float, double, long)
```
String name = "integer_value";
String compareValue = 10;
boolean includedEqual = false;  // Set true to include as <=/>=.

boolean pass;
try {
    // Example: Compare value = 10, Variable value = 2 -> Compare value > Variable value -> does greater than existing? yes -> true.
    pass = VariableHolder.doesGreaterThanExisting(name, compareValue, includedEqual);
    // Same to:
    // pass = VariableHolder.doesSmallerThanExisting(name, compareValue, includedEqual);
}
// Same to others.
catch (NullPointerException ignored) {}
catch (NumberFormatException ignored) {}
```

If you wish to know other compare method, please view `api.VariableHolder.java` for details.

<5> Modify

You can mask a new value by:
```
try {
    String name = "integer_value";
    // Cast your value to string first.
    String newValue = "10";
    
    VariableHolder.modifyVariable(name, newValue);
}
catch (NullPointerException ignored) {}
catch (NumberFormatException ignored) {}
```

Or you can add/reduce the number by:
```
try {
    String name = "float_value";
    String value = "15.5";
    
    VariableHolder.addNumber(name, value);
    // Same to:
    // VariableHolder.reduceNumber(name, value);
}
catch (NullPointerException ignored) {}
catch (NumberFormatException ignored) {}
```

<6> Scoreboard operations

You can put value from/to scoreboard to/from variable by the following methods.

There have two options:
```
String name = "value";
// The scoreboard owner.
String playerId = "player1";
// The scoreboard name.
String scoreName = "dummy_value";

try {
    VariableHolder.copyVariableFromScore(name, playerId, scoreName);
}
catch (NullPointerException e) {
    // Throw when value not found.
}
catch (NumberFormatException e) {
    // Throw when value not supported the variable type.
}
catch (IllegalStateException e) {
    // Throw when Minecraft server not start.
}
catch (IllegalArgumentException e) {
    // Throw when scoreboard objective not found.
}
```
```
String name = "record";
// The scoreboard owner.
String playerId = "player1";
// The scoreboard name.
String scoreName = "sanity";

try {
    VariableHolder.copyScoreFromVariable(name, playerId, scoreName);
}
catch (NullPointerException e) {
    // Throw when value not found.
}
catch (NumberFormatException e) {
    // Throw when value not supported the variable type.
}
catch (IllegalStateException e) {
    // Throw when Minecraft server not start.
}
catch (IllegalArgumentException e) {
    // Throw when scoreboard objective not found.
}
```

<7> Other operations

If you wish to know more method to operate, you may check `api.VariableHolder.java` for details.

#### Command side guides

Variable system supports to use command at players, command blocks, or console for operations,
excepts float/double value approximate equals.

<1> Create

Ensure you understand what type you should be used.

Execute as format: `
/chx variable create [string: variable type (string/integer/boolean/float/double/long)] 
[string: variable name] [string: variable value] {-override (Optional)}
`

Example: `
/chx variable create integer int_value 150
`

<2> Delete

You can only delete one variable at once.

Execute as format: `
/chx variable delete [string: variable name]
`

<3> Copy from/to scoreboard score

If you want to operate variable with Minecraft scoreboard, you can copy and paste by the following methods.

Format that copy variable from score: `
/chx variable copy [string: variable name] from [string: player id (scoreboard owner)] [string: score name]
`

Format that copy variable to score: `
/chx variable copy [string: variable name] to [string: player id (scoreboard owner)] [string: score name]
`

<4> Compare

Variable system supports "if then" operation by if command.

Compare in variable value: `
/chx variable if value [string: variable name] [string: compare sign (==/</<=/>/>=/!=/contains...)] [string: compare value] ...
`

Compare in scoreboard value: `
/chx variable if score [string: player id] [string: score name] [string: compare sign (==/</<=/>/>=/!=/contains...)] [string: compare value] ...
`

Compare in variable margin value: `
/chx variable if margin [string: variable name] % [string: compare value] =/== [string: compare value] ...
`

If determined this condition is true, then you can define what behavior to do.

Execute command if satisfied: `
/chx variable if ...(Follow the format upside) execute [greedy string: command]
`

Further action if satisfied: `
/chx variable if ...(Follow the format upside) then [string: target variable] 
{set/add/reduce/copy_from/copy_to/same(Copy variable to variable)} 
[string: target (target value/ target scoreboard score/ target variable)] 
[string: player id (Optional. Use when you definding target scoreboard owner)]
`

<5> Modify

If you want to modify a value immediately, you can use modify command.

Format: `
/chx variable modify [string: variable name] {set/add/reduce/same(Copy variable to variable)} [string: new value (new value/ target variable)]
`

<6> Other operations

If you seek for more commands, you can view `/chx variable help` for help.