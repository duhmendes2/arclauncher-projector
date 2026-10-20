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

---

### 📦 Repositório & Compilação do Launcher
* **Repositório GitHub:** [`https://github.com/duhmendes2/arclauncher-projector`](https://github.com/duhmendes2/arclauncher-projector)
* **Branch Principal Atual (Nativa v2.0):** `main`
* **Branch Legada (Flutter v1.1.5):** `flutter-legacy`
* **Nome do Pacote / Application ID:** `com.omeda.arc`
* **Versão Atual:** `2.0.0-projector` (VersionCode: `4200`)
* **Chave Criptográfica Permanente:**
  * Caminho: `app/upload-keystore.jks`
  * Senha da Keystore: `projectorpass`
  * Alias da Chave: `upload`
  * Senha da Chave: `projectorpass`
* **Pasta de Saída dos APKs Locais:** `D:\IA\MCP\arclauncher\release_apks\`
* **APK Atual de Alta Performance:** `ArcLauncher_Projector_Native_v2.0.apk` (~427 KB)

---

### 🛠️ Recursos de Hardware do Projetor Integrados no Código
* **Ajustes de Foco / Keystone:** Handlers com fallbacks para chips `com.whaletv.settings`, `com.zeasn.whale.settings`, `com.softwinner.tv.settings`, `com.mstar.tv.setting` e `com.android.tv.settings.display.DisplayActivity`.
* **Entradas de Vídeo (HDMI 1 / HDMI 2 / AV):** `com.mstar.tv.tvapp`, `com.android.tv` e `content://android.media.tv/passthrough`.
* **Captura do Botão Home & Auto-Start:** `BootReceiver` e `HomeKeyService` registrados para inicialização instantânea.
* **Telemetria na Tela Inicial:** CPU, RAM Livre e Armazenamento monitorados em tempo real (`show_stats: true`).
