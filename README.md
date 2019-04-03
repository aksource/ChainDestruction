ChainDestruction
================

this add chain destruction system in Minecraft


# How to use

## First:Register tools
1. Pick a tool you want to register.
1. Press 'K' key (changeable in game).
1. Chat show "Add Tool: xxx", registration is success.

## Second:Register blocks
1. Pick a registered tool.
1. Press 'Shift' key and 'Right' click.
1. Chat show "Add Block : xxxx", registration is success.

## Third:Break registered blocks at once.
1. Break a registered block using a registered tool.
1. When a block is just broken, same block next to be broken block is also broken.(Chain destruction)

## Change woodcutter mode
1. Press 'SEMICOLON' key (changeable in game).
1. Changed normal mode to wood cutter mode.
- In woodcutter mode, block registration is different to normal mode.
- In woodcutter mode, all registered block is in 3 * 3 * 3 range of broken block is broken.

## Change individual tool mode
1. Press 'Shift' key and 'SEMICOLON' key (changeable in game).
1. Changed normal mode or wood cutter mode to individual tool (normal/woodcutter) mode.
- In individual tool mode, block registration is not global but is by tools.
- In individual tool mode, you can change normal/woodcutter mode(pressing 'SEMICOLON' key).

## Change the range of Chain Destruction
1. Pick a registered tool.
1. Click middle button on your mouse (with 'Shift' key).
1. Then the range increases (decreases) one block wroth.


## Config parameters
- destroyingSequentiallyMode
  - false:Destruction target blocks is broken at once.
  - true:Destruction target blocks is broken little by little.
- digTaskMaxCounter:Destruction process wait time (per tick).
- excludeRegisterItem:Exclude Item to register chain destruction. comma separated.
- maxYforTreeMode:Max Height of destroyed block for woodcutter mode. Be careful to set over 200.
- notToDestroyItem:Stop Destruction not to destroy item