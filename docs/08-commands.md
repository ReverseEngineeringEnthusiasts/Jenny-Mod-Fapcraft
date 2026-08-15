# Commands

| Command | Registered by | Level | What it does |
|---|---|---|---|
| `/locatenearestgoblinlair` | Main (server) | any | Finds the closest goblin lair (a goblin flagged `aX`) to the sender and prints its coordinates. Rejects the Nether/End outright. |
| `/futa <true\|false>` | ClientProxy (client) | any | Toggles the **Galath futa** feature. Persists the flag to `sexmod/futa` (read back in the constructor); when enabled, spawns dragon-breath particles at the `cockParticles` bone of every local Galath. |
| `/reloadcustommodels` | Main (server) | **op 2** | Reloads the server's custom-model registry and pushes the fresh model scales to every connected player via UnknownPacket, so client rendering matches the new registry without a rejoin. |
| `/setmodelcode` | ClientProxy (client) | any | Client-side upload of a custom model code (and optional `$`-separated part-id list) for the targeted girl; without a valid girl target it applies to the sender's own player-girl. Payload goes via UploadModelStringPacket for server-side validation + persistence. |
| `/whitelistserver [confirm]` | ClientProxy (client) | any | Client-side opt-in for a server to push custom models: adds the current server IP to `sexmod/custom_models/whitelisted_servers.txt` and requests the model download. The two-step confirm is a security gate — only whitelist servers you trust. |

## Dev-only commands (DebugMode, obfuscated builds never load them)

| Command | What it does |
|---|---|
| `set <N> <value>` | Set dev-float array entry (renderer tuning constants) |
| `get <N>` | Read a dev-float entry |
| `time` | Print timing info |
| `girls` | List girls |
| `kobs` | List kobolds |
| `setcumtime` | Set the cum-time |
| `resetcolor` | Reset color state |
