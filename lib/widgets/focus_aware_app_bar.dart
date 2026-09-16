import 'package:flauncher/widgets/settings/settings_panel.dart';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

import '../flauncher_channel.dart';
import '../providers/launcher_state.dart';
import '../providers/settings_service.dart';
import '../providers/sleep_timer_service.dart';
import 'daily_wifi_usage_widget.dart';
import 'date_time_widget.dart';
import 'movie_wallpapers_dialog.dart';
import 'network_widget.dart';
import 'projector_menu_dialog.dart';
import 'sleep_timer_dialog.dart';
import 'weather_widget.dart';

class FocusAwareAppBar extends StatefulWidget implements PreferredSizeWidget
{
  const FocusAwareAppBar({Key? key}) : super(key: key);

  @override
  State<StatefulWidget> createState() {
    return FocusAwareAppBarState();
  }

  @override
  Size get preferredSize => Size.fromHeight(kToolbarHeight);
}

class FocusAwareAppBarState extends State<FocusAwareAppBar>
{
  bool focused = false;
  late FocusNode _settingsFocusNode;

  @override
  void initState() {
    super.initState();
    _settingsFocusNode = FocusNode();
  }

  @override
  void dispose() {
    _settingsFocusNode.dispose();
    super.dispose();
  }

  void focusSettings() {
    _settingsFocusNode.requestFocus();
  }

  @override
  Widget build(BuildContext context) {
    return Selector<SettingsService, bool>(
      selector: (_, settings) => settings.autoHideAppBarEnabled,
      builder: (context, autoHide, widget) {
        if (autoHide) {
          return Focus(
            canRequestFocus: false,
            child: AnimatedContainer(
              curve: Curves.decelerate,
              duration: Duration(milliseconds: 150),
              height: focused ? kToolbarHeight : 0,
              child: widget!
            ),
            onFocusChange: (hasFocus) {
              if (hasFocus) {
                context.read<LauncherState>().setAppGridFocused(false);
              }
              this.setState(() {
                focused = hasFocus;
              });
            }
          );
        }

        return widget!;
      },
      child: RepaintBoundary(
        child: AppBar(
          elevation: 0,
          scrolledUnderElevation: 0,
          backgroundColor: Colors.transparent,
          // Left side: Logo, Settings, Projector Controls, Network indicator, WiFi usage
          title: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              // Custom DUH! Film & Photo Logo
              Padding(
                padding: const EdgeInsets.only(right: 14),
                child: Image.asset(
                  'assets/duh_logo.png',
                  height: 38,
                  fit: BoxFit.contain,
                ),
              ),
              // Settings button
              _FocusableIconButton(
                icon: Icons.settings_outlined,
                label: 'Ajustes',
                focusNode: _settingsFocusNode,
                onPressed: () => showDialog(context: context, builder: (_) => const SettingsPanel()),
              ),
              const SizedBox(width: 8),
              // Movie Wallpapers (Cinema)
              _FocusableIconButton(
                icon: Icons.movie_filter_rounded,
                label: 'Cinema',
                color: Colors.amberAccent,
                onPressed: () => showDialog(
                  context: context,
                  builder: (_) => const MovieWallpapersDialog(),
                ),
              ),
              const SizedBox(width: 8),
              // Central do Projetor (Keystone, HDMI, Home, Boot)
              _FocusableIconButton(
                icon: Icons.tv_rounded,
                label: 'Projetor',
                color: Colors.lightGreenAccent,
                onPressed: () => showDialog(
                  context: context,
                  builder: (_) => const ProjectorMenuDialog(),
                ),
              ),
              const SizedBox(width: 8),
              // Sleep Timer button with live remaining time badge
              Consumer<SleepTimerService>(
                builder: (context, timer, _) => _FocusableIconButton(
                  icon: timer.isActive ? Icons.bedtime_rounded : Icons.bedtime_outlined,
                  color: timer.isActive ? Colors.amberAccent : null,
                  label: timer.isActive ? timer.formattedRemaining : null,
                  onPressed: () => showDialog(
                    context: context,
                    builder: (_) => const SleepTimerDialog(),
                  ),
                ),
              ),
              const SizedBox(width: 14),
              // Network indicator (conditionally shown)
              Selector<SettingsService, bool>(
                selector: (_, settings) => settings.showNetworkIndicatorInStatusBar,
                builder: (context, showNetwork, _) => showNetwork
                  ? Padding(
                      padding: const EdgeInsets.only(right: 12),
                      child: _FocusableNetworkWidget(),
                    )
                  : const SizedBox.shrink(),
              ),
              // WiFi usage widget
              Selector<SettingsService, bool>(
                selector: (_, settings) => settings.showWifiWidgetInStatusBar,
                builder: (context, showWifi, _) => showWifi
                  ? const DailyWifiUsageWidget()
                  : const SizedBox.shrink(),
              ),
            ],
          ),
          // Right side: Weather and Date/Time
          actions: [
            Padding(
              padding: const EdgeInsets.only(left: 16, right: 32),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  const WeatherWidget(),
                  const SizedBox(width: 16),
                  Selector<SettingsService,
                      ({
                        bool showDateInStatusBar,
                        bool showTimeInStatusBar,
                        String dateFormat,
                        String timeFormat })>(
                    selector: (context, service) => (
                    showDateInStatusBar: service.showDateInStatusBar,
                    showTimeInStatusBar: service.showTimeInStatusBar,
                    dateFormat: service.dateFormat,
                    timeFormat: service.timeFormat),
                    builder: (context, dateTimeSettings, _) {
                      // Define standard text style
                      const textStyle = TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w400,
                        color: Colors.white,
                        shadows: [
                          Shadow(color: Colors.black54, offset: Offset(0, 2), blurRadius: 4)
                        ],
                      );

                      return Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          // Date
                          if (dateTimeSettings.showDateInStatusBar)
                            DateTimeWidget(
                              dateTimeSettings.dateFormat,
                              key: const Key("statusbar_date"),
                              updateInterval: const Duration(minutes: 1),
                              textStyle: textStyle,
                            ),
                          
                          if (dateTimeSettings.showDateInStatusBar && dateTimeSettings.showTimeInStatusBar)
                              const SizedBox(width: 16),

                          // Clock
                          if (dateTimeSettings.showTimeInStatusBar)
                            DateTimeWidget(
                              dateTimeSettings.timeFormat,
                              key: const Key("statusbar_clock"),
                              updateInterval: const Duration(minutes: 1),
                              textStyle: textStyle.copyWith(fontWeight: FontWeight.bold),
                            ),
                        ]
                      );
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

/// Reusable focusable icon button with consistent outline focus indicator
class _FocusableIconButton extends StatefulWidget {
  final IconData icon;
  final VoidCallback onPressed;
  final FocusNode? focusNode;
  final Color? color;
  final String? label;

  const _FocusableIconButton({
    required this.icon,
    required this.onPressed,
    this.focusNode,
    this.color,
    this.label,
  });

  @override
  State<_FocusableIconButton> createState() => _FocusableIconButtonState();
}

class _FocusableIconButtonState extends State<_FocusableIconButton> {
  bool _focused = false;

  @override
  Widget build(BuildContext context) {
    return Actions(
      actions: <Type, Action<Intent>>{
        ActivateIntent: CallbackAction<ActivateIntent>(onInvoke: (_) => widget.onPressed()),
        ButtonActivateIntent: CallbackAction<ButtonActivateIntent>(onInvoke: (_) => widget.onPressed()),
      },
      child: Focus(
        focusNode: widget.focusNode,
        onFocusChange: (hasFocus) {
          if (hasFocus) {
            context.read<LauncherState>().setAppGridFocused(false);
          }
          setState(() => _focused = hasFocus);
        },
        child: InkWell(
          onTap: widget.onPressed,
          borderRadius: BorderRadius.circular(8),
          child: Container(
            padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 4),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(8),
              border: _focused
                ? Border.all(color: Theme.of(context).colorScheme.primary, width: 2)
                : null,
              boxShadow: _focused
                ? const [BoxShadow(color: Colors.black54, blurRadius: 8, spreadRadius: 1)]
                : null,
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  widget.icon,
                  color: widget.color,
                  shadows: const [
                    Shadow(color: Colors.black54, blurRadius: 8, offset: Offset(0, 2))
                  ],
                ),
                if (widget.label != null && widget.label!.isNotEmpty) ...[
                  const SizedBox(width: 4),
                  Text(
                    widget.label!,
                    style: TextStyle(
                      fontSize: 13,
                      fontWeight: FontWeight.bold,
                      color: widget.color ?? Colors.white,
                    ),
                  ),
                ],
              ],
            ),
          ),
        ),
      ),
    );
  }
}

/// Network widget with consistent focus indicator
class _FocusableNetworkWidget extends StatefulWidget {
  @override
  State<_FocusableNetworkWidget> createState() => _FocusableNetworkWidgetState();
}

class _FocusableNetworkWidgetState extends State<_FocusableNetworkWidget> {
  bool _focused = false;

  @override
  Widget build(BuildContext context) {
    return Focus(
      onFocusChange: (hasFocus) {
        if (hasFocus) {
          context.read<LauncherState>().setAppGridFocused(false);
        }
        setState(() => _focused = hasFocus);
      },
      child: Container(
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(8),
          border: _focused
            ? Border.all(color: Theme.of(context).colorScheme.primary, width: 2)
            : null,
          boxShadow: _focused
            ? const [BoxShadow(color: Colors.black54, blurRadius: 8, spreadRadius: 1)]
            : null,
        ),
        child: const NetworkWidget(),
      ),
    );
  }
}