package com.gathin.tvhome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CinemaWallpapers {
    public static class MovieItem {
        public final String title;
        public final String year;
        public final String url;
        public final String fileName;

        public MovieItem(String title, String year, String url, String fileName) {
            this.title = title;
            this.year = year;
            this.url = url;
            this.fileName = fileName;
        }
    }

    public static final MovieItem[] MOVIES = new MovieItem[]{
        new MovieItem("Interestelar", "2014", "https://image.tmdb.org/t/p/w1280/tR1XVa5bxgdh2bRw2u0DzrgkO2l.jpg", "interestelar.jpg"),
        new MovieItem("Blade Runner 2049", "2017", "https://image.tmdb.org/t/p/w1280/49pANIZXRAdHUiWjjBv4vxPeqRC.jpg", "blade_runner_2049.jpg"),
        new MovieItem("Matrix", "1999", "https://image.tmdb.org/t/p/w1280/lDqMDI3xpbB9UQRyeXfei0MXhqb.jpg", "matrix.jpg"),
        new MovieItem("Pulp Fiction", "1994", "https://image.tmdb.org/t/p/w1280/tptjnB2LDbuUWya9Cx5sQtv5hqb.jpg", "pulp_fiction.jpg"),
        new MovieItem("Duna: Parte 2", "2024", "https://image.tmdb.org/t/p/w1280/hH5lhwd8RzvVGbpRixPvIOltZLt.jpg", "duna_2.jpg"),
        new MovieItem("Oppenheimer", "2023", "https://image.tmdb.org/t/p/w1280/uqrrZICcZFHqlNuZIR3iM1h0Rst.jpg", "oppenheimer.jpg"),
        new MovieItem("A Origem", "2010", "https://image.tmdb.org/t/p/w1280/9e3Dz7aCANy5aRUQF745IlNloJ1.jpg", "a_origem.jpg"),
        new MovieItem("O Poderoso Chefão", "1972", "https://image.tmdb.org/t/p/w1280/wOMxE93W6KcZTuCeNUByNTSaLLt.jpg", "o_poderoso_chefao.jpg"),
        new MovieItem("Clube da Luta", "1999", "https://image.tmdb.org/t/p/w1280/mCICnh7QBH0gzYaTQChBDDVIKdm.jpg", "clube_da_luta.jpg"),
        new MovieItem("Coringa", "2019", "https://image.tmdb.org/t/p/w1280/xLxgVxFWvb9hhUyCDDXxRPPnFck.jpg", "coringa.jpg"),
        new MovieItem("Batman: O Cavaleiro das Trevas", "2008", "https://image.tmdb.org/t/p/w1280/4lj1ikfsSmMZNyfdi8R8Tv5tsgb.jpg", "batman_cavaleiro_trevas.jpg"),
        new MovieItem("O Senhor dos Anéis: O Retorno do Rei", "2003", "https://image.tmdb.org/t/p/w1280/rU4oIKv5I4C59DpcXKmT7kNwGI0.jpg", "senhor_dos_aneis.jpg"),
        new MovieItem("Homem-Aranha: Através do Aranhaverso", "2023", "https://image.tmdb.org/t/p/w1280/fBS6y0LYX4kU6pPSBYMdQy6SIHX.jpg", "aranhaverso.jpg"),
        new MovieItem("Avatar: O Caminho da Água", "2022", "https://image.tmdb.org/t/p/w1280/hm6nONQOgVpKmRK5YUX9EqfJ0NH.jpg", "avatar_2.jpg"),
        new MovieItem("Gladiador", "2000", "https://image.tmdb.org/t/p/w1280/4DUClyGA6OqjXv6yC0Imf6THGfp.jpg", "gladiador.jpg"),
        new MovieItem("Vingadores: Ultimato", "2019", "https://image.tmdb.org/t/p/w1280/q6725aR8Zs4IwGMXzZT8aC8lh41.jpg", "vingadores_ultimato.jpg"),
        new MovieItem("Mad Max: Estrada da Fúria", "2015", "https://image.tmdb.org/t/p/w1280/tH64gzAHDFg7EFcgfkkZyHdGM5P.jpg", "mad_max.jpg"),
        new MovieItem("Top Gun: Maverick", "2022", "https://image.tmdb.org/t/p/w1280/kPbuLGVSJHATkW9fX9L3h1wM0Pa.jpg", "top_gun_maverick.jpg"),
        new MovieItem("A Viagem de Chihiro", "2001", "https://image.tmdb.org/t/p/w1280/hhoKhsyJ3hFaxEm5pMdZRiTu2lJ.jpg", "viagem_chihiro.jpg"),
        new MovieItem("Your Name", "2016", "https://image.tmdb.org/t/p/w1280/bMOKAjTU1TNUjRSF7icldbii06u.jpg", "your_name.jpg"),
        new MovieItem("Bastardos Inglórios", "2009", "https://image.tmdb.org/t/p/w1280/6UjYycIR6VLYgggfNpmSPJHmFS0.jpg", "bastardos_inglorios.jpg"),
        new MovieItem("Star Wars: O Império Contra-Ataca", "1980", "https://image.tmdb.org/t/p/w1280/dLGT8b4Ut10z44uYLaip4QiwKta.jpg", "star_wars_imperio.jpg")
    };

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static void showCinemaPicker(Context context, Runnable onApplied) {
        String[] labels = new String[MOVIES.length + 1];
        labels[0] = "🌟 Baixar Todos os 22 e Ativar Rotação Automática";
        for (int i = 0; i < MOVIES.length; i++) {
            labels[i + 1] = MOVIES[i].title + " (" + MOVIES[i].year + ")";
        }

        new AlertDialog.Builder(context)
            .setTitle("Artes Cinematográficas (TMDB 1080p)")
            .setItems(labels, (dialog, which) -> {
                dialog.dismiss();
                if (which == 0) {
                    downloadAllForRotation(context, onApplied);
                } else {
                    downloadAndApply(context, MOVIES[which - 1], onApplied);
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .show();
    }

    public static void downloadAndApply(Context context, MovieItem movie, Runnable onApplied) {
        Toast.makeText(context, "Baixando arte de \"" + movie.title + "\"...", Toast.LENGTH_SHORT).show();
        EXECUTOR.execute(() -> {
            File dir = Config.dir(context);
            if (dir == null) return;
            File wallpaperFile = new File(dir, Config.WALLPAPER_NAME);
            boolean success = downloadFile(movie.url, wallpaperFile);
            MAIN_HANDLER.post(() -> {
                if (success) {
                    SharedPreferences choices = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE);
                    choices.edit().putString(Config.WALLPAPER_CHOICE_KEY, Config.WALLPAPER_NAME).apply();
                    Toast.makeText(context, "🎬 Papel de parede de \"" + movie.title + "\" aplicado!", Toast.LENGTH_SHORT).show();
                    if (onApplied != null) onApplied.run();
                } else {
                    Toast.makeText(context, "Falha ao baixar arte. Verifique a conexão.", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    public static void downloadAllForRotation(Context context, Runnable onApplied) {
        Toast.makeText(context, "Iniciando download dos 22 papéis de parede em segundo plano...", Toast.LENGTH_LONG).show();
        EXECUTOR.execute(() -> {
            File dir = Config.dir(context);
            if (dir == null) return;
            File wallpapersDir = new File(dir, "wallpapers");
            if (!wallpapersDir.exists()) wallpapersDir.mkdirs();

            int downloaded = 0;
            for (MovieItem m : MOVIES) {
                File target = new File(wallpapersDir, m.fileName);
                if (!target.exists() || target.length() == 0) {
                    if (downloadFile(m.url, target)) downloaded++;
                } else {
                    downloaded++;
                }
            }

            final int total = downloaded;
            MAIN_HANDLER.post(() -> {
                SharedPreferences choices = context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE);
                choices.edit().remove(Config.WALLPAPER_CHOICE_KEY).apply(); // theme default uses wallpapers rotation
                Toast.makeText(context, "🎉 " + total + " papéis de parede prontos! Rotação automática ativada.", Toast.LENGTH_LONG).show();
                if (onApplied != null) onApplied.run();
            });
        });
    }

    private static boolean downloadFile(String fileUrl, File destination) {
        HttpURLConnection conn = null;
        InputStream in = null;
        FileOutputStream out = null;
        try {
            URL url = new URL(fileUrl);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.connect();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return false;
            }
            in = conn.getInputStream();
            out = new FileOutputStream(destination);
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
            return true;
        } catch (Exception e) {
            if (destination.exists()) destination.delete();
            return false;
        } finally {
            try { if (in != null) in.close(); } catch (Exception ignored) {}
            try { if (out != null) out.close(); } catch (Exception ignored) {}
            if (conn != null) conn.disconnect();
        }
    }
}
