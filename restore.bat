@echo off
set LocalJAVA="java"
set LocalJAVAOptions=-cp SimpleBackup.jar ru.seits.simplebackup.Restore
set LocalSource=D:\archive
set LocalDestination=C:\Documents
set LocalSettings=%LocalSource%\arch_info.txt

%LocalJAVA% %LocalJAVAOptions% %LocalSource% %LocalDestination% %LocalSettings%
