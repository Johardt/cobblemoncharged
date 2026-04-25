# Cobblemon: Charged

Cobblemon: Charged is a mod that makes Cobblemon machines require power (In the form of Forge Energy or Fabric Energy).
This energy must be provided via some external mod that adds power generation.  
Without such a mod, this mod effectively disables cobblemon machines such as the healing machine or fossil analyzer.

## Configuration

On first launch, the mod creates `config/cobblemoncharged.json` on both Fabric and NeoForge.

```json
{
  "healingMachine": {
    "energyPerCharge": 4000
  },
  "fossilAnalyzer": {
    "energyPerRestorationTick": 20
  }
}
```

Fabric and NeoForge do not require an additional config mod for this file. Optional client-side config screens, such as Mod Menu with Cloth Config on Fabric, are separate UI conveniences and are not required.
