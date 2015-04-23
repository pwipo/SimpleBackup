@echo off
set LocalJAVA="java"
set LocalJAVAOptions=-jar SimpleBackup.jar
set LocalSource=C:\Documents
set LocalDestination=D:\archive
set LocalNameStart=arch
set LocalSettings=%LocalDestination%\%LocalNameStart%_info.txt

%LocalJAVA% %LocalJAVAOptions% %LocalSource% %LocalDestination% %LocalSettings% %LocalNameStart%
