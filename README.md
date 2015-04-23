backup data which minimum parameters and write operation in destination folder (only copy file)

required java 8

main class ru.seits.simplebackup.Backup
used params:
1 - source folder (need exist)
2 - destination folder (need exist)
3 - setting file. used for save state

action:
if no dest subfolders and setting file - create new archive
else update exist

