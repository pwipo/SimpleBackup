@echo off
set LocalJAVA="java"
set LocalJAVAOptions=-jar SimpleBackup.jar
set LocalSource=C:\Documents
set LocalDestination=D:\archive
set LocalSettings=%LocalDestination%\arch_info.txt
set LocalCheckDestination=false

%LocalJAVA% %LocalJAVAOptions% %LocalSource% %LocalDestination% %LocalSettings% %LocalCheckDestination%
