@REM Maven Wrapper script for Windows
@REM Licensed to the Apache Software Foundation (ASF)

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __ 

@SET MAVEN_PROJECTBASEDIR=%~dp0

@IF "x%JAVA_HOME%"=="x" (
  SET JAVACMD=java.exe
) ELSE (
  SET JAVACMD="%JAVA_HOME%\bin\java.exe"
)

@SET MAVEN_WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.jar"
@SET MAVEN_WRAPPER_PROPERTIES="%MAVEN_PROJECTBASEDIR%.mvn\wrapper\maven-wrapper.properties"

%JAVACMD% ^
  -classpath %MAVEN_WRAPPER_JAR% ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*
