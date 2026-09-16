import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../flauncher_channel.dart';

class ProjectorMenuDialog extends StatelessWidget {
  const ProjectorMenuDialog({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: const Color(0xFF14171C),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        width: 820,
        height: 500,
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.tv_rounded, color: Colors.amberAccent, size: 30),
                const SizedBox(width: 14),
                const Text(
                  'Central do Projetor & Controle Remoto',
                  style: TextStyle(
                    fontSize: 22,
                    fontWeight: FontWeight.bold,
                    color: Colors.white,
                  ),
                ),
                const Spacer(),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.white70),
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
            const SizedBox(height: 6),
            const Text(
              'Ajuste o comportamento do controle remoto, inicialização automática e imagem:',
              style: TextStyle(fontSize: 13, color: Colors.white60),
            ),
            const SizedBox(height: 18),
            Expanded(
              child: ListView(
                children: [
                  _ProjectorActionTile(
                    autofocus: true,
                    icon: Icons.home_repair_service_rounded,
                    iconColor: Colors.amberAccent,
                    title: '1. Controle do Botão HOME (Bloquear Whale TV / Zeasn)',
                    subtitle:
                        'Ative o "Arc Launcher - Controle do Botão Home" na tela de Acessibilidade. Toda vez que você apertar Home, o projetor voltará para cá.',
                    actionLabel: 'Abrir Acessibilidade',
                    onTap: () {
                      FLauncherChannel().openAccessibilitySettings();
                    },
                  ),
                  const SizedBox(height: 12),
                  _ProjectorActionTile(
                    icon: Icons.rocket_launch_rounded,
                    iconColor: Colors.lightGreenAccent,
                    title: '2. Iniciar com o Projetor (Launcher Padrão)',
                    subtitle:
                        'O Arc Launcher já inicia automaticamente no boot. Clique aqui para defini-lo como aplicativo inicial padrão do sistema Android.',
                    actionLabel: 'Definir Padrão',
                    onTap: () {
                      FLauncherChannel().openSettings();
                    },
                  ),
                  const SizedBox(height: 12),
                  _ProjectorActionTile(
                    icon: Icons.aspect_ratio_rounded,
                    iconColor: Colors.cyanAccent,
                    title: '3. Calibração de Imagem (Keystone / Foco / Zoom)',
                    subtitle:
                        'Acessa as opções de geometria, correção trapezoidal de 4 pontos e configurações de tela do projetor.',
                    actionLabel: 'Ajustar Imagem',
                    onTap: () {
                      FLauncherChannel().openProjectorSettings();
                    },
                  ),
                  const SizedBox(height: 12),
                  _ProjectorActionTile(
                    icon: Icons.input_rounded,
                    iconColor: Colors.orangeAccent,
                    title: '4. Entrada HDMI / Vídeo Externo',
                    subtitle:
                        'Alterna diretamente para a porta HDMI conectada (notebook, videogame, TV box ou receptor).',
                    actionLabel: 'Abrir HDMI',
                    onTap: () {
                      FLauncherChannel().openTvInputs();
                    },
                  ),
                  const SizedBox(height: 12),
                  _ProjectorActionTile(
                    icon: Icons.bluetooth_rounded,
                    iconColor: Colors.blueAccent,
                    title: '5. Caixas de Som & Bluetooth',
                    subtitle:
                        'Conecte soundbars, fones de ouvido sem fio ou novos controles remotos ao projetor.',
                    actionLabel: 'Abrir Bluetooth',
                    onTap: () {
                      FLauncherChannel().openBluetoothSettings();
                    },
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ProjectorActionTile extends StatefulWidget {
  final bool autofocus;
  final IconData icon;
  final Color iconColor;
  final String title;
  final String subtitle;
  final String actionLabel;
  final VoidCallback onTap;

  const _ProjectorActionTile({
    Key? key,
    this.autofocus = false,
    required this.icon,
    required this.iconColor,
    required this.title,
    required this.subtitle,
    required this.actionLabel,
    required this.onTap,
  }) : super(key: key);

  @override
  State<_ProjectorActionTile> createState() => _ProjectorActionTileState();
}

class _ProjectorActionTileState extends State<_ProjectorActionTile> {
  bool _focused = false;

  @override
  Widget build(BuildContext context) {
    return Focus(
      autofocus: widget.autofocus,
      onFocusChange: (has) => setState(() => _focused = has),
      onKeyEvent: (node, event) {
        if (event is KeyDownEvent &&
            (event.logicalKey == LogicalKeyboardKey.select ||
                event.logicalKey == LogicalKeyboardKey.enter ||
                event.logicalKey == LogicalKeyboardKey.space)) {
          widget.onTap();
          return KeyEventResult.handled;
        }
        return KeyEventResult.ignored;
      },
      child: GestureDetector(
        onTap: widget.onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 140),
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
          decoration: BoxDecoration(
            color: _focused ? const Color(0xFF262C36) : const Color(0xFF1A1D23),
            borderRadius: BorderRadius.circular(10),
            border: Border.all(
              color: _focused ? Colors.amberAccent : Colors.white12,
              width: _focused ? 2.5 : 1.0,
            ),
            boxShadow: _focused
                ? [
                    BoxShadow(
                      color: Colors.amberAccent.withOpacity(0.25),
                      blurRadius: 10,
                      spreadRadius: 1,
                    ),
                  ]
                : [],
          ),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: widget.iconColor.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(widget.icon, color: widget.iconColor, size: 24),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      widget.title,
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.bold,
                        color: _focused ? Colors.amberAccent : Colors.white,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      widget.subtitle,
                      style: const TextStyle(fontSize: 12, color: Colors.white70, height: 1.3),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 14),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                decoration: BoxDecoration(
                  color: _focused ? Colors.amberAccent : Colors.white10,
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(
                      widget.actionLabel,
                      style: TextStyle(
                        fontSize: 12,
                        fontWeight: FontWeight.bold,
                        color: _focused ? Colors.black : Colors.white,
                      ),
                    ),
                    const SizedBox(width: 4),
                    Icon(
                      Icons.arrow_forward_rounded,
                      size: 14,
                      color: _focused ? Colors.black : Colors.white70,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
