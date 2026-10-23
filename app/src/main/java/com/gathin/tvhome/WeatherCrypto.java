package com.gathin.tvhome;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Lightweight background fetcher for Taubaté weather (Open-Meteo) and crypto/USDT quotes (Binance).
 * Zero API keys required.
 */
public final class WeatherCrypto {

    public interface Callback {
        void onUpdate(String line);
    }

    private static final String WEATHER_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=-23.0333&longitude=-45.5556&current=temperature_2m,weather_code,is_day&timezone=auto";
    private static final String BINANCE_URL =
            "https://api.binance.com/api/v3/ticker/price?symbols=%5B%22BTCUSDT%22,%22ETHUSDT%22,%22SOLUSDT%22,%22USDTBRL%22%5D";

    private static final long WEATHER_INTERVAL_MS = 20 * 60_000L; // 20 min
    private static final long CRYPTO_INTERVAL_MS = 3 * 60_000L;   // 3 min

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static long lastWeatherFetch = 0;
    private static long lastCryptoFetch = 0;

    private static String cachedWeather = "Taubaté --°C";
    private static String cachedCrypto = "";
    private static String lastFullLine = "";

    public static void start(Callback callback) {
        executor.execute(() -> {
            refresh(callback);
        });
    }

    public static String getLiveLine() {
        if (lastFullLine.isEmpty()) {
            return formatLine();
        }
        return lastFullLine;
    }

    public static void refresh(Callback callback) {
        long now = System.currentTimeMillis();
        boolean changed = false;

        if (now - lastWeatherFetch > WEATHER_INTERVAL_MS || lastWeatherFetch == 0) {
            String w = fetchWeather();
            if (w != null && !w.isEmpty()) {
                cachedWeather = w;
                lastWeatherFetch = now;
                changed = true;
            }
        }

        if (now - lastCryptoFetch > CRYPTO_INTERVAL_MS || lastCryptoFetch == 0) {
            String c = fetchCrypto();
            if (c != null && !c.isEmpty()) {
                cachedCrypto = c;
                lastCryptoFetch = now;
                changed = true;
            }
        }

        String fullLine = formatLine();
        if (changed || !fullLine.equals(lastFullLine)) {
            lastFullLine = fullLine;
            if (callback != null) {
                mainHandler.post(() -> callback.onUpdate(fullLine));
            }
        }
    }

    private static String formatLine() {
        StringBuilder sb = new StringBuilder();
        if (!cachedWeather.isEmpty()) {
            sb.append("⛅ ").append(cachedWeather);
        }
        if (!cachedCrypto.isEmpty()) {
            if (sb.length() > 0) sb.append("   ·   ");
            sb.append(cachedCrypto);
        }
        return sb.toString();
    }

    private static String fetchWeather() {
        try {
            String json = httpGet(WEATHER_URL, 6000);
            if (json == null) return null;
            JSONObject root = new JSONObject(json);
            JSONObject cur = root.getJSONObject("current");
            int temp = (int) Math.round(cur.getDouble("temperature_2m"));
            int code = cur.getInt("weather_code");
            String cond = conditionText(code);
            return String.format(Locale.ROOT, "Taubaté %d°C %s", temp, cond);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String conditionText(int code) {
        switch (code) {
            case 0: return "Céu limpo";
            case 1: return "Tempo limpo";
            case 2: return "Parcialmente nublado";
            case 3: return "Nublado";
            case 45: case 48: return "Nevoeiro";
            case 51: case 53: case 55: return "Garoa";
            case 61: case 63: case 65: return "Chuva";
            case 80: case 81: case 82: return "Pancadas de chuva";
            case 95: case 96: case 99: return "Tempestade";
            default: return "Nublado";
        }
    }

    private static String fetchCrypto() {
        try {
            String json = httpGet(BINANCE_URL, 6000);
            if (json == null) return null;
            JSONArray arr = new JSONArray(json);
            double usdtBrl = 5.0;
            double btcUsdt = 0;
            double ethUsdt = 0;
            double solUsdt = 0;

            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.getJSONObject(i);
                String sym = item.getString("symbol");
                double price = item.getDouble("price");
                if ("USDTBRL".equals(sym)) usdtBrl = price;
                else if ("BTCUSDT".equals(sym)) btcUsdt = price;
                else if ("ETHUSDT".equals(sym)) ethUsdt = price;
                else if ("SOLUSDT".equals(sym)) solUsdt = price;
            }

            int btcBrl = (int) Math.round(btcUsdt * usdtBrl);
            int ethBrl = (int) Math.round(ethUsdt * usdtBrl);
            int solBrl = (int) Math.round(solUsdt * usdtBrl);

            return String.format(Locale.ROOT,
                    "₿ BTC R$ %,d   ·   Ξ ETH R$ %,d   ·   ◎ SOL R$ %,d   ·   $ Dólar R$ %.2f",
                    btcBrl, ethBrl, solBrl, usdtBrl);
        } catch (Throwable t) {
            return null;
        }
    }

    private static String httpGet(String urlStr, int timeoutMs) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestProperty("User-Agent", "ArcLauncher/2.2.6");
            int code = conn.getResponseCode();
            if (code != 200) return null;

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Throwable t) {
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
