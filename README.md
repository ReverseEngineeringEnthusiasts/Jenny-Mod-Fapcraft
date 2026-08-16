# Jenny Mod / Fapcraft 1.12.2 Remapped + Deobfuscated

Warning: This is an 18+ project (Adults Only!)

### Why?

This project took about 3 days of work, done for the love of java reverse engineering of course. (2 hands were used, at times...) 

I was thinking about what to write for this project. Initially I expected this to be a pretty easy decompilation project with SRG remapping for 1.12.2 forge but then I discovered that the new developer "trolmastercard" had started to obfuscate the later versions of Jenny Mod and so then I was forced to actually deobfuscate the java mod (ZKM 13 Obf) and remap everything. This took some real effort and process was quite interesting.

I entered this challenge thinking I was perhaps the first person to do this.<br>
Upon a github code search mid way through the project I had found:
- https://github.com/palkaline/jenny-mod-re existed (by @palkaline 1.12.2) 
- https://github.com/Angina830/Jenny-mod-1.21.1 (by @Angina830 1.21 recreation)

So I am by all means late to the game. 
<br>Nontheless what I have done here is unique in the sense that my intentions were for a **FULL remap** to the point that the deobfuscated code FULLY matched the likely sourcecode that the developer of the actual project was holding. This means every class, method, and field that could be renamed has been renamed to something meaningful.

All of this is REAL work done on a project that would otherwise have had hidden code which could have been malicious in nature. Most of the pipeline is documented here (For ZKM deobf seek https://github.com/OpenVapeCN/zkm-flowdeobf and https://github.com/Diobf/Deobfuscator and also https://github.com/java-deobfuscator) 

My intentions for this project is mainly to have a VERIFIED safe place to download the mod, unlike the many shady websites propped up.<br> This project aims to fill the position as all the correct avenues were all taken down by the original developer.<br> Palkaline mentions of the insidious reddit subreddit (https://www.reddit.com/r/jennymod/) in which it seems the administrators are ACTIVELY being malicious.<br> They do not let you send remapped opensource versions under the pretense of "letting the mod die" (??? Why continue to moderate the subreddit)<br> So as to not violate Github's terms of service I will keep my thoughts on the grotesque endings asinine reddit mods deserve to myself.<br> Nontheless I will comment that they (the vast majority of reddit mods) are pathetic beings who's only source of pleasure and "work" is derived from being terminally-on-reddit power-tripping pricks in moderation of hobbyist places pushing their own flawed agendas.<br> They are hacks without talent, gifted power through being the first to hit a button to create a subreddit.

### Privacy

It's no secret that typing "Jenny Mod" leads you to all sorts of malware/adware filled websites. The developer is also dead-silent on exactly when they will release updates (hints that it's abandoned perhaps due to a letter from Mojang) The palkaline-port dude speculates that the dev farms money from horned-out Minecraft porn addicts on patreon and doesn't wish to put in the work for another update (maybe also due to some sort of threatened legal action by Mojang) which could be true. 

Currently the only "official" method left is a Discord server where the administrators ask for your government ID. God knows what the hell they are doing with it. You should NEVER give your ID to anyone. You can barely trust companies to have the bare-minimum systems to keep it safe from leaks, and yet you would trust some idiot on Discord? Horrible Operational Security.

The biggest reason for utilising this sourcecode is the fact that YOU KNOW it's safe. You have the FULL code, completely readable, you can mod it however you like. The only connections here are to the Mojang API to get your skin. You can't know that with the obfuscated "official" mod. If you do wish to get an official version you can still go to the fapcraft.org domain on archive.org howeve do so at your own discretion. 

## Features

- Fully deobfuscated and remapped source for Forge 1.12.2
- Forge SRG → MCP mappings applied
- ZKM 13 obfuscation fully reversed (dev probably pirated it)
- Fully buildable Maven 1.12.2 Mod
- Pipeline documented
- docs/ has all the documentation necessary for developer understanding

You could use a free AI-agent like [Opencode Zen's Deepseek v4 flash free](https://opencode.ai/download) to hack on features, no promises that it will be great. I would recommend that you learn Blockbench and working with Geckolib if you wish to add on features (Human work is always the best!)

### How do I download this?
You go to releases on the right hand side, click on the "Latest" build and then download the .jar and place it in your AppData folder .minecraft/mods/

Link to latest jar build: https://github.com/ReverseEngineeringEnthusiasts/Jenny-Mod-Fapcraft/releases/latest

### Windows

1. Make sure you have Forge 1.12.2 installed from [files.minecraftforge.net Forge 1.12.2](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.12.2.html).  
2. Press `Win + R`, type `%appdata%` and then hit Enter.  
3. Go to `.minecraft` and then `mods`.  
4. Place the downloaded release `.jar` there.  
5. Launch the Minecraft Launcher, select the Forge 1.12.2 profile, and run.

### Prism Launcher (Recommended)

1. Install [Prism Launcher](https://prismlauncher.org/).  
2. Create a new instance, press **Modrinth**, search “More FPS [FORGE]”.  
3. In the version filter, select **1.12.2** and install.  
4. Launch once, then close.  
5. Right-click the instance → **Edit** → **Mods** → **Open Folder**.  
6. Paste in the Jenny Mod/Fapcraft latest `.jar`, ensure it is enabled.  
7. Run. This also gives you some performance mods.

### Linux / macOS

The process is nearly identical:

- Linux: `~/.minecraft/mods/`  
- macOS: `~/Library/Application Support/minecraft/mods/`

## MIT License <3