# SeizureWatch Android Prototype

Prototipo Android nativo en Kotlin + Jetpack Compose para monitorización de riesgo convulsivo/cardiaco.

## Compilar por terminal en Windows

Abre PowerShell dentro de la carpeta del proyecto y ejecuta:

```powershell
.\gradlew.bat assembleDebug
```

La primera vez el script descargará Gradle automáticamente y luego compilará el proyecto.

La APK esperada queda en:

```text
app\build\outputs\apk\debug\app-debug.apk
```

## Compilar por terminal en macOS / Linux

```bash
chmod +x ./gradlew
./gradlew assembleDebug
```

## Requisitos

- JDK 17
- Android SDK instalado
- Variable `ANDROID_HOME` o `sdk.dir` configurada

Si no tienes `local.properties`, crea uno en la raíz del proyecto con algo así:

```properties
sdk.dir=C:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
```

## Nota

Este wrapper es simplificado para el prototipo: descarga Gradle 8.10.2 si todavía no está presente en tu equipo.
