# WearableAI - Resting / Not-Resting Detection

An Android app (Java) that uses the phone's built-in 3-axis accelerometer and an on-device RNN-LSTM model (TensorFlow Lite) for **human activity recognition**, summarised as a **Resting vs. Not-Resting** score.

## How it works

- `MainActivity` registers a listener for `Sensor.TYPE_ACCELEROMETER` (`SENSOR_DELAY_NORMAL`) and shows the live X / Y / Z values.
- Samples are buffered into windows of **125 samples x 3 axes**.
- Each full window is passed to `HARClassifier`, which loads `app/src/main/ml/model_HAR_RNNLSTM.tflite` and returns probabilities for 6 activity classes.
- The 6 class probabilities are summed into two values shown on screen:
  - **Resting** = classes 0 + 1 + 4
  - **Not-Resting** = classes 2 + 3 + 5

  (Commented-out code in `MainActivity` maps the indices as 0 Sitting, 1 Standing, 2 Upstairs, 3 Downstairs, 4 Walking, 5 Running.)

## Tech stack

- Java, Android Gradle Plugin 7.0, Gradle 7.0.2
- `compileSdk` 30, `targetSdk` 30, **`minSdk` 19**, ABI filter `x86`
- TensorFlow Lite (+ select TF ops for LSTM support), TFLite Support library, ML model binding
- Chaquopy 10 Gradle plugin is applied (Python build configured), but the project currently contains no Python sources
- AndroidX AppCompat, Material Components, ConstraintLayout

## Project structure

```
app/src/main/java/com/example/har_lstm_androidsensor/
    MainActivity.java     accelerometer listener, windowing, UI
    HARClassifier.java    TFLite interpreter wrapper
app/src/main/ml/model_HAR_RNNLSTM.tflite   trained HAR model
```

## Build and run

1. Install Android Studio and a Python 3 interpreter (required by the Chaquopy plugin at build time).
2. In `app/build.gradle`, change `python { buildPython "..." }` to your own Python executable (it currently points to a local Anaconda path on the original author's machine).
3. The ABI filter is set to `x86`; change `ndk { abiFilters ... }` to `"armeabi-v7a"` / `"arm64-v8a"` to run on a physical ARM phone.
4. Sync Gradle and run the `app` configuration, or build from the command line:
   ```
   ./gradlew assembleDebug
   ```

## Notes

- The TensorFlow Lite core dependencies use `0.0.0-nightly-SNAPSHOT` versions and `settings.gradle` still lists `jcenter()`, which may need updating for a fresh build.
