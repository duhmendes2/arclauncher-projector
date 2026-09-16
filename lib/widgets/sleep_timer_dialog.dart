import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/sleep_timer_service.dart';

class SleepTimerDialog extends StatelessWidget {
  const SleepTimerDialog({Key? key}) : super(key: key);

  static const List<int> _durations = [15, 30, 45, 60, 90, 120];

  @override
  Widget build(BuildContext context) {
    final timerService = context.watch<SleepTimerService>();

    return Dialog(
      backgroundColor: const Color(0xFF1E1E1E),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        width: 400,
        padding: const EdgeInsets.all(24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.bedtime_outlined, color: Colors.amberAccent, size: 28),
                const SizedBox(width: 12),
                const Text(
                  'Sleep Timer (Desligamento)',
                  style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              timerService.isActive
                  ? 'Timer ativo: ${timerService.formattedRemaining} restantes'
                  : 'Selecione em quanto tempo o projetor deve entrar em repouso:',
              style: const TextStyle(fontSize: 14, color: Colors.white70),
            ),
            const SizedBox(height: 20),
            Wrap(
              spacing: 12,
              runSpacing: 12,
              children: [
                ..._durations.map((mins) {
                  final isCurrent = timerService.isActive && timerService.initialMinutes == mins;
                  return ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: isCurrent ? Theme.of(context).colorScheme.primary : const Color(0xFF2A2A2A),
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    onPressed: () {
                      context.read<SleepTimerService>().setTimer(mins);
                      Navigator.of(context).pop();
                    },
                    child: Text('$mins min', style: const TextStyle(fontSize: 15, fontWeight: FontWeight.w600)),
                  );
                }),
                if (timerService.isActive)
                  ElevatedButton(
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.redAccent.withOpacity(0.8),
                      foregroundColor: Colors.white,
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
                      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                    ),
                    onPressed: () {
                      context.read<SleepTimerService>().cancelTimer();
                      Navigator.of(context).pop();
                    },
                    child: const Text('Desativar', style: TextStyle(fontSize: 15, fontWeight: FontWeight.bold)),
                  ),
              ],
            ),
            const SizedBox(height: 16),
            Align(
              alignment: Alignment.centerRight,
              child: TextButton(
                onPressed: () => Navigator.of(context).pop(),
                child: const Text('Fechar', style: TextStyle(color: Colors.white54)),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
