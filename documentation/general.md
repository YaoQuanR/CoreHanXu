
General
=

### Introduction

This documentation set is for HanXu (Core) Powered Engine.
This documentation under the terms of CC BY-NC-ND (Follow license of resources).

This engine is made for easier customization on Minecraft save (map)/ mod/ modpack modding.
Also, it is primarily used for my projects.

### Supports

If you seek for help or raise issues, please contact me by email (Formal):
3501226176@qq.com (Primary) or 3648711935@qq.com.

### Development

It is an open-sourced mod, so that if you seek for other Minecraft version,
you are encouraged to migrate by yourself and under the terms from license.

It **may** be migrated to Minecraft Java Edition 1.21.1 in NeoForge (Not a promise).

### License

To avoiding dispute, I have announced the license of using this mod and affiliated resources
in document "License.txt" and "Assistant.txt".

If you are using command side system, 
you may require to check for the license state by using `/chx license state`.
After checking the general license by `/chx license origin`,
you may execute `/chx license agree` for activate command side permission.
But if you are using API side system, you can freely to use the API within the terms of license.

### Permission

Permission is a system to handle user's permission level.
If you are not admin and not agreed license, you will be considered as level 0.

#### Permission set
You can check the permission requirement by game file: ".minecraft/config/core_hanxu-permission.toml" for setup and lookup.
You are welcomed to discuss the range of permission set for better operations.

#### First grant
In general, player's first granted level is 2 out of 10 (Admin = 10).
You can check the grant level by `/chx permission player_first_grant`,
or modify this value by ".minecraft/config/core_hanxu-general.toml": auto_level = [integer].

#### Non-player source
If the executor is non-player source (Command block/ Console),
it depends on the game rule by `/gamerule nonPlayerSourcePermissionLevel [integer]` (Default: 2).
You can check the server level by `/chx permission server` for current level value.

#### Grant new player permission level
If you wish to modify the player permission level,
you should ensure that the general rule (.toml) "player_permission_editable" is set to true.
Then, execute command `/chx permission player [string: player id] set [integer: level]` for modification.

#### API support
If you wish to check the permission level of a source, use API:
```
// Get result.
boolean pass = PermissionHolder.Verify.hasPermission(source, requiredLevel);
```


