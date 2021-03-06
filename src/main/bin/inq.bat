@echo off
rem Copyright 2006 Inqwell Ltd
rem
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.
rem Credit: Apache Forrest startup script for windows
rem     http://www.apache.org/licenses/LICENSE-2.0

rem ----- use the location of this script to infer $INQHOME -------
if NOT "%OS%"=="Windows_NT" set DEFAULT_INQHOME=..
if "%OS%"=="Windows_NT" set DEFAULT_INQHOME=%~dp0\..
if "%OS%"=="WINNT" set DEFAULT_INQHOME=%~dp0\..
if "%INQHOME%"=="" set INQHOME=%DEFAULT_INQHOME%

rem ----- other variables for inq system properties
if "%INQDOMIMPL%"=="" set INQDOMIMPL="com.sun.org.apache.xerces.internal.dom.DocumentImpl"
if "%INQJMSFACTORY%"=="" set INQJMSFACTORY="com.sun.messaging.ConnectionFactory"


rem ----- if JAVAHOME is set then use that for the launcher. Otherwise just assume java
set LAUNCHER=java
if "%JAVAHOME%" NEQ "" set LAUNCHER=%JAVAHOME%\bin\java
if "%JAVA_HOME%" NEQ "" set LAUNCHER=%JAVA_HOME%\bin\java

rem ----- set the current working dir variable  ----
if NOT "%OS%"=="Windows_NT" call "%INQHOME%\bin\setpwdvar98.bat"
if "%OS%"=="Windows_NT" call "%INQHOME%\bin\setpwdvar.bat"
if "%OS%"=="WINNT" call "%INQHOME%\bin\setpwdvar.bat"

set INQCOMMONARGS=-Dinq.home=%INQHOME% -Djava.util.logging.manager=com.inqwell.any.AnyLogManager -Dinq.xml.dom=%INQDOMIMPL% -Dinq.jms.connectionfactory=%INQJMSFACTORY%

@call %INQHOME%\bin\custom.bat

rem ----- Save and set CLASSPATH --------------------------------------------
set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=%INQHOME%\lib\inq-runtime.jar
cd /d "%INQHOME%\lib\endorsed\"
for %%i in ("*.jar") do call "%INQHOME%\bin\appendcp.bat" "%INQHOME%\lib\endorsed\%%i"
cd /d %PWD%

rem ----- Run inq ---------------------------------------------------
set mode=%1
rem Hmmm...
set arg1=%2
set arg2=%3
set arg3=%4
set arg4=%5
set arg5=%6
set arg6=%7
set arg7=%8
set arg8=%9
shift
shift
shift
shift
set arg9=%6
set arg10=%7
set arg11=%8
set arg12=%9
if "%mode%"=="-server" GOTO SERVER
if "%mode%"=="-client" GOTO CLIENT
if "%mode%"=="-load"   GOTO LOADSERVER

rem -- Assume interactive
%LAUNCHER% -Dsun.java2d.noddraw=true %INQCOMMONARGS% %INQCUSTOM% com.inqwell.any.parser.Inq %mode% %arg1% %arg2% %arg3% %arg4% %arg5% %arg6% %arg7% %arg8% %arg9% %arg10% %arg11% %arg12%
GOTO END

rem -- Server Startup
:SERVER
rem Add the headless and logging config system properties
if "%INQSERVERARGS%"=="" set INQSERVERARGS=-Xmx1024m
set INQSERVERARGS=-server -Xmx1024m -Djava.awt.headless=true -Djava.util.logging.config.file=%INQHOME%/etc/server.log.properties -Dinq_rsa.pub=%INQHOME%/keys/public.der
rem Some JDBC drivers, though there are others
set INQJDBCARGS=-Djdbc.drivers=com.mysql.jdbc.Driver:oracle.jdbc.driver.OracleDriver:com.sybase.jdbc.SybDriver
%LAUNCHER% %INQSERVERARGS% %INQCOMMONARGS% %INQCUSTOM% -Xms512m %INQJDBCARGS% com.inqwell.any.server.Server %arg1% %arg2% %arg3% %arg4% %arg5% %arg6% %arg7% %arg8% %arg9% %arg10% %arg11% %arg12%
GOTO END

:CLIENT
if "%INQCLIENTARGS%"=="" set INQCLIENTARGS=-Xms256m -Xmx512m
%LAUNCHER% -Dsun.java2d.noddraw=true %INQCOMMONARGS% %INQCUSTOM% %INQCLIENTARGS% com.inqwell.any.tools.AnyClient %arg1% %arg2% %arg3% %arg4% %arg5% %arg6% %arg7% %arg8% %arg9% %arg10% %arg11% %arg12%
GOTO END

:LOADSERVER
%LAUNCHER% %INQCOMMONARGS% com.inqwell.any.tools.Inqs %arg1% %arg2% %arg3% %arg4% %arg5% %arg6% %arg7% %arg8% %arg9% %arg10% %arg11% %arg12%
GOTO END

:END
rem ---- Restore old CLASSPATH
set CLASSPATH=%OLD_CLASSPATH%
set arg1=
set arg2=
set arg3=
set arg4=
set arg5=
set arg6=
set arg7=
set arg8=
set arg9=
set arg10=
set arg11=
set arg12=
set mode=
