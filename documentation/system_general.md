
System: General
=

### License System

User (Player) are required to agreed license for using command system.
Check the state from "/chx license state"

You can get the license state from "PermissionHolder.java":
"getLicenseState" from API.

### Permission System

Permission system is designed to avoid unauthorized action on command system.

System provided 2 definitions which is "player" and "command block".
Here is the list of this feature:
- Maximum permission level: Normally fixed player to 2, command block to 0.
  - You can by using GameRule system to modify command block maximum level "/gamerule overrideCommandblockPermission",
    which is aims to avoiding command block cheat.
  - Player's maximum level is also allowed to set by API.

- Player current permission level: For API.
  - Player permission cannot be modified inside game (server), API of "setPlayerPermissionLevel"
    is used for editing player level, then storage into NBT folder.

- Auto authorize permission: Player will get NBT of permission level (normally used 1) when game (server) start.
  - You are allowed to stop authorize permission when server start by using API.

- Permission check: API defined method and allowed to plug to the command requires feature.