
System: Scene
=

### Introduction

Scene system is a system that provided convince way to execute dialogs and commands.
By using timer system, each dialog will contain deferred execution of every dialog node.

Define a scene by using YAML document at .minecraft\config\core_hanxu\scene\DOCS.yaml (global path) or
.minecraft\saves\WORLD-NAME\data\core_hanxu\scene\DOCS.yaml (world path).
Load world path will come first.

User (player) is allowed to use command "/chx scene" for managing scene documents.
Also, example will be given by executing "/chx scene template",
which is a book that allowed to modify and create.

API is provided at "SceneHolder.java".

### Features

- Create a New Scene
  - User can add YAML document at the given path.
    Execute "/chx scene play [scene_id]" for start scene.
  - In addition, HOLD writable book OR written book and execute
    "/chx scene create [to_path]" can generate a new YAML document into given path.

- Requirements of Writing a New Scene
  - YAML document should at least contain "id" + "type" + "dialogs" for execution.
  
  Format:
  ```
    id: "this string should same as file name (exclude .yaml)"
    type: "simple" <- Using "simple" mode. (other mode will be supported in future)
    
    default: <- Using this included field when the node not defined.
                Default value will be used when nothing.
      interval: [integer in ticks]
      color: [integer in hexadecimal]
      speaker: [boolean] <- Always ignore "speaker" field if set to *false*
      json: [boolean] <- Identify JSON text if set to *true*
      bold: [boolean]
      italic: [boolean]
      ...(other format)
  
    dialogs: <- Field that determine what is gonna to display.
      - speaker: "show this when speaker is true, or existed"
        text: "basic content"
        execute: "/say hi"
      - text: "ignore speaker when no speaker existed."
      - text: "override default value"
        interval: 20
        underline: true
        color: 0xFFDDFF
      - text: '{"text":"work when enabled json","click_event":{"action":"open_url","url":"https://www.minecraft.net"}}'
        execute: "/say if json is false, json element will become normal string."
      - speaker: "@skip"
        text: "you will not see this message, because it is skiped."
        execute: "/give @a stone 2"
      - speaker: "@r"
        text: "random player will be selected as the speaker."
      - speaker: "@a"
        text: "as same as @e, all player's name will be used at speaker's name"
  ```
    Example:
  ```
    id: "example_file"
    type: "simple"
  
    default:
      interval: 20
      json: true
  
    dialogs:
      - text: "Player 1 joined the game."
        interval: 60
        bold: true
        color: 0xFFFF00
      - speaker: "@skip"
        execute: "/weather rain"
      - text: '{"text":"raw text"}'
      - speaker: "@a"
        text: "123456789"
        obfuscated: true
  ```
