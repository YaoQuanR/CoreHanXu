
System: Timer
=

### Introduction

Timer system is a system that allows Java callback or custom behavior after timer time out.
This system provides API and command side support.

### General terms

#### Master/Master id

It is a term that reference to the owner of the timer.
It can be player, global, or temporary.

### API side guides

#### <1> Template timer

Template timer is a temporary timer to define timer id, duration time,
time unit, and behavior details for repeatedly apply instances.

API side:
To use API template timer, you can use `TimeHolder.createTemplateTimer`.

Simple use:
```
// As example:
String timerId = "example_unique_timer_name";
int durationTime = 20;
String timeUnit = "second";
Consumer<ServerPlayer> callback = player -> {
    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (server == null || player == null) {
        return;
    }
    
    // Execute your behavior.
    // Example:
    player.sendSystemMessage(Component.literal("message..."));
};
// Optional, unless you use Creator callback.
String titleParameter = null, contentParameter = null;
// Ensure this field is unique, normally use mod name.
String masterGroup = "example";

TimeHolder.createTemplateTimer(
    timerId,
    durationTime,
    timeUnit,
    callback,
    titleParameter,
    contentParameter,
    masterGroup
);

// Then apply to instance for use.

// Make sure the source is available.
// Example of getting player.
MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
if (server == null) {
    return;
}
UUID masterId = server.getPlayerList().getPlayer("player_name");

TimeHolder.createInstanceFromTemplate(
    masterId,
    timerId
);

// Apply more (Example used a global UUID for global timer).
TimeHolder.createInstanceFromTemplate(
    General.TargetUUID.GLOBAL_UUID,
    timerId
);
```
Which will create a timer that starts will 20 seconds remain.
When timer end, send player a message (As example).
*If global UUID is used, 'player' will be null.

Using simple method will lose callback when restart (Because it is not being registered).
To register a callback behavior, we can use recovery method: TimerCallback
(Example provided at `registry.test.TestCallback.java`).
```
// Write a new class.
public class ExampleCallback implements TimerCallback {
    // Master group, which can be same to your mod id.
    @Override
    public String getMasterGroupId() {
        return "example_unique_timer_name";
    }
    
    // Your main register.
    // Title parameter and content parameter is optional for you to operation (Primary purpose is used for command side).
    @Override
    public Consumer<ServerPlayer> createCustomCallback(String timerId, String titleParameter, String contentParameter) {
        return switch (timerId) {
            case "timer1" -> player -> {// Your code...}
            case "timer2" -> player -> {// Your code...}
            default -> player -> {// Normally be nothing, or you can set reminder.}
        };
    }
}
```
```
// Then use it.
// If parameter is not used, you can use this simple method:
TimeHolder.createTemplateTimer(
    timerId,
    durationTime,
    timeUnit,
    masterGroup
);

// Or simple method but:
TimeHolder.createTemplateTimer(
    timerId,
    durationTime,
    timeUnit,
    null,               // Send null for using API register.
    titleParameter,     // Optional, depends on your needs.
    contentParameter,   // Optional, depends on your needs.
    masterGroup
);
```

#### <2> Instance timer

Instance timer is a method to immediately create a usable timer.
It similar to template timer, but provide masterId (playerUUID or global/temp) at first step.
You can view `registry.test.TestHolder.java` for detailed examples.
```
// Another example:
TimeHolder.createInstanceTimer(
    player.getUUID(),           // Provide UUID at first step (Or use global UUID).
    timerId,
    durationTime,
    null,                       // Null for TimerCallback overrides.
    titleParameter,             // Optional.
    contentParameter,           // Optional.
    "example_unique_timer_name"
);

// Or same like template timer method: skip callback and parameters.
```

#### <3> Using timer

When finish creation, you can use those method to run and modify your timer:
```
// Start: Provide the master and timer id for apply start.
TimeHolder.startInstanceTimer(masterId, timerId);
```
```
// Stop: Also provide the master and timer id.
TimeHolder.stopInstanceTimer(masterId, timerId);
```

If you want to recover timer to initial state, using reset/restart functions.
```
// Reset: Recover to initial state only.
TimeHolder.resetInstanceTimer(masterId, timerId);

// Restart: Recover and also start.
TimeHolder.restartInstanceTimer(masterId, timerId);
```

If you want to delete timer, using delete function.
```
// Delete tempalte timer.
TimeHolder.deleteTemplateTimer(timerId);

// Delete instance timer (Make sure it will not glitch when working).
TimeHolder.deleteInstanceTimer(masterId, timerId);
```

#### <4> Modify timer

When using instance timer, you are advised to modify timer when no system using this timer now.
```
// Example: Modifing initial time.
ModifyCategory category = TimeHolder.ModifyCategory.INITIAL_TIME;
// Or TimeHolder.ModifyCategory.REMAINING_TIME for remaining time modification.
TimeHolder.modifyInstanceTimer(
    masterId,
    timerId,
    newTime,            // Provide a new time value.
    timeUnit,           // The unit of this new value.
    category            // Category that determined this modification is modifing initial time or remaining time.
);
```

#### <5> Other operations

Sometimes you may need to get the status of a timer, you may check `api.TimeHolder.java` for details.

Special operation:
If you need a clear response of timer status, you can use display method to display information
to F4 (Using F4 key in default) for operations.
```
// The display state will be clear when restart the game.
TimeHolder.displayToInfoPage(
    player,             // Who's F4 should display this timer information.
    masterId,           // The UUID of timer's master.
    timerId,            // The timer.
    true                // Set false to stop display.
);
```

### Command side guides

For easier to use timer system. Mod provides commands for operations.
You can use limited callback (General use) or full callback (Requires Java register) for timer behavior.

#### <1> Create timer

Template timer is an intermediate form to apply timer usage.
Instance timer is for immediately use.

Format of template timer: `
/chx timer template create [string: timer id] [integer: time amount] 
[string: time unit (tick/second/minute/hour)] {execute/remind} [greedy string: content (execute: Considered as command; remind: Considered as sentence)]
`

Example 1: `
/chx timer template timer1 20 second execute /give @a apple 2
`

Example 2: `
/chx timer template timer2 60 tick remind this is a reminder text...
`

Instance timer is similar to template timer, but provides master (Owner) id is needed.

Format of instance timer: `
/chx timer instance create [string: timer id] [string: master id] [integer: time amount] 
[string: time unit (tick/second/minute/hour)] {execute/remind} [greedy string: content (execute: Considered as command; remind: Considered as sentence)]
`

Example 1: `
/chx timer instance timer100 -me 15 second execute /say hi 
`

Example 2: `
/chx timer instance timer200 -global 15 second remind time is up!
`

#### <2> Apply timer

When created template timer, you must apply it to instance for use.

Format: `
/chx timer instance apply [string: timer id (Template timer's name)] [string: master id (Who take this timer)]
`

#### <3> Using timer

When finish creation, use those commands to run timer.

Start timer: `
/chx timer instance start [string: master id] [string: timer id]
`

Stop timer: `
/chx timer instance stop [string: master id] [string: timer id]
`

Reset timer (Recover to initial state): `
/chx timer instance reset [string: master id] [string: timer id]
`

Restart timer (Recover and start): `
/chx timer instance restart [string: master id] [string: timer id]
`

<4> Modify timer

You may modify initial/remaining time by following commands.

Modify timer: `
/chx timer instance modify [string: master id] [string: timer id] {remaining/initial} [integer: new value] [string: time unit (tick/second/minute/hour)]
`

#### <5> Other operations

If you seek for more commands, you can view `/chx timer help` for help.

Special operation:
You can use display command for debugging. It will display timer information into F4 (Default key) page.
(!) Make sure you have level 3 / admin permission of Core HanXu to use this function.

Format: `
/chx timer instance display [string: master id] [string: timer id] {true/false}
`