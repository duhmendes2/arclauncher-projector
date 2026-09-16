import 'package:flutter/material.dart';
import 'package:flauncher/flauncher_channel.dart';
import 'package:flauncher/widgets/movie_wallpapers_dialog.dart';
import 'focusable_settings_tile.dart';

class ProjectorSettingsPage extends StatelessWidget {
  static const String routeName = "projector_settings_panel";

  const ProjectorSettingsPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        const Text(
          'Projetor & Controle Remoto',
          style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
        ),
        const Divider(),
        Expanded(
          child: SingleChildScrollView(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Text(
                    'CONTROLE REMOTO & BOTÕES',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.primary,
                      letterSpacing: 1.2,
                    ),
                  ),
                ),
                FocusableSettingsTile(
                  autofocus: true,
                  leading: const Icon(Icons.accessibility_new_rounded, color: Colors.amberAccent),
                  title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        'Ativar Retorno ao Arc (Acessibilidade)',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Clique aqui para abrir o menu do Android e ligar o serviço "Arc Launcher Home Override". Impede que o botão Home caia no Whale TV.',
                        style: TextStyle(fontSize: 10, color: Colors.white70),
                      ),
                    ],
                  ),
                  onPressed: () => FLauncherChannel().openAccessibilitySettings(),
                ),
                FocusableSettingsTile(
                  leading: const Icon(Icons.shield_outlined, color: Colors.greenAccent),
                  title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        'Trava do Botão Voltar (Back)',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Ativado: Pressionar Voltar na tela inicial nunca sai para o launcher padrão.',
                        style: TextStyle(fontSize: 10, color: Colors.white70),
                      ),
                    ],
                  ),
                  onPressed: () {},
                ),
                const Divider(),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Text(
                    'HARDWARE DO PROJETOR',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.primary,
                      letterSpacing: 1.2,
                    ),
                  ),
                ),
                FocusableSettingsTile(
                  leading: const Icon(Icons.aspect_ratio_rounded, color: Colors.lightBlueAccent),
                  title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        'Calibragem & Keystone',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Abre o menu nativo de correção trapezoidal, foco e alinhamento.',
                        style: TextStyle(fontSize: 10, color: Colors.white70),
                      ),
                    ],
                  ),
                  onPressed: () => FLauncherChannel().openProjectorSettings(),
                ),
                FocusableSettingsTile(
                  leading: const Icon(Icons.settings_input_hdmi_rounded, color: Colors.orangeAccent),
                  title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        'Entradas de Vídeo (HDMI)',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Alternar para conexões HDMI, AV ou entradas externas.',
                        style: TextStyle(fontSize: 10, color: Colors.white70),
                      ),
                    ],
                  ),
                  onPressed: () => FLauncherChannel().openTvInputs(),
                ),
                FocusableSettingsTile(
                  leading: const Icon(Icons.bluetooth_audio_rounded, color: Colors.cyanAccent),
                  title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        'Dispositivos Bluetooth',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Conectar soundbars, caixas de som e fones sem fio.',
                        style: TextStyle(fontSize: 10, color: Colors.white70),
                      ),
                    ],
                  ),
                  onPressed: () => FLauncherChannel().openBluetoothSettings(),
                ),
                const Divider(),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                  child: Text(
                    'CINEMA & PERSONALIZAÇÃO',
                    style: TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: Theme.of(context).colorScheme.primary,
                      letterSpacing: 1.2,
                    ),
                  ),
                ),
                FocusableSettingsTile(
                  leading: const Icon(Icons.movie_filter_rounded, color: Colors.amberAccent),
                  title: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: const [
                      Text(
                        'Papéis de Parede de Cinema',
                        style: TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                      ),
                      SizedBox(height: 2),
                      Text(
                        'Galeria com posters clássicos em 1080p (Interestelar, Blade Runner, Matrix, etc.).',
                        style: TextStyle(fontSize: 10, color: Colors.white70),
                      ),
                    ],
                  ),
                  onPressed: () => showDialog(
                    context: context,
                    builder: (_) => const MovieWallpapersDialog(),
                  ),
                ),
              ],
            ),
          ),
        ),
      ],
    );
  }
}
