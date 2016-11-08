del /F /Q .\target\scala-2.12\*.jar
sbt assembly
move /Y .\target\scala-2.12\*.jar .\azure-c2d-receive.jar
