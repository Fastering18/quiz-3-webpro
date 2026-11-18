$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$TomcatLib = if ($env:TOMCAT_LIB) { $env:TOMCAT_LIB } else { "D:\Program Files\xampp\tomcat\lib" }
$WebInfClasses = Join-Path $ProjectRoot "src\main\webapp\WEB-INF\classes"
$ResourcesDir = Join-Path $ProjectRoot "src\main\resources"
$JavaSrcDir = Join-Path $ProjectRoot "src\main\java"
$WebAppDir = Join-Path $ProjectRoot "src\main\webapp"
$TargetDir = Join-Path $ProjectRoot "target"

$ServletJar = Join-Path $TomcatLib "servlet-api.jar"
$JspJar = Join-Path $TomcatLib "jsp-api.jar"
if (!(Test-Path $ServletJar) -or !(Test-Path $JspJar)) {
    throw "Tomcat servlet-api.jar or jsp-api.jar not found at $TomcatLib. Set TOMCAT_LIB env var."
}

if (Test-Path $WebInfClasses) {
    Remove-Item -Recurse -Force $WebInfClasses
}
New-Item -ItemType Directory -Force -Path $WebInfClasses | Out-Null

$JavaFiles = Get-ChildItem -Recurse -Filter "*.java" $JavaSrcDir | ForEach-Object { $_.FullName }
$LibJars = (Get-ChildItem -Filter "*.jar" (Join-Path $WebAppDir "WEB-INF\lib") | ForEach-Object { $_.FullName }) -join ";"
$Classpath = "$ServletJar;$JspJar;$LibJars"

Write-Host "Compiling $($JavaFiles.Count) Java source files..."
& javac -encoding UTF-8 --release 17 -cp $Classpath -d $WebInfClasses $JavaFiles

if (Test-Path $ResourcesDir) {
    Copy-Item -Recurse -Force "$ResourcesDir\*" $WebInfClasses
}

New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
$WarPath = Join-Path $TargetDir "zstore.war"
Write-Host "Packaging WAR archive to $WarPath..."
& jar -cf $WarPath -C $WebAppDir .

Write-Host "Build completed successfully."

