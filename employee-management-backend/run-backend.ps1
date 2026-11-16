param(
    [string]$Profile = "dev"
)

$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.12.1"
$mvn = "C:\Users\sauak\.m2\wrapper\dists\apache-maven-3.9.16-bin\5grr65jo27hi51sujmtcldfovl\apache-maven-3.9.16\bin\mvn.cmd"

Write-Host "Starting Smart Employee Management Backend (Profile: $Profile)..." -ForegroundColor Cyan
if ($Profile -eq "mysql") {
    & $mvn spring-boot:run -o "-Dspring-boot.run.arguments=--spring.profiles.active=mysql"
} else {
    & $mvn spring-boot:run -o
}
