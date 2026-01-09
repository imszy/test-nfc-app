#!/bin/bash
set -e

echo "=== 设置环境变量 ==="
export JAVA_HOME="$HOME/android-dev/jdk-17"
export ANDROID_HOME="$HOME/android-dev/android-sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

echo "=== 清理旧构建 ==="
./gradlew clean

echo "=== 构建 Release APK ==="
./gradlew assembleRelease

echo "=== 构建 AAB ==="
./gradlew bundleRelease

echo ""
echo "✅ 构建完成！"
echo ""
echo "APK 位置："
ls -lh app/build/outputs/apk/release/*.apk
echo ""
echo "AAB 位置："
ls -lh app/build/outputs/bundle/release/*.aab
