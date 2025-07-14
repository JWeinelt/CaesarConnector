# 🏛️ Caesar Connector

![Static Badge](https://img.shields.io/badge/version-v0.0.1-green)[![Maven](https://github.com/JWeinelt/CaesarConnector/actions/workflows/maven.yml/badge.svg)](https://github.com/JWeinelt/CaesarConnector/actions/workflows/maven.yml)

Caesar is a full-featured server management system for Minecraft servers. It seemlessly integrated with **CloudNET v4** and provides you an advanced interface for everything.
With this Minecraft plugin, Caesar will connect to your Minecraft servers, either running standalone or with a cloud management software like CloudNET.

> [!WARNING]
> This plugin won't work without a connection to a central Caesar backend ([GitHub](https://github.com/Jweinelt/Caesar)).

## 🌟 Features
- 🔐 Secure and authenticated connection to your Caesar backend
- 🖥️ Live server console in the Caesar dashboard
- 🎮 Remote control of your Minecraft instance
- 🔨 Built-in moderation tools (ban, mute, kick)
- 🗣️ Player reports directly from the game
- 🔌 Plugin and extension integration (Spark, LuckPerms, etc.)
- 🧩 Developer-friendly architecture with a global API


## 💻 Plugins/extensions
Caesar is designed to be developer and user friendly. Therefore, developers may add "extension functionality" into their Caesar plugins. These plugins will also work with this Minecraft plugin and e.g. make data queries.

If you want to find plugins and/or extensions, head to the [Caesar plugin marketplace](https://market.caesarnet.cloud).

## ⚙️ Installation
### Requirements
- Java 15 or newer (CloudNET compatibility: Java 24)
- A Caesar backend (v1.0.0 or newer)
- Minecraft server running Paper 1.17.2 or newer (forks may work too)
- An internet connection (recommended)
- Caesar not running on SQLite or H2

> [!WARNING]
> The database engines **SQLite** and **H2** are not recommended for production environments and may cause data inconsistency.

> [!TIP]
> Caesar is using **semantic versioning** in all modules.
> That means, that e.g. CaesarConnector v1.__2__.1 is compatible with CaesarServer v1.__2__.1 and v1.2.__8__ aswell.

### Getting started
- 📥 Download the latest release from [Releases](https://github.com/Jweinelt/CaesarConnector/releases)
- 🛑 Stop your Minecraft server
- 📂 Put the downloaded jar file into your `plugins` folder
- 🛫 Start your server

> [!CAUTION]
> Using Plugman/PlugmanX for loading the plugin is **not recommended!** It may work without errors, but if anything is not running correct, please **restart** your server!
> When searching for help, you will always be asked to restart it when using a plugin like Plugman!
> This problem may occur while registering components such as commands and permissions.

## 🔌 API
Read more about the API of Caesar [here](https://github.com/Jweinelt/CaesarConnector/wiki/API)

## 🧪 Contributing
Contributions are welcome! Just clone this repository (for backend modifications):

```bash
git clone https://github.com/JWeinelt/CaesarConnector.git
```
Please create a pull request for any contributions and use the [Code Conventions](https://github.com/JWeinelt/Caesar/wiki/Developer-Conventions).

If you find a problem with Caesar, please open an issue. But report any security issues using the ticket system on [Discord](https://dc.caesarnet.cloud).

## 🤝 License

This project is licensed under [GNU GPL v3 License](https://www.gnu.org/licenses/gpl-3.0.en.html).


## 🧭 Road map

- [ ] Report system with GUIs
- [ ] Punishment system
- [ ] Live console
- [ ] Hook into CloudNET API (new branch)
- [ ] Integrate Spark, LuckPerms, etc.

---

> **Caesar** – bring organization into your servers.

## ❤️ These amazing people make Caesar big!

<a href="https://github.com/FireAnimationStudios "><img src="https://github.com/FireAnimationStudios.png" width="50" height="50" alt="@FireAnimationStudios "/></a>
<a href="https://github.com/PhastixTV"><img src="https://github.com/PhastixTV.png" width="50" height="50" alt="@PhastixTV"/></a>
<a href="https://github.com/zCreeperYT"><img src="https://github.com/zCreeperYT.png" width="50" height="50" alt="@zCreeperYT"/></a>
<a href="https://github.com/ProJakob"><img src="https://github.com/ProJakob.png" width="50" height="50" alt="@ProJakob"/></a>
