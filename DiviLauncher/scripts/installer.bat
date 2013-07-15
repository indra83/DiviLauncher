set APP_DIR=""
set APP_NAME="DiviLauncher.apk"
set APP=%APP_DIR%\%APP_NAME%

adb push %APP% /mnt/sdcard/

echo su > cmd.txt
echo mount -o remount,rw /system >> cmd.txt

echo mv /mnt/sdcard/%APP_NAME% /system/app/ >> cmd.txt

echo mv /system/app/SystemUI.apk /system/app/SystemUI.apk.bkp >> cmd.txt

echo mv /system/app/SystemUI.odex /system/app/SystemUI.odex.bkp >> cmd.txt

echo mv /system/app/Launcher2.apk /system/app/Launcher2.apk.bkp >> cmd.txt
echo mv /system/app/Launcher2.odex /system/app/Launcher2.odex.bkp >> cmd.txt

echo mount -o remount,ro /system >> cmd.txt

echo reboot >> cmd.txt

echo exit >> cmd.txt
echo exit >> cmd.txt

adb shell < cmd.txt

