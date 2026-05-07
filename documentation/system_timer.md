
System: Timer
=

### Introduction

Timer system is a system to reduce time on timing design.
User (player) is allowed to use command "/chx-a timer" for managing timer system.
Also, API is provided for advanced modification (focus on callback features),
you can define an auto callback behavior by using API "TimerCallback" interface.

In default setting, timer system required users who are admin (permission level = 2),
and command block are also affected by maximum permission level (able to set by GameRule).
But, API will not block the modification on timer system, you are freely to depend on it.

### Features

Timer system provided two sets of timer, included command generated callback and custom callback.

You can create template timer for apply multiple instance timers by same, 
or using instance timer creation for quick use.

Defining master (timer owner) id is required for the timer feature,
timer will use player UUID or server identity for executing callback commands.

In detailed, API provided:
- Generals:
  - Instance Timer
    - Create
    - Create-Range (Randomly select a time depend on range)
    - Start
    - Stop
    - Reset (Reset remaining time to initial time)
    - Delete
    - List (List out all timers)
    - Read (Read timer remaining time/ initial time/ end behavior/ state)
    - Modify (Modify the time of timer)
    - Apply (Only usable when applying template timer to instance)
  - Template Timer
    - Create
    - Create-Range (Same)
    - List
    - Read

- Help:
  - You can execute "/chx-a timer help" for details.

- Template and Instance:
  - When create template timer, data will NOT save into NBTs.
    You are required to use apply (command are also provided) to instance for save and use.

- Examples:
  - If you do not understand how to modify timer by API,
    folder "test": "TestCallback.java" & "TestHolder.java" provided examples for you to learn.
    Remind to register your player id for the private test and demonstration (add your id to set).

- Debug:
  - If you want to review the timer, display command at timer instance is provided to review.
    You need to click "F4" for checking.
    Naming timer as "master_group:timer_id" is encouraged.
    Test → timer_display is the only method to review inner timer (included element ":").