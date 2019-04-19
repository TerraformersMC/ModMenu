# ModMenu
~~Hard to be more descriptive than that.~~ It enriches the standard Minecraft menu with an interface displaying a one-dimensional array of modifications



A picture's worth 2 words

![](https://i.imgur.com/JKEatou.png "Mod Menu")

### Developers:
- Mod Menu is on maven at: https://maven.fabricmc.net/io/github/prospector/modmenu/ModMenu/
- The icon comes from the icon specified in your fabric.mod.json (as per the spec)
- Clientside-only and API badges are defined as custom objects in your fabric.mod.json as such:
```json
"custom": {
    "modmenu:api": true,
    "modmenu:clientsideOnly": true
}
```
- ModMenuAPI
    - To use the API, implement the ModMenuApi interface on a class and add that as an entry point of type "modmenu" in your fabric.mod.json as such:
  ```json
  "entrypoints": {
	"modmenu": [ "com.example.mod.ExampleModMenuApiImpl" ]
  }
  ```
    - Features
        - Mods can provide a Supplier<Screen> to provide a custom config screen to open with the config button. Implement the `getConfigScreen` method in your API implementation.
