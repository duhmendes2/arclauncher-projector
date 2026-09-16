import 'dart:async';
import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;

class WeatherWidget extends StatefulWidget {
  final TextStyle textStyle;

  const WeatherWidget({
    Key? key,
    this.textStyle = const TextStyle(
      fontSize: 20,
      fontWeight: FontWeight.w400,
      color: Colors.white,
      shadows: [
        Shadow(color: Colors.black54, offset: Offset(0, 2), blurRadius: 4)
      ],
    ),
  }) : super(key: key);

  @override
  State<WeatherWidget> createState() => _WeatherWidgetState();
}

class _WeatherWidgetState extends State<WeatherWidget> {
  int? _temperature;
  IconData? _weatherIcon;
  String? _cityName;
  Timer? _refreshTimer;

  @override
  void initState() {
    super.initState();
    _fetchWeather();
    _refreshTimer = Timer.periodic(const Duration(minutes: 30), (_) => _fetchWeather());
  }

  @override
  void dispose() {
    _refreshTimer?.cancel();
    super.dispose();
  }

  Future<void> _fetchWeather() async {
    try {
      // 1. Get approximate coordinates via free IP geolocation
      final geoResponse = await http
          .get(Uri.parse('http://ip-api.com/json'))
          .timeout(const Duration(seconds: 6));

      if (geoResponse.statusCode == 200) {
        final geoData = jsonDecode(geoResponse.body);
        final double lat = (geoData['lat'] as num).toDouble();
        final double lon = (geoData['lon'] as num).toDouble();
        final String city = geoData['city'] ?? '';

        // 2. Query Open-Meteo forecast API (free, no API key needed)
        final weatherUrl = Uri.parse(
            'https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current=temperature_2m,weather_code');
        final weatherResponse =
            await http.get(weatherUrl).timeout(const Duration(seconds: 6));

        if (weatherResponse.statusCode == 200) {
          final weatherData = jsonDecode(weatherResponse.body);
          final current = weatherData['current'];
          final double temp = (current['temperature_2m'] as num).toDouble();
          final int code = (current['weather_code'] as num).toInt();

          if (mounted) {
            setState(() {
              _temperature = temp.round();
              _weatherIcon = _getWeatherIcon(code);
              _cityName = city;
            });
          }
        }
      }
    } catch (_) {
      // Silently ignore network failures on TV startup
    }
  }

  IconData _getWeatherIcon(int code) {
    if (code == 0) return Icons.wb_sunny_rounded;
    if (code <= 3) return Icons.cloud_rounded;
    if (code == 45 || code == 48) return Icons.cloud_queue_rounded;
    if (code >= 51 && code <= 67) return Icons.water_drop_rounded;
    if (code >= 71 && code <= 77) return Icons.ac_unit_rounded;
    if (code >= 80 && code <= 82) return Icons.water_drop_rounded;
    if (code >= 95) return Icons.flash_on_rounded;
    return Icons.wb_sunny_rounded;
  }

  @override
  Widget build(BuildContext context) {
    if (_temperature == null) {
      return const SizedBox.shrink();
    }

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(
          _weatherIcon ?? Icons.wb_sunny_rounded,
          color: Colors.amberAccent,
          size: 20,
          shadows: const [
            Shadow(color: Colors.black54, offset: Offset(0, 2), blurRadius: 4)
          ],
        ),
        const SizedBox(width: 6),
        Text(
          '$_temperature°C',
          style: widget.textStyle,
        ),
        if (_cityName != null && _cityName!.isNotEmpty) ...[
          const SizedBox(width: 4),
          Text(
            '· $_cityName',
            style: widget.textStyle.copyWith(
              fontSize: 14,
              color: Colors.white70,
            ),
          ),
        ],
      ],
    );
  }
}
