set APP_DIR=C:\Users\VAIO\Downloads
set APP_LAUNCHER_NAME=DiviLauncher3b.apk
set OLD_APP_LAUNCHER_NAME=DiviLauncher3a.apk
set APP_NAME=Divi1a.apk
set APP_LAUNCHER=%APP_DIR%\%APP_LAUNCHER_NAME%
set APP=%APP_DIR%\%APP_NAME%

echo %APP%
echo %APP_LAUNCHER%
echo %APP_LAUNCHER_NAME%

pause

adb install %APP% 
adb push %APP_LAUNCHER% /mnt/sdcard/

pause

echo su > cmd.txt
echo mount -o remount,rw /system >> cmd.txt

:: can't use mv across partitions, use dd intead
echo dd if=/mnt/sdcard/%APP_LAUNCHER_NAME% of=/system/app/%APP_LAUNCHER_NAME% >> cmd.txt
::echo rm /system/app/%OLD_APP_LAUNCHER_NAME% >> cmd.txt
echo chmod 644 /system/app/%APP_LAUNCHER_NAME% >> cmd.txt

echo mv /system/app/SystemUI.apk /system/app/SystemUI.apk.bkp >> cmd.txt

echo mv /system/app/SystemUI.odex /system/app/SystemUI.odex.bkp >> cmd.txt

echo mv /system/app/Launcher2.apk /system/app/Launcher2.apk.bkp >> cmd.txt
echo mv /system/app/Launcher2.odex /system/app/Launcher2.odex.bkp >> cmd.txt

echo mount -o remount,ro /system >> cmd.txt

echo reboot >> cmd.txt

echo exit >> cmd.txt
echo exit >> cmd.txt

echo "About to run the following :"
type cmd.txt
pause

adb shell < cmd.txt
