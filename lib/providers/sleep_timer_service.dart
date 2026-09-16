import 'dart:async';
import 'package:flutter/foundation.dart';
import '../flauncher_channel.dart';

class SleepTimerService extends ChangeNotifier {
  final FLauncherChannel _channel;
  Timer? _timer;
  int _remainingSeconds = 0;
  int _initialMinutes = 0;

  SleepTimerService(this._channel);

  bool get isActive => _remainingSeconds > 0;
  int get remainingSeconds => _remainingSeconds;
  int get initialMinutes => _initialMinutes;

  String get formattedRemaining {
    if (!isActive) return '';
    final minutes = _remainingSeconds ~/ 60;
    final seconds = _remainingSeconds % 60;
    if (minutes > 0) {
      return '${minutes}m';
    } else {
      return '${seconds}s';
    }
  }

  void setTimer(int minutes) {
    _timer?.cancel();
    if (minutes <= 0) {
      _remainingSeconds = 0;
      _initialMinutes = 0;
      notifyListeners();
      return;
    }

    _initialMinutes = minutes;
    _remainingSeconds = minutes * 60;
    notifyListeners();

    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      if (_remainingSeconds > 1) {
        _remainingSeconds--;
        notifyListeners();
      } else {
        _remainingSeconds = 0;
        _initialMinutes = 0;
        timer.cancel();
        notifyListeners();
        _triggerSleep();
      }
    });
  }

  void cancelTimer() {
    setTimer(0);
  }

  Future<void> _triggerSleep() async {
    try {
      await _channel.startAmbientMode();
    } catch (_) {}
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }
}
