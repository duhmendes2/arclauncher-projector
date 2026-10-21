import os
import subprocess
from http.server import HTTPServer, BaseHTTPRequestHandler
import json
import urllib.parse

ADB_PATH = r"C:\Users\ddudu\AppData\Local\Microsoft\WinGet\Packages\Google.PlatformTools_Microsoft.Winget.Source_8wekyb3d8bbwe\platform-tools\adb.exe"
PROJECTOR_IP = "192.168.101.187:5555"

HTML_CONTENT = """<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>Controle do Projetor • Duh Mendes</title>
    <style>
        * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
        body {
            margin: 0; padding: 20px 10px;
            background: radial-gradient(circle at 50% 10%, #1a162b, #09080f);
            color: #fff; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            display: flex; flex-direction: column; align-items: center; min-height: 100vh;
        }
        .header { text-align: center; margin-bottom: 24px; }
        .header h1 { margin: 0; font-size: 1.15rem; font-weight: 600; letter-spacing: 0.5px; color: #f1f1f1; }
        .header p { margin: 4px 0 0; font-size: 0.75rem; color: #a09cb0; }
        
        .remote-case {
            width: 100%; max-width: 320px;
            background: rgba(25, 22, 40, 0.7);
            border: 1px solid rgba(255, 255, 255, 0.08);
            border-radius: 36px; padding: 24px 18px;
            box-shadow: 0 20px 50px rgba(0,0,0,0.6), inset 0 1px 0 rgba(255,255,255,0.1);
            backdrop-filter: blur(20px);
            display: flex; flex-direction: column; gap: 20px; align-items: center;
        }
        
        .row { display: flex; justify-content: space-between; width: 100%; gap: 12px; }
        
        .btn {
            background: rgba(255, 255, 255, 0.06);
            border: 1px solid rgba(255, 255, 255, 0.08);
            border-radius: 18px; color: #fff; font-size: 0.85rem; font-weight: 500;
            cursor: pointer; display: flex; align-items: center; justify-content: center;
            transition: all 0.12s ease; outline: none; user-select: none;
            padding: 12px 14px; flex: 1;
        }
        .btn:active { background: rgba(255, 255, 255, 0.18); transform: scale(0.95); }
        .btn-power { background: rgba(239, 68, 68, 0.2); border-color: rgba(239, 68, 68, 0.4); color: #fca5a5; }
        .btn-sleep { background: rgba(147, 51, 234, 0.2); border-color: rgba(147, 51, 234, 0.4); color: #d8b4fe; }
        
        /* D-PAD */
        .dpad {
            position: relative; width: 200px; height: 200px;
            background: rgba(15, 13, 26, 0.85);
            border-radius: 50%; border: 1px solid rgba(255, 255, 255, 0.08);
            box-shadow: inset 0 2px 10px rgba(0,0,0,0.5);
            display: flex; align-items: center; justify-content: center; margin: 10px 0;
        }
        .dpad-btn {
            position: absolute; background: none; border: none; color: #e2e8f0;
            font-size: 1.3rem; cursor: pointer; display: flex; align-items: center; justify-content: center;
            width: 60px; height: 60px; outline: none;
        }
        .dpad-btn:active { color: #38bdf8; transform: scale(1.15); }
        .dpad-up { top: 6px; left: 70px; }
        .dpad-down { bottom: 6px; left: 70px; }
        .dpad-left { left: 6px; top: 70px; }
        .dpad-right { right: 6px; top: 70px; }
        .dpad-ok {
            position: absolute; width: 68px; height: 68px; border-radius: 50%;
            background: linear-gradient(135deg, rgba(255,255,255,0.12), rgba(255,255,255,0.03));
            border: 1px solid rgba(255, 255, 255, 0.15);
            color: #fff; font-weight: 700; font-size: 0.95rem; cursor: pointer;
            box-shadow: 0 4px 15px rgba(0,0,0,0.3); outline: none;
        }
        .dpad-ok:active { background: rgba(56, 189, 248, 0.3); border-color: #38bdf8; transform: scale(0.93); }
        
        .streaming-grid {
            display: grid; grid-template-columns: 1fr 1fr; gap: 10px; width: 100%;
        }
        .btn-stream {
            font-size: 0.78rem; font-weight: 600; padding: 10px 8px; border-radius: 14px;
        }
        .btn-youcine { background: rgba(220, 38, 38, 0.2); border-color: rgba(220, 38, 38, 0.4); color: #fca5a5; }
        .btn-youtube { background: rgba(239, 68, 68, 0.2); border-color: rgba(239, 68, 68, 0.4); color: #f87171; }
        .btn-stremio { background: rgba(147, 51, 234, 0.2); border-color: rgba(147, 51, 234, 0.4); color: #c084fc; }
        .btn-store { background: rgba(14, 165, 233, 0.2); border-color: rgba(14, 165, 233, 0.4); color: #7dd3fc; }
    </style>
</head>
<body>
    <div class="header">
        <h1>PROJETOR DUH MENDES</h1>
        <p>Controle Remoto Wi-Fi (192.168.101.187)</p>
    </div>
    
    <div class="remote-case">
        <div class="row">
            <button class="btn btn-power" onclick="sendKey(26)">⏻ Liga/Desl</button>
            <button class="btn btn-sleep" onclick="sendKey(771)">⏱️ Sleep</button>
        </div>
        
        <div class="dpad">
            <button class="dpad-btn dpad-up" onclick="sendKey(19)">▲</button>
            <button class="dpad-btn dpad-left" onclick="sendKey(21)">◀</button>
            <button class="dpad-ok" onclick="sendKey(23)">OK</button>
            <button class="dpad-btn dpad-right" onclick="sendKey(22)">▶</button>
            <button class="dpad-btn dpad-down" onclick="sendKey(20)">▼</button>
        </div>
        
        <div class="row">
            <button class="btn" onclick="sendKey(4)">↩ Voltar</button>
            <button class="btn" onclick="sendKey(3)">🏠 Início</button>
            <button class="btn" onclick="sendKey(82)">⚙️ Menu</button>
        </div>
        
        <div class="row">
            <button class="btn" onclick="sendKey(25)">🔉 Vol -</button>
            <button class="btn" onclick="sendKey(164)">🔇 Mudo</button>
            <button class="btn" onclick="sendKey(24)">🔊 Vol +</button>
        </div>
        
        <div class="streaming-grid">
            <button class="btn btn-stream btn-youcine" onclick="launchApp('com.world.youcinetv')">🎬 YouCine</button>
            <button class="btn btn-stream btn-youtube" onclick="launchApp('com.WhaleTV.whaleos.youtube.tv')">▶️ YouTube</button>
            <button class="btn btn-stream btn-stremio" onclick="launchApp('com.stremio.one')">🍿 Stremio</button>
            <button class="btn btn-stream btn-store" onclick="launchApp('com.aurora.store.debug')">🏪 Play Store</button>
        </div>
    </div>

    <script>
        function sendKey(code) {
            navigator.vibrate && navigator.vibrate(25);
            fetch('/key?code=' + code);
        }
        function launchApp(pkg) {
            navigator.vibrate && navigator.vibrate(40);
            fetch('/app?pkg=' + encodeURIComponent(pkg));
        }
    </script>
</body>
</html>
"""

def run_adb(cmd):
    full_cmd = f'"{ADB_PATH}" -s {PROJECTOR_IP} {cmd}'
    subprocess.Popen(full_cmd, shell=True)

class RemoteHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        parsed = urllib.parse.urlparse(self.path)
        if parsed.path == "/" or parsed.path == "/index.html":
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.end_headers()
            self.wfile.write(HTML_CONTENT.encode("utf-8"))
        elif parsed.path == "/key":
            query = urllib.parse.parse_qs(parsed.query)
            code = query.get("code", ["0"])[0]
            run_adb(f"shell input keyevent {code}")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(b'{"status":"ok"}')
        elif parsed.path == "/app":
            query = urllib.parse.parse_qs(parsed.query)
            pkg = query.get("pkg", [""])[0]
            if pkg:
                run_adb(f"shell monkey -p {pkg} -c android.intent.category.LAUNCHER 1")
            self.send_response(200)
            self.send_header("Content-Type", "application/json")
            self.end_headers()
            self.wfile.write(b'{"status":"ok"}')
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        pass

if __name__ == "__main__":
    server = HTTPServer(("0.0.0.0", 8080), RemoteHandler)
    print("Controle Remoto Web rodando em http://0.0.0.0:8080")
    print("Acesse pelo celular no mesmo Wi-Fi: http://192.168.101.165:8080")
    server.serve_forever()
