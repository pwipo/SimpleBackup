@echo off
set LocalJAVA="java"
rem set LocalJAVAOptions=-cp C:\SimpleBackup ru.seits.simplebackup.Backup
set LocalJAVAOptions=-jar SimpleBackup.jar
set LocalSource=C:\Documents
set LocalDestination=d:\archive
set LocalNameStart=arch
set LocalSettings=%LocalSource%\%LocalNameStart%_info.txt

echo %LocalJAVA% %LocalJAVAOptions% %LocalSource% %LocalDestination% %LocalSettings% %LocalNameStart%
%LocalJAVA% %LocalJAVAOptions% %LocalSource% %LocalDestination% %LocalSettings% %LocalNameStart%
