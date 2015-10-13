::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: ServerRestartBat Config
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
@echo off
set filemove=0
set workdrive=
set workspace=
set serverdrive=
set serverfolder=

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: File Copys
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

if %filemove%==1 (
	@echo on
	timeout 5
	%drive%
	cd \
	cd %workspace%
	move /y l1jserver.jar %serverdrive%\%serverfolder%
	cd \
	cd %serverfolder%
	timeout 5
	@echo off
) else (
	@echo on
	timeout 10
	@echo off
)

::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
:: ServerRestart
::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
@echo on
@java -server -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -Xms1024m -Xmx1024m -XX:NewRatio=2 -XX:SurvivorRatio=8 -jar l1jserver.jar