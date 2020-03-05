# ModMenu
~~Hard to be more descriptive than that.~~ It enriches the standard Minecraft menu with an interface displaying a one-dimensional array of modifications



A picture's worth 2 words

![](https://i.imgur.com/JKEatou.png "Mod Menu")

### Developers:
- Mod Menu is on maven at: https://maven.fabricmc.net/io/github/prospector/modmenu/
- The icon comes from the icon specified in your fabric.mod.json (as per the spec)
- Clientside-only and API badges are defined as custom objects in your fabric.mod.json as such:
```json
"custom": {
    "modmenu:api": true,
    "modmenu:clientsideOnly": true
}
```
- Mod parenting is used to display a mod as a child of another one. This is meant to be used for mods divided into different modules. The following element in a fabric.mod.json will define the mod as a child of the mod 'flamingo':
```json
"custom": {
    "modmenu:parent": "flamingo"
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
        - Mods can provide a Screen factory to provide a custom config screen to open with the config button. Implement the `getModConfigScreenFactory` method in your API implementation to do this.
        - Mods can provide Screen factories to provide a custom config screens to open with the config buttons for other mods as well. Implement the `getProvidedConfigScreenFactories` method in your API implementation for this.
