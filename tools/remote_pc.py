import sys
import subprocess
import threading
import tkinter as tk
from tkinter import ttk

ADB_PATH = r"C:\Users\ddudu\AppData\Local\Microsoft\WinGet\Packages\Google.PlatformTools_Microsoft.Winget.Source_8wekyb3d8bbwe\platform-tools\adb.exe"
PROJECTOR_IP = "192.168.101.187:5555"

def send_adb_async(cmd):
    def _run():
        full_cmd = f'"{ADB_PATH}" -s {PROJECTOR_IP} {cmd}'
        subprocess.run(full_cmd, shell=True, capture_output=True)
    threading.Thread(target=_run, daemon=True).start()

def key(code):
    send_adb_async(f"shell input keyevent {code}")

def launch_app(pkg):
    send_adb_async(f"shell monkey -p {pkg} -c android.intent.category.LAUNCHER 1")

class ProjectorRemoteApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title("Controle • Projetor")
        self.geometry("280x620")
        self.resizable(False, False)
        self.configure(bg="#12101b")
        self.attributes("-topmost", True)
        
        # Connect to projector at start
        threading.Thread(target=lambda: subprocess.run(f'"{ADB_PATH}" connect {PROJECTOR_IP}', shell=True, capture_output=True), daemon=True).start()

        self.create_widgets()
        self.bind_keys()

    def create_widgets(self):
        # Header
        header = tk.Frame(self, bg="#12101b")
        header.pack(fill="x", padx=15, pady=(15, 8))
        
        title_label = tk.Label(header, text="DUH MENDES", font=("Segoe UI", 10, "bold"), fg="#a78bfa", bg="#12101b")
        title_label.pack()
        sub_label = tk.Label(header, text="Projetor Inteligente (Wi-Fi)", font=("Segoe UI", 8), fg="#6b7280", bg="#12101b")
        sub_label.pack()

        # Top Power & Sleep
        top_bar = tk.Frame(self, bg="#12101b")
        top_bar.pack(fill="x", padx=16, pady=6)
        
        btn_power = tk.Button(top_bar, text="⏻ Power", font=("Segoe UI", 9, "bold"), bg="#7f1d1d", fg="#fca5a5",
                              activebackground="#991b1b", activeforeground="#ffffff", relief="flat", cursor="hand2",
                              command=lambda: key(26))
        btn_power.pack(side="left", expand=True, fill="x", padx=(0, 4), ipady=5)
        
        btn_sleep = tk.Button(top_bar, text="⏱️ Sleep", font=("Segoe UI", 9, "bold"), bg="#581c87", fg="#d8b4fe",
                              activebackground="#6b21a8", activeforeground="#ffffff", relief="flat", cursor="hand2",
                              command=lambda: key(771))
        btn_sleep.pack(side="right", expand=True, fill="x", padx=(4, 0), ipady=5)

        # D-PAD Frame
        dpad_frame = tk.Frame(self, bg="#1a162b", bd=1, relief="solid")
        dpad_frame.pack(padx=20, pady=12)

        dpad_canvas = tk.Canvas(dpad_frame, width=190, height=190, bg="#181528", highlightthickness=0)
        dpad_canvas.pack()

        btn_up = tk.Button(dpad_canvas, text="▲", font=("Segoe UI", 12, "bold"), bg="#26213d", fg="#e2e8f0",
                           activebackground="#38bdf8", relief="flat", cursor="hand2", command=lambda: key(19))
        dpad_canvas.create_window(95, 28, window=btn_up, width=54, height=40)

        btn_left = tk.Button(dpad_canvas, text="◀", font=("Segoe UI", 12, "bold"), bg="#26213d", fg="#e2e8f0",
                             activebackground="#38bdf8", relief="flat", cursor="hand2", command=lambda: key(21))
        dpad_canvas.create_window(28, 95, window=btn_left, width=40, height=54)

        btn_ok = tk.Button(dpad_canvas, text="OK", font=("Segoe UI", 11, "bold"), bg="#3b82f6", fg="#ffffff",
                           activebackground="#60a5fa", relief="flat", cursor="hand2", command=lambda: key(23))
        dpad_canvas.create_window(95, 95, window=btn_ok, width=58, height=58)

        btn_right = tk.Button(dpad_canvas, text="▶", font=("Segoe UI", 12, "bold"), bg="#26213d", fg="#e2e8f0",
                              activebackground="#38bdf8", relief="flat", cursor="hand2", command=lambda: key(22))
        dpad_canvas.create_window(162, 95, window=btn_right, width=40, height=54)

        btn_down = tk.Button(dpad_canvas, text="▼", font=("Segoe UI", 12, "bold"), bg="#26213d", fg="#e2e8f0",
                             activebackground="#38bdf8", relief="flat", cursor="hand2", command=lambda: key(20))
        dpad_canvas.create_window(95, 162, window=btn_down, width=54, height=40)

        # Nav Row (Back, Home, Menu)
        nav_frame = tk.Frame(self, bg="#12101b")
        nav_frame.pack(fill="x", padx=16, pady=6)

        btn_back = tk.Button(nav_frame, text="↩ Voltar", font=("Segoe UI", 9), bg="#26213d", fg="#cbd5e1",
                             activebackground="#332d52", relief="flat", cursor="hand2", command=lambda: key(4))
        btn_back.pack(side="left", expand=True, fill="x", padx=2, ipady=4)

        btn_home = tk.Button(nav_frame, text="🏠 Início", font=("Segoe UI", 9, "bold"), bg="#26213d", fg="#f1f5f9",
                             activebackground="#332d52", relief="flat", cursor="hand2", command=lambda: key(3))
        btn_home.pack(side="left", expand=True, fill="x", padx=2, ipady=4)

        btn_menu = tk.Button(nav_frame, text="⚙️ Menu", font=("Segoe UI", 9), bg="#26213d", fg="#cbd5e1",
                             activebackground="#332d52", relief="flat", cursor="hand2", command=lambda: key(82))
        btn_menu.pack(side="left", expand=True, fill="x", padx=2, ipady=4)

        # Volume Row
        vol_frame = tk.Frame(self, bg="#12101b")
        vol_frame.pack(fill="x", padx=16, pady=6)

        btn_voldown = tk.Button(vol_frame, text="🔉 Vol -", font=("Segoe UI", 9), bg="#1e1b2e", fg="#94a3b8",
                                activebackground="#2a2640", relief="flat", cursor="hand2", command=lambda: key(25))
        btn_voldown.pack(side="left", expand=True, fill="x", padx=2, ipady=4)

        btn_mute = tk.Button(vol_frame, text="🔇", font=("Segoe UI", 9), bg="#1e1b2e", fg="#94a3b8",
                             activebackground="#2a2640", relief="flat", cursor="hand2", command=lambda: key(164))
        btn_mute.pack(side="left", expand=False, padx=2, ipady=4, ipadx=6)

        btn_volup = tk.Button(vol_frame, text="🔊 Vol +", font=("Segoe UI", 9), bg="#1e1b2e", fg="#94a3b8",
                              activebackground="#2a2640", relief="flat", cursor="hand2", command=lambda: key(24))
        btn_volup.pack(side="left", expand=True, fill="x", padx=2, ipady=4)

        # Quick Streaming Apps Grid (2x2)
        apps_frame = tk.Frame(self, bg="#12101b")
        apps_frame.pack(fill="x", padx=16, pady=(10, 10))

        # Row 1: YouCine & Stremio
        app_r1 = tk.Frame(apps_frame, bg="#12101b")
        app_r1.pack(fill="x", pady=2)
        
        btn_yc = tk.Button(app_r1, text="🎬 YouCine", font=("Segoe UI", 9, "bold"), bg="#450a0a", fg="#fca5a5",
                           activebackground="#7f1d1d", relief="flat", cursor="hand2",
                           command=lambda: launch_app("com.world.youcinetv"))
        btn_yc.pack(side="left", expand=True, fill="x", padx=2, ipady=5)

        btn_stremio = tk.Button(app_r1, text="🍿 Stremio", font=("Segoe UI", 9, "bold"), bg="#3b0764", fg="#d8b4fe",
                                activebackground="#581c87", relief="flat", cursor="hand2",
                                command=lambda: launch_app("com.stremio.one"))
        btn_stremio.pack(side="right", expand=True, fill="x", padx=2, ipady=5)

        # Row 2: Pluto TV & YouTube
        app_r2 = tk.Frame(apps_frame, bg="#12101b")
        app_r2.pack(fill="x", pady=2)

        btn_pluto = tk.Button(app_r2, text="📺 Pluto TV", font=("Segoe UI", 9, "bold"), bg="#1e1b4b", fg="#a5b4fc",
                              activebackground="#312e81", relief="flat", cursor="hand2",
                              command=lambda: launch_app("tv.pluto.android"))
        btn_pluto.pack(side="left", expand=True, fill="x", padx=2, ipady=5)

        btn_yt = tk.Button(app_r2, text="▶️ YouTube", font=("Segoe UI", 9, "bold"), bg="#450a0a", fg="#f87171",
                           activebackground="#7f1d1d", relief="flat", cursor="hand2",
                           command=lambda: launch_app("com.WhaleTV.whaleos.youtube.tv"))
        btn_yt.pack(side="right", expand=True, fill="x", padx=2, ipady=5)

        # Status footer
        lbl_hint = tk.Label(self, text="Dica: Use as setas do teclado do PC e Enter!", font=("Segoe UI", 7), fg="#4b5563", bg="#12101b")
        lbl_hint.pack(side="bottom", pady=6)

    def bind_keys(self):
        self.bind("<Up>", lambda e: key(19))
        self.bind("<Down>", lambda e: key(20))
        self.bind("<Left>", lambda e: key(21))
        self.bind("<Right>", lambda e: key(22))
        self.bind("<Return>", lambda e: key(23))
        self.bind("<space>", lambda e: key(23))
        self.bind("<BackSpace>", lambda e: key(4))
        self.bind("<Escape>", lambda e: key(4))
        self.bind("<Home>", lambda e: key(3))
        self.bind("<h>", lambda e: key(3))
        self.bind("<m>", lambda e: key(82))
        self.bind("<plus>", lambda e: key(24))
        self.bind("<minus>", lambda e: key(25))

if __name__ == "__main__":
    app = ProjectorRemoteApp()
    app.mainloop()
