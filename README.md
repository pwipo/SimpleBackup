backup data which minimum parameters and write operation in destination folder (only copy file). best chose for backup on flash,sdd,tap.


required java 8


backup: ru.seits.simplebackup.Backup
use backup.bat
if no dest subfolders and setting file - create new archive else update exist archive

Example: backup.bat source_path archive_path file_setting check_archive 
params:
source_path - source folder (need exist) (bat file variable - LocalSource)

archive_path - archive folder (need exist) (bat file variable - LocalDestination)

file_setting - setting file. used for save state (bat file variable - LocalSettings)

check_archive - if updating archive, ant this set true, then check archive, and copy not exist files - slow operation. set false for increase speed.


restore: ru.seits.simplebackup.Restore

useful restore.bat

restore last files from archive

Example: restore.bat archive_path destination_path file_setting 

params:

archive_path - archive folder (need exist) (bat file variable - LocalSource)

destination_path - destination folder (need exist) (bat file variable - LocalDestination)

file_setting - setting file. used for restore last files (bat file variable - LocalSettings)

