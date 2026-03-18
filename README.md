# Guard - Native Android App (Kotlin)

## Переписано на Kotlin!

Полностью нативное Android приложение на Kotlin.

### Сборка

```bash
cd guard-kotlin
./gradlew assembleDebug
```

### Структура проекта

- `app/src/main/java/com/guardian/app/` - Kotlin исходный код
- `app/src/main/res/` - Ресурсы (layouts, strings, colors)

### Экраны

- Home - главный экран с щитом
- Blacklist - чёрный список приложений
- Log - журнал событий
- Settings - настройки

### Технологии

- Kotlin
- AndroidX
- Material Design 3
- ViewModel + StateFlow
- DataStore (вместо AsyncStorage)
- Navigation Component
