
System: Scene
=

### Introduction

Scene system provides YAML format for user to easier execute chain commands and dialogs.
Currently, it is not finished yet, since customize particle system and camera track system is not defined now.

This system used YAML for defining scene node.
You can create and modify YAML scene at ".minecraft/config/core_hanxu/scene/[name].yaml" (Global path)
or ".minecraft/saves/[save]/data/core_hanxu/scene/[name].yaml" (World path).
Load world path will come first.

### Features

#### Basic format

Every file (Including every system's YAML/JSON file)
should include id field and ensure it same to file name.
Otherwise, file will not able to operate.

In general format, it included:
```
id: "[string: file name]"

# In current version, it only included simple type.
type: "simple"

# Define default behavior that execute every node.
default:
  # What interval in executing every node that expected.
  interval: [integer: (default) 1]
  # Default node text color in hexadecimal integer.
  color: [hexadecimal integer: (default) 0xFFFFFF]
  # Determine that should node accept json format text. Set false if this field not setup.
  json: true
  # Other general format modfication. It is optional field (Normally not setup at default).
  italic: false
  underlined: false
  bold: false
  strikethrough: false
  obfuscated: false

# Define each node by dialog.
dialogs:
  # A simple node
  - speaker: "[string: speaker name]"
    text: "[string: general text]"
    # Execute command if reached to this node.
    execute: "[string: command]"
  # If no speaker field, it display bare text.
  - text: "[string: bare text]"
  # If you wish to execute command only, use speaker "@skip".
  - speaker: "@skip"
    execute: "[string: command]"
  # If you want to modify interval of display next node, provide interval field.
  - speaker: "system"
    text: "next node will display later"
    interval: 120
  # Define any format you like to override default settings.
  - text: "modified text..."
    italic: true
    underlined: true
  # Using selector to choice speaker.
  - speaker: "@p"
    text: "this text was spoken by yourself..."
  - speaker: "@r"
    text: "random people say this text..."
  - speaker: "@a"
    text: "all player becomes speaker"
  # If you enabled json format, you can use text like this.
  # It is a click event. Don't forget to use different quotation marks.
  - text: '{"text":"click","click_event":{"action":"open_url","url":"https://www.minecraft.net"}}'
```

Example:
```
id: "example1"
type: "simple"

default:
  interval: 20
  color: 0xFFD700
  json: true
  
dialogs:
  - speaker: "@skip"
    execute: "give @a stone 5"
    interval: 120
  - speaker: "@skip"
    text: "you will not see this message"
    execute: "weather rain"
    interval: 60
  - text: '[{"text":"does this text red?","color":"red"}]'
  - text: "end."
    execute: "weather clear"
```

### Create

If you want to create a YAML scene within the game.
You can execute `/chx scene template` to receive a book for writing format.
Then, hold the book and execute `/chx scene create` to submit your scene.

### Play/Broadcast

When finished creation, you can play (Play to one player) or broadcast (Play to everyone).

#### API side start
Before start to play scene, you are advised to check the situation as follows:
```
// Check if the target is player.
// Get context from CommandSourceStack.
ServerPlayer player = context.getSource().getPlayer();
if (player == null) {
    return;
}

// Then check if scene existed.
String sceneName = "scene_name";

if (!SceneHolder.doesSceneExist(sceneName)) {
    return;
}

// Then display (If format break, it will failed to display).
try {
    SceneHolder.playScene(player, sceneName);
    // Same to broadcast:
    // SceneHolder.playSceneToEveryone(context.getSource().getServer(), sceneName);
}
catch (Exception e) {
    return;
}
```

#### Command side start
It will automatically check for the source and error handling.
Execute `/chx scene play [string: scene name]` for yourself or execute 
`/chx scene play [string: scene name] [string: player id]` for specific player (It supported at this documentation update).

Also, execute broadcast command by `/chx scene broadcast [string: scene name]` to play for everyone.

### Helps
Scene system is same to other system that provides help by executing `/chx scene help`.
