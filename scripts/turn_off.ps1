$adb = "C:\Users\ddudu\AppData\Local\Microsoft\WinGet\Packages\Google.PlatformTools_Microsoft.Winget.Source_8wekyb3d8bbwe\platform-tools\adb.exe"
& $adb connect 192.168.101.187:5555 | Out-Null
& $adb -s 192.168.101.187:5555 shell "input keyevent 223"
Write-Output "Projetor desligado (Sleep mode ativado)."
