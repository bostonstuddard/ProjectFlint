# Flint Quests Integration

Project Flint integrates with Flint Quests through its public `CUSTOM_EVENT` API when Flint Quests is installed. Flint Quests is optional; Project Flint does not require it to launch.

## Workstation event IDs

A successful claimed workstation result emits:

- `projectflint:workstation/any`
- `projectflint:workstation/<station>`
- `projectflint:workstation/<station>/output/<namespace>/<item_path>`

The output-specific event increments by the produced stack count. The generic/station events increment once per operation/result claim.

Current station paths:

- `clay_grinder`
- `wooden_grinder`
- `sanding_station`
- `bookshelf`
- `primitive_oven`
- `advanced_kiln`
- `lumber_processor`

The direct-interaction heat processors do not have result slots. Starting a valid processing operation emits the generic event plus:

- `projectflint:workstation/primitive_campfire`
- `projectflint:workstation/baked_kiln`
- `projectflint:workstation/mortar_kiln`

Lumber Processor formation additionally emits:

- `projectflint:workstation/lumber_processor_formed`

## API behavior

Project Flint resolves the public Flint Quests method at runtime:

`com.projectflint.flintquests.api.FlintQuestHooks.trigger(ServerPlayer, String, int)`

No Flint Quests classes are linked directly into Project Flint bytecode, so removing Flint Quests simply leaves these custom events without a consumer.

Project Flint also exposes `WorkstationQuestEvents.CRAFT` as a normal Fabric event and publishes that event through Fabric Loader ObjectShare under `projectflint:workstation_craft_event` for other integrations.
