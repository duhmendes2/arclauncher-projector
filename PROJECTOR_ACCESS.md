# Dados de Acesso & Configuração do Projetor

Documento de consulta rápida para manutenção, compilação e deploy do launcher e sincronização com o projetor.

---

### 🌐 Conexão de Rede & Dispositivos
* **IP do Projetor na Rede Wi-Fi:** `192.168.101.187`
* **Sub-rede Local:** `192.168.101.x`
* **Porta ADB Padrão:** `5555`

---

### 🔄 Sincronização via Syncthing (Projetor & PC)
* **Interface Web do Syncthing (PC):** `http://127.0.0.1:8384/`
* **Pasta Compartilhada no PC:** `D:\Projetor`
* **ID do Dispositivo (PC - duh_notebook):** `LFU4OOB-YMVMAVK-O45LQZU-FPI2ZUF-NPQDZ2N-QSPXNPT-DOHM6CK-2P7QCQP`
* **ID do Dispositivo (Projetor - Syncthing-Fork):** `K3EXSUU-Z4PD7IG-RZCJRYZ-2DL3KPB-Y5IHB24-IRR6IEP-NUCQFTZ-2FDREAJ`
* **ID da Pasta no Syncthing:** `gpghi-pnngr` (Label: `Projetor`)
* **Executável Syncthing:** `C:\Users\ddudu\AppData\Local\Microsoft\WinGet\Packages\Syncthing.Syncthing_Microsoft.Winget.Source_8wekyb3d8bbwe\syncthing-windows-amd64-v2.1.5\syncthing.exe`
* **Comando para Iniciar em Segundo Plano:**
  ```powershell
  & "C:\Users\ddudu\AppData\Local\Microsoft\WinGet\Packages\Syncthing.Syncthing_Microsoft.Winget.Source_8wekyb3d8bbwe\syncthing-windows-amd64-v2.1.5\syncthing.exe" serve --no-browser --no-upgrade
  ```

### 🚀 Deploy e Instalação Direta via ADB (Sem Syncthing)
Com o ADB ativado no Bettdow AC1066, a sincronização manual e o Syncthing tornaram-se desnecessários (economizando memória RAM no projetor):
* **Transferir para a memória interna `/sdcard/Projetor/`:**
  ```powershell
  adb -s 192.168.101.187:5555 push "D:\IA\MCP\arclauncher\release_apks\ArcLauncher_Projector_Native_v2.0.apk" "/sdcard/Projetor/ArcLauncher_Projector_Native_v2.1.2.apk"
  ```
* **Instalação Silenciosa Direta:**
  ```powershell
  adb -s 192.168.101.187:5555 push "D:\IA\MCP\arclauncher\release_apks\ArcLauncher_Projector_Native_v2.0.apk" "/data/local/tmp/update.apk"
  adb -s 192.168.101.187:5555 shell "pm install -r -d /data/local/tmp/update.apk"
  ```

---

### 📦 Repositório & Compilação do Launcher
* **Repositório GitHub:** [`https://github.com/duhmendes2/arclauncher-projector`](https://github.com/duhmendes2/arclauncher-projector)
* **Branch Principal Atual (Nativa v2.0):** `main`
* **Branch Legada (Flutter v1.1.5):** `flutter-legacy`
* **Nome do Pacote / Application ID:** `com.omeda.arc`
* **Versão Atual:** `2.1.2-projector` (VersionCode: `4204`)
* **Chave Criptográfica Permanente:**
  * Caminho: `app/upload-keystore.jks`
  * Senha da Keystore: `projectorpass`
  * Alias da Chave: `upload`
  * Senha da Chave: `projectorpass`
* **Pasta de Saída dos APKs Locais:** `D:\IA\MCP\arclauncher\release_apks\`
* **APK Atual de Alta Performance:** `ArcLauncher_Projector_Native_v2.1.2.apk` (~447 KB)

### ⚡ Controle Remoto de Energia (Ligar & Desligar via Rede)
Como configuramos a política `wifi_sleep_policy=2` e `stay_on_while_plugged_in=3`, a placa de rede do Bettdow AC1066 permanece ouvindo comandos mesmo com a lâmpada/projeção desligada.
* **Comando para Desligar (Sleep):**
  ```powershell
  adb -s 192.168.101.187:5555 shell "input keyevent 223"
  ```
  *(Ou execute: `D:\IA\MCP\arclauncher\scripts\turn_off.ps1`)*
* **Comando para Ligar (Wakeup):**
  ```powershell
  adb -s 192.168.101.187:5555 shell "input keyevent 224"
  ```
  *(Ou execute: `D:\IA\MCP\arclauncher\scripts\turn_on.ps1`)*

---

### 🛠️ Recursos de Hardware do Projetor Integrados no Código
* **🎯 Grade de Calibração de Foco & Keystone (`CalibrationActivity`):** Padrão de precisão em 16:9, marcas nos 4 cantos para keystone e miras de foco óptico para nitidez de lente.
* **⏱️ Sleep Timer (`SleepTimer`):** Desligamento/suspensão automática em 15, 30, 45, 60, 90 ou 120 minutos com monitoramento em tempo real.
* **📻 Modo Apenas Áudio / Tela Apagada (`ScreenOffActivity`):** Apaga o feixe de projeção e mantém Spotify/Bluetooth tocando em segundo plano.
* **🌙 Protetor de Tela Integrado (`ScreensaverActivity`):** Relógio minimalista flutuante anti-burnin e slideshow de pôsteres de cinema ativado por inatividade.
* **🕶️ Dimmer de Cinema (Filtro Escuro):** Filtro de tela por software (20%, 40%, 60%) para quartos escuros.
* **🚫 Gerenciador de Apps Ocultos (`AppHideActivity`):** Oculta aplicativos indesejados e bloatwares de fábrica da tela inicial.
* **📂 Seletor de Wallpapers da Memória e Pendrive:** Navega em diretórios locais e pendrives USB para aplicar papéis de parede.
* **Ajustes de Foco / Keystone:** Handlers com fallbacks para chips `com.whaletv.settings`, `com.zeasn.whale.settings`, `com.softwinner.tv.settings`, `com.mstar.tv.setting` e `com.android.tv.settings.display.DisplayActivity`.
* **Entradas de Vídeo (HDMI 1 / HDMI 2 / AV):** `com.mstar.tv.tvapp`, `com.android.tv` e `content://android.media.tv/passthrough`.
* **Captura do Botão Home & Auto-Start:** `BootReceiver` e `HomeKeyService` registrados para inicialização instantânea.
* **Telemetria na Tela Inicial:** CPU, RAM Livre e Armazenamento monitorados em tempo real (`show_stats: true`).
