backup data which minimum parameters and write operation in destination folder (only copy file)

required java 8

backup: ru.seits.simplebackup.Backup
useful backup.bat
used params:
1 - source folder (need exist) (bat file variable - LocalSource)
2 - archive folder (need exist) (bat file variable - LocalDestination)
3 - setting file. used for save state (bat file variable - LocalSettings)

if no dest subfolders and setting file - create new archive else update exist archive

restore: ru.seits.simplebackup.Restore
useful restore.bat
used params:
1 - archive folder (need exist) (bat file variable - LocalSource)
2 - destination folder (need exist) (bat file variable - LocalDestination)
3 - setting file. used for restore last files (bat file variable - LocalSettings)

restore last files from archive
