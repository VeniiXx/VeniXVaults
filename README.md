# VeniXVaults

VeniXVaults is a modern personal vault plugin for Paper 1.21.x through 26.x servers. It is built from scratch for Turkish Minecraft communities that want a clean, branded, configurable storage system.

## Features

- `/depo` vault selector GUI
- `/depo <number>` direct vault opening
- Per-permission vault count: `venixvaults.vaults.5`, `venixvaults.vaults.10`, etc.
- Per-permission vault size: `venixvaults.size.3` to `venixvaults.size.6`
- Admin open command: `/venixvaults open <player> <vault>`
- YAML data storage with Base64 Bukkit item serialization
- Auto-save and save-on-close
- Data backup command: `/venixvaults backup`
- Disabled-world support
- Item blacklist with bypass permission
- Hex color support in messages using `&#RRGGBB`
- Turkish default config and messages

## Requirements

- Paper 1.21.x - 26.x
- Java 21 or newer

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/depo` | `venixvaults.use` | Opens the vault selector |
| `/depo <number>` | `venixvaults.use` | Opens a specific vault |
| `/venixvaults help` | none | Shows help |
| `/venixvaults reload` | `venixvaults.admin` | Reloads config and messages |
| `/venixvaults save` | `venixvaults.admin` | Saves cached vaults |
| `/venixvaults backup` | `venixvaults.admin` | Creates a zip backup of vault data |
| `/venixvaults open <player> <number>` | `venixvaults.admin` | Opens another player's vault |
| `/venixvaults info <player>` | `venixvaults.admin` | Shows player vault info |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `venixvaults.use` | true | Allows vault use |
| `venixvaults.admin` | op | Allows admin commands |
| `venixvaults.vaults.<amount>` | false | Grants more vaults |
| `venixvaults.size.<rows>` | false | Grants larger vaults |
| `venixvaults.bypass.world` | op | Bypass disabled worlds |
| `venixvaults.bypass.blacklist` | op | Bypass item blacklist |

## Build

```bash
mvn clean package
```

The compiled jar will be in `target/VeniXVaults-1.0.0.jar`.

## License

MIT License. You can publish and modify this plugin, but keep the license notice.
