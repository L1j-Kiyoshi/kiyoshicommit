::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Backup the L1J database
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
::
:: This program is free software: you can redistribute it and/or modify
:: it under the terms of the GNU General Public License as published by
:: the Free Software Foundation, either version 3 of the License, or
:: (at your option) any later version.
::
:: This program is distributed in the hope that it will be useful,
:: but WITHOUT ANY WARRANTY; without even the implied warranty of
:: MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
:: GNU General Public License for more details.
::
:: You should have received a copy of the GNU General Public License
:: along with this program.  If not, see <http://www.gnu.org/licenses/>.
::
:: Copyright (c) L1J-JP Project All Rights Reserved.
@echo off
echo Backup the database...

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: MySQL Config
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
set database=l1jdb
set username=root
set password=""

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: CSV Config
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
set delimiter=,
set enclosed=
set newline=\r\n
set tmpdir=%~dp0
set outdir=%tmpdir:\=/%backup

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Date Format
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
set tempdate=%date:/=%
set temptime=%time: =0%
set yyyy=%tempdate:~0,4%
set mm=%tempdate:~4,2%
set dd=%tempdate:~6,2%
set hh=%temptime:~0,2%
set ii=%temptime:~3,2%
set ss=%temptime:~6,2%
set datetime=%yyyy%-%mm%-%dd%_%hh%h%ii%m%ss%s

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Create dump directory
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
if not exist .\backup md .\backup
if not exist .\backup\%datetime% md .\backup\%datetime%

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: Dump commands table
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
echo %outdir%/%datetime%/commands.csv
set query=^
SELECT 'name','access_level','class_name' ^
UNION SELECT * INTO OUTFILE '%outdir%/%datetime%/commands.csv' ^
FIELDS TERMINATED BY '%delimiter%' ENCLOSED BY '%enclosed%' ^
LINES TERMINATED BY '%newline%' ^
FROM %database%.commands
mysql -u %username% -p%password% -e "%query%" --local-infile=1
if errorlevel 1 goto ERR

::::::
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:END
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
echo Backup is complete.
exit \b 0

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:ERR
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
pause
exit \b 1
