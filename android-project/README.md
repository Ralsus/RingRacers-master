# Ring Racers Android Port

[![Build Android APK](https://github.com/YOUR_USERNAME/RingRacers-Android/actions/workflows/android-build.yml/badge.svg)](https://github.com/YOUR_USERNAME/RingRacers-Android/actions/workflows/android-build.yml)

Неофициальный Android порт [Dr. Robotnik's Ring Racers](https://www.kartkrew.org/) с сенсорным управлением.

## 📱 Возможности

- ✅ Сенсорное управление (D-Pad + кнопки)
- ✅ Landscape ориентация
- ✅ Поддержка Bluetooth геймпадов
- ✅ Fullscreen immersive mode
- ✅ Вибрация при нажатии

## ⬇️ Скачать APK

1. Перейдите в [Actions](../../actions)
2. Выберите последний успешный билд
3. Скачайте `RingRacers-debug-apk`

## 🛠️ Сборка

### Требования
- Android Studio или Gradle
- JDK 17
- Android NDK 25.2

### Команды

```bash
cd android-project
./gradlew assembleDebug
```

APK будет в: `app/build/outputs/apk/debug/`

## 📁 Файлы игры

Файлы .pk3 уже включены в репозиторий! После установки APK игра готова к запуску.

Если нужно обновить файлы:
1. Скачайте Ring Racers: https://www.kartkrew.org/
2. Скопируйте .pk3 в `android-project/app/src/main/assets/gamedata/`


## 🎮 Управление

```
┌────────────────────────────────────┐
│                                    │
│   [D-PAD]            [ITEM]        │
│    ←↑→↓         [DRIFT] [GAS]      │
│                      [BRAKE]       │
│                                    │
└────────────────────────────────────┘
```

## 📄 Лицензия

GPL v2.0 - см. [LICENSE](LICENSE.txt)

## ⚠️ Дисклеймер

Это неофициальный порт. Kart Krew Dev не поддерживает Android версии.
