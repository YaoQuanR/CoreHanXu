
System: Attribute
=

### Introduction

Attribute system is a heavy system that storage value, threshold behavior,
zero callback, and recovery system.
The primarily use of attribute system is provided for players and entities.

### Features

This system is freely to define attribute such as "Adrenaline", "Sanity" and much more.
For supporting those features, you can define thresholds which is a trigger value defined for threshold behavior (Java callback or execute commands).


Also, if you wish to make the value modification at time based,
you can define recovery callback for complex behavior.
But, recovery method is optional (You can use set add reduce to manually modify attribute value).

It will also split as API side and command side operations.

#### General terms

Directions

It is a field that tells what is this attribute pass through the threshold.
There is three possible situation and four types of trigger.

Situation:
- POINT: The value is exactly equals to the threshold.
  (threshold = x; current = x)
- UP: The value is from lower value recovered into upper value,
  and it was pass through the threshold.
  (threshold = x; old = x-1 -> new = x+1)
- DOWN: The value is from upper value dropped into lower value,
  and it was pass through the threshold.
  (threshold = x; old = x+1 -> new = x-1)

Trigger type:
There are same to those three situation, but with one different:
- POINT + UP + DOWN -> Trigger when the situation happened.
- FLEX: It triggered when it pass through by POINT, UP or DOWN.

#### API side guides

<1> Define general attribute

At the starts of designing callbacks, I decided to use api.custom.BehaviorRegistry.java for
general callback register.
It will NOT automatically register for game, unless you subscribe the server start event and execute register.

At first, we need to define a new attribute.

It requires name field: `[mod id/system name]:[exact attribute id]` for attribute,
and a callback name field: `attribute:[mod id/system name]:[exact attribute id]`.

```
public class YourClass {
    public static void registerMethod() {
        // Define the name fields.
        String attributeId = "example_mod-example:test";
        String threshold20 = "attribute:example_mod-example:20";
        String threshold40 = "attribute:example_mod-example:40";
        String zeroThreshold = "attribute:example_mod-example:0";
        String recoveryMethod = "attribute:example_mod-example:recovery";
        
        // Maximum value.
        int maximum = 60;
        // Initial value.
        int defaultValue = 0;
        
        // Register by a chain factory.
        // It depends on your need:
        // How much threshold is needed? When do I need?
        // Should I define a zero threshold? (It also optional)
        // Should I use recovery method? (Optional)
        AttributeHolder.CustomAttribute testAttribute = new AttributeHolder.CustomAttribute(
            attributeId, maximum, defaultValue)
            // Register every threshold and their attribute.
            .onThreshold(20, threshold20)
            .onThreshold(40, threshold40)
            .onZero(zeroThreshold)
            .setRecovery(recoveryMethod)
            // This defined how many ticks for interval to execute recovery method (Default: 1).
            .setRecoveryIntervalTicks(1);
            
        // Then register this attribute.
        AttributeHolder.register(testAttribute);
        
        // Register each callback.
        BehaviorRegistry.register(threshold20, (player, parameters) -> {
            // Execute your behavior here...
            // Example.
            if (player == null) {
                return;
            }
            
            player.sendSystemMessage(Component.literal("threshold reached to 20..."));
        });
        
        // And more.
        BehaviorRegistry.register(threshold40, (player, parameters) -> {
            // Execute your behavior here...
        });
        
        BehaviorRegistry.register(zeroThreshold, (player, parameters) -> {...});
        
        BehaviorRegistry.register(recoveryMethod, (player, parameters) -> {...});
        
        // If you wish to display to F4 page for operations, you can use this method:
        ServerPlayer serverPlayer = yourMethodToGetDisplayTarget();
        
        AttributeHolder.displayToInfoPage(
            serverPlayer,
            serverPlayer.getUUID(),
            attributeId,
            true,               // This attribute is register from API, so that it is true.
            true                // Set false to stop display.
        );
    }
}

```

Then, it will register to apiAttributes for use.
It requires user to run this Java method on every server start for register by:
```
@SubscribeEvent
public void onServerStarting(ServerStartingEvent event) {
    YourClass.registerMethod();
}
```

If you still confused, you may check for the examples at `registry.test.TestHolder.java`.

<2> Parameters

It is a context to help you for customization.
It contains:
- master_id: Who owns this attribute value.
- master_name: What is the name of this owner.
- attribute_id: The triggered attribute's id.
- threshold: What threshold has been triggered (Recovery method will receive null).
- current_value: Old value of this attribute (It comes to past value when triggered).
- new_value: Next value of this attribute (It comes to current value when triggered).
- direction: Trigger type.

It can be used at register.
```
BehaviorRegister("attribute:example:example100", (player, parameters) -> {
    // You may need to cast the value class from string.
    float current = Float.parseFloat(parameters.get("current_value"));
    String direction = parameters.get("direction");
    
    // Execute your behavior...
});
```

<3> Assign value

The register is an attribute definition. 
To assign for player or global, you can use two different ways:
1. Recovery Method
2. Set/Add/Reduce

For set/add/reduce method, it can be operated by:
```
// Target to assign.
// Get context from CommandSourceStack, or get UUID by other way.
// Such as:
// MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
// if (server == null) { return; }
// UUID masterId = server.getPlayerList().getPlayer("player_name");
UUID masterId = context.getSource().getPlayer().getUUID();
String attributeId = "attribute:example:example200";
float newValue = 0.0f;
ThresholdDirection direction = ThresholdDirection.POINT;

// Set value.
AttributeHolder.setValue(
    masterId,
    attributeId,
    newValue,
    true,                   // If this attribute was registered from API, set to true.
    direction
);

// Or simple method that ignore direction, it will set as POINT.
AttributeHolder.setValue(
    masterId,
    attributeId,
    newValue,
    true
);

// If the target is global attribute, use this method (It will use General.TargetUUID.GLOBAL_UUID).
AttributeHolder.setGlobalValue(
    attributeId,
    newValue,
    true,
    direction
);
```
```
// The value to add (Accept negative).
float addValue = -1.0f;

// Similar method.
AttributeHolder.addValue(
    masterId,
    attributeId,
    addValue,
    true,
    direction
);
```
```
// The value to reduce (Always considered as minus).
float reduceValue = 1.0f;

// Similar method.
AttributeHolder.reduceValue(
    masterId,
    attributeId,
    reduceValue,
    true,
    direction
);
```

<4> Other operations

If you wish to know more method to operate, you may check `api.AttributeHolder.java` for details.

Special operation:
If you need a clear response of attribute status, you can use display method to display information
to F4 (Using F4 key in default) for operations.
```
AttributeHolder.displayToInfoPage(
    serverPlayer,
    serverPlayer.getUUID(),
    attributeId,
    isApiSource,                // A boolean value that determine is this attribute defined from API.
    true                        // Set false to stop display.
);
```

#### Command side guides

It is an easier method to operates attributes.
You can use limited callback (General use) or full callback (Requires Java register) for threshold/recovery.

<1> Define simple attribute

If you define a command attribute, it is *deletable attribute (API attribute can not be deleted).
Also, command attribute will automatically register by YAML attribute files.
You can view the file from ".minecraft/config/core_hanxu/attribute/[name].yaml" (Global path)
or ".minecraft/saves/[save]/data/core_hanxu/attribute/[name].yaml" (World path).

To create a new attribute, execute as format: 
`/chx attribute create [string: attribute id] {global/world} [float: maximum value] [float: default value]`

<2> Define threshold

When you finish the creation, define a threshold by format: 
`/chx attribute define [string: attribute id] [float: threshold] {execute/remind} [greedy string: content (execute: Considered as command; remind: Considered as sentence)]`

If you wish to link up the API callback, you can execute as format:
`/chx attribute define [string: attribute id] [float: threshold] api [string: callback api]`
(!) Ensure you are admin (Permission level = 10).

*Define threshold to 0 for creating zero threshold.

<3> Define recovery

It is an optional method that to linearly increase/decrease value, or link up to API recovery.

To create simple recovery curve:
`/chx attribute recovery simple [string: attribute id] [float: interval] [string: interval unit (tick/second/minute/hour)] [string: direction (point/up/down/flex)]`

To link up API recovery:
`/chx attribute recovery api [string: callback api]`
(!) Ensure you are admin (Permission level = 10).

<4> Modify attribute

If you need a flexible modification to attribute, you can execute by format:
`/chx attribute modify {set/add/reduce} [float: new value] [string: direction (point/up/down/flex)]`

Set: Set the value to the new value.
Add: Add value to old value (Accepted negative).
Reduce: Reduce value to old value (Considered as minus).

<5> Other operations

If you seek for more commands, you can view `/chx attribute help` for help.

Special operation:
You can use display command for debugging. It will display attribute information into F4 (Default key) page.
(!) Make sure you have level 3 / admin permission of Core HanXu to use this function.

Format: `
/chx attribute display [string: attribute id] [string: master id] {true/false}
`
