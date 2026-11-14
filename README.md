# Refurbished Cams (Forge 1.20.1)
Addon for **MrCrayfish's Furniture Mod: Refurbished** (required, >= 1.0.12 + its Framework dependency).

- Craft/place **Security Cameras** (they look where you look).
- Open the **Computer** from Refurbished -> run the **Cameras** program -> it lists cameras within 500 blocks
  (config `detectionRadius`) -> click one to see its live view. Arrow keys rotate, ESC exits.

## Build the .jar
- Linux/macOS/Git Bash: `./build.sh`  |  Windows: `build.bat`
- Result: `build/libs/refurbishedcams-1.20.1-1.0.0.jar`

## GitHub Actions
Every push to `main` builds the jar (Actions -> run -> Artifacts).
Publish a Release: `git tag v1.0.0 && git push origin v1.0.0`.
