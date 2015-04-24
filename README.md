backup data which minimum parameters and write operation in destination folder (only copy file). best chose for backup on flash,sdd,tap.


required java 8


backup: ru.seits.simplebackup.Backup

if no dest subfolders and setting file - create new archive else update exist archive

for backup use - backup.bat (without params)

bat file internal variables:

LocalSource - source folder (need exist)

LocalDestination - archive folder (need exist) 

LocalSettings - setting file. used for save state

LocalCheckDestination - if updating archive, ant this set true, then check archive, and copy not exist files - slow operation. set false for increase speed.


restore: ru.seits.simplebackup.Restore

restore last files from archive

for restore use - restore.bat (without params)

bat file internal variables:

LocalSource - archive folder (need exist)

LocalDestination - destination folder (need exist) 

LocalSettings - setting file. used for restore last files

