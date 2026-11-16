@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.12.1
set MAVEN_CMD=C:\Users\sauak\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd

echo Starting Smart Employee Management & Biometric Attendance Backend...
cd /d "%~dp0"
"%MAVEN_CMD%" spring-boot:run -o
