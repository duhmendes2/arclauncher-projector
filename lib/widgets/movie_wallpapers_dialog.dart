import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:http/http.dart' as http;
import 'package:provider/provider.dart';
import '../providers/wallpaper_service.dart';
import 'tv_media_picker.dart';

class MovieWallpaperItem {
  final String title;
  final String year;
  final String imageUrl;
  final String thumbUrl;

  const MovieWallpaperItem({
    required this.title,
    required this.year,
    required this.imageUrl,
    required this.thumbUrl,
  });
}

class MovieWallpapersDialog extends StatefulWidget {
  const MovieWallpapersDialog({Key? key}) : super(key: key);

  @override
  State<MovieWallpapersDialog> createState() => _MovieWallpapersDialogState();
}

class _MovieWallpapersDialogState extends State<MovieWallpapersDialog> {
  bool _loading = false;
  String _loadingTitle = '';

  static const List<MovieWallpaperItem> _movies = [
    MovieWallpaperItem(
      title: 'Interestelar',
      year: '2014',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/tR1XVa5bxgdh2bRw2u0DzrgkO2l.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/tR1XVa5bxgdh2bRw2u0DzrgkO2l.jpg',
    ),
    MovieWallpaperItem(
      title: 'Blade Runner 2049',
      year: '2017',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/49pANIZXRAdHUiWjjBv4vxPeqRC.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/49pANIZXRAdHUiWjjBv4vxPeqRC.jpg',
    ),
    MovieWallpaperItem(
      title: 'Matrix',
      year: '1999',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/lDqMDI3xpbB9UQRyeXfei0MXhqb.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/lDqMDI3xpbB9UQRyeXfei0MXhqb.jpg',
    ),
    MovieWallpaperItem(
      title: 'Pulp Fiction',
      year: '1994',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/tptjnB2LDbuUWya9Cx5sQtv5hqb.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/tptjnB2LDbuUWya9Cx5sQtv5hqb.jpg',
    ),
    MovieWallpaperItem(
      title: 'Duna: Parte 2',
      year: '2024',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/hH5lhwd8RzvVGbpRixPvIOltZLt.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/hH5lhwd8RzvVGbpRixPvIOltZLt.jpg',
    ),
    MovieWallpaperItem(
      title: 'Oppenheimer',
      year: '2023',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/uqrrZICcZFHqlNuZIR3iM1h0Rst.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/uqrrZICcZFHqlNuZIR3iM1h0Rst.jpg',
    ),
    MovieWallpaperItem(
      title: 'A Origem',
      year: '2010',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/9e3Dz7aCANy5aRUQF745IlNloJ1.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/9e3Dz7aCANy5aRUQF745IlNloJ1.jpg',
    ),
    MovieWallpaperItem(
      title: 'O Poderoso Chefão',
      year: '1972',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/wOMxE93W6KcZTuCeNUByNTSaLLt.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/wOMxE93W6KcZTuCeNUByNTSaLLt.jpg',
    ),
    MovieWallpaperItem(
      title: 'Clube da Luta',
      year: '1999',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/mCICnh7QBH0gzYaTQChBDDVIKdm.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/mCICnh7QBH0gzYaTQChBDDVIKdm.jpg',
    ),
    MovieWallpaperItem(
      title: 'Coringa',
      year: '2019',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/xLxgVxFWvb9hhUyCDDXxRPPnFck.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/xLxgVxFWvb9hhUyCDDXxRPPnFck.jpg',
    ),
    MovieWallpaperItem(
      title: 'Batman: O Cavaleiro das Trevas',
      year: '2008',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/4lj1ikfsSmMZNyfdi8R8Tv5tsgb.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/4lj1ikfsSmMZNyfdi8R8Tv5tsgb.jpg',
    ),
    MovieWallpaperItem(
      title: 'O Senhor dos Anéis: O Retorno do Rei',
      year: '2003',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/rU4oIKv5I4C59DpcXKmT7kNwGI0.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/rU4oIKv5I4C59DpcXKmT7kNwGI0.jpg',
    ),
    MovieWallpaperItem(
      title: 'Homem-Aranha: Através do Aranhaverso',
      year: '2023',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/fBS6y0LYX4kU6pPSBYMdQy6SIHX.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/fBS6y0LYX4kU6pPSBYMdQy6SIHX.jpg',
    ),
    MovieWallpaperItem(
      title: 'Avatar: O Caminho da Água',
      year: '2022',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/hm6nONQOgVpKmRK5YUX9EqfJ0NH.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/hm6nONQOgVpKmRK5YUX9EqfJ0NH.jpg',
    ),
    MovieWallpaperItem(
      title: 'Gladiador',
      year: '2000',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/4DUClyGA6OqjXv6yC0Imf6THGfp.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/4DUClyGA6OqjXv6yC0Imf6THGfp.jpg',
    ),
    MovieWallpaperItem(
      title: 'Vingadores: Ultimato',
      year: '2019',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/q6725aR8Zs4IwGMXzZT8aC8lh41.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/q6725aR8Zs4IwGMXzZT8aC8lh41.jpg',
    ),
    MovieWallpaperItem(
      title: 'Mad Max: Estrada da Fúria',
      year: '2015',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/tH64gzAHDFg7EFcgfkkZyHdGM5P.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/tH64gzAHDFg7EFcgfkkZyHdGM5P.jpg',
    ),
    MovieWallpaperItem(
      title: 'Top Gun: Maverick',
      year: '2022',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/kPbuLGVSJHATkW9fX9L3h1wM0Pa.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/kPbuLGVSJHATkW9fX9L3h1wM0Pa.jpg',
    ),
    MovieWallpaperItem(
      title: 'A Viagem de Chihiro',
      year: '2001',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/hhoKhsyJ3hFaxEm5pMdZRiTu2lJ.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/hhoKhsyJ3hFaxEm5pMdZRiTu2lJ.jpg',
    ),
    MovieWallpaperItem(
      title: 'Your Name',
      year: '2016',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/bMOKAjTU1TNUjRSF7icldbii06u.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/bMOKAjTU1TNUjRSF7icldbii06u.jpg',
    ),
    MovieWallpaperItem(
      title: 'Bastardos Inglórios',
      year: '2009',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/6UjYycIR6VLYgggfNpmSPJHmFS0.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/6UjYycIR6VLYgggfNpmSPJHmFS0.jpg',
    ),
    MovieWallpaperItem(
      title: 'Star Wars: O Império Contra-Ataca',
      year: '1980',
      imageUrl: 'https://image.tmdb.org/t/p/w1280/dLGT8b4Ut10z44uYLaip4QiwKta.jpg',
      thumbUrl: 'https://image.tmdb.org/t/p/w500/dLGT8b4Ut10z44uYLaip4QiwKta.jpg',
    ),
  ];

  Future<void> _applyWallpaper(MovieWallpaperItem movie) async {
    setState(() {
      _loading = true;
      _loadingTitle = movie.title;
    });

    try {
      final response = await http
          .get(Uri.parse(movie.imageUrl))
          .timeout(const Duration(seconds: 15));

      if (response.statusCode == 200 && mounted) {
        await context.read<WallpaperService>().setWallpaperFromBytes(response.bodyBytes);
        Navigator.of(context).pop();
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('🎬 Papel de parede cinematográfico de "${movie.title}" aplicado!'),
            backgroundColor: const Color(0xFF1E2614),
            duration: const Duration(seconds: 3),
          ),
        );
      } else {
        throw Exception('Status code: ${response.statusCode}');
      }
    } catch (e) {
      if (mounted) {
        setState(() => _loading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Falha ao carregar arte de "${movie.title}". Tente outra opção.'),
            backgroundColor: Colors.redAccent,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: const Color(0xFF101214),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        width: 900,
        height: 520,
        padding: const EdgeInsets.all(22),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.movie_filter_rounded, color: Colors.amberAccent, size: 28),
                const SizedBox(width: 12),
                const Text(
                  'Artes Cinematográficas (1080p)',
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.white),
                ),
                const Spacer(),
                OutlinedButton.icon(
                  style: OutlinedButton.styleFrom(
                    foregroundColor: Colors.amberAccent,
                    side: const BorderSide(color: Colors.amberAccent),
                    padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                  ),
                  icon: const Icon(Icons.folder_open_rounded, size: 18),
                  label: const Text('Do Pendrive / Armazenamento'),
                  onPressed: () async {
                    Navigator.of(context).pop();
                    final media = await TvMediaPicker.show(
                      context,
                      mode: TvMediaPickerMode.image,
                    );
                    if (media != null && context.mounted) {
                      await context.read<WallpaperService>().pickWallpaperFromUri(media.uri);
                    }
                  },
                ),
                const SizedBox(width: 8),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.white70),
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
            const SizedBox(height: 6),
            const Text(
              'Navegue com as setas do controle remoto e aperte OK para aplicar como fundo:',
              style: TextStyle(fontSize: 13, color: Colors.white60),
            ),
            const SizedBox(height: 14),
            Expanded(
              child: _loading
                  ? Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const CircularProgressIndicator(color: Colors.amberAccent),
                          const SizedBox(height: 16),
                          Text(
                            'Baixando arte de "$_loadingTitle"...',
                            style: const TextStyle(color: Colors.white, fontSize: 16),
                          ),
                          const SizedBox(height: 8),
                          const Text(
                            'Aplicando fundo de tela em alta resolução...',
                            style: TextStyle(color: Colors.white54, fontSize: 12),
                          ),
                        ],
                      ),
                    )
                  : GridView.builder(
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 4,
                        crossAxisSpacing: 14,
                        mainAxisSpacing: 14,
                        childAspectRatio: 16 / 9.5,
                      ),
                      itemCount: _movies.length,
                      itemBuilder: (context, index) {
                        return _MovieGridCard(
                          movie: _movies[index],
                          autofocus: index == 0,
                          onSelect: () => _applyWallpaper(_movies[index]),
                        );
                      },
                    ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MovieGridCard extends StatefulWidget {
  final MovieWallpaperItem movie;
  final bool autofocus;
  final VoidCallback onSelect;

  const _MovieGridCard({
    Key? key,
    required this.movie,
    required this.autofocus,
    required this.onSelect,
  }) : super(key: key);

  @override
  State<_MovieGridCard> createState() => _MovieGridCardState();
}

class _MovieGridCardState extends State<_MovieGridCard> {
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
          widget.onSelect();
          return KeyEventResult.handled;
        }
        return KeyEventResult.ignored;
      },
      child: GestureDetector(
        onTap: widget.onSelect,
        child: AnimatedScale(
          scale: _focused ? 1.06 : 1.0,
          duration: const Duration(milliseconds: 140),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 140),
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(10),
              border: Border.all(
                color: _focused ? Colors.amberAccent : Colors.white24,
                width: _focused ? 3.5 : 1.0,
              ),
              boxShadow: _focused
                  ? [
                      BoxShadow(
                        color: Colors.amberAccent.withOpacity(0.4),
                        blurRadius: 14,
                        spreadRadius: 2,
                      ),
                    ]
                  : [],
            ),
            child: ClipRRect(
              borderRadius: BorderRadius.circular(7),
              child: Stack(
                fit: StackFit.expand,
                children: [
                  Image.network(
                    widget.movie.thumbUrl,
                    fit: BoxFit.cover,
                    loadingBuilder: (ctx, child, progress) {
                      if (progress == null) return child;
                      return Container(
                        color: const Color(0xFF1E2228),
                        child: const Center(
                          child: SizedBox(
                            width: 22,
                            height: 22,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              color: Colors.white24,
                            ),
                          ),
                        ),
                      );
                    },
                    errorBuilder: (ctx, err, stack) {
                      return Container(
                        color: const Color(0xFF1E2228),
                        child: const Center(
                          child: Icon(Icons.movie_rounded, color: Colors.white38),
                        ),
                      );
                    },
                  ),
                  Positioned(
                    bottom: 0,
                    left: 0,
                    right: 0,
                    child: Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 5),
                      decoration: const BoxDecoration(
                        gradient: LinearGradient(
                          begin: Alignment.topCenter,
                          end: Alignment.bottomCenter,
                          colors: [Colors.transparent, Colors.black87],
                        ),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            widget.movie.title,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(
                              color: _focused ? Colors.amberAccent : Colors.white,
                              fontSize: 12,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            widget.movie.year,
                            style: const TextStyle(
                              color: Colors.white70,
                              fontSize: 10,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                  if (_focused)
                    Positioned(
                      top: 6,
                      right: 6,
                      child: Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(
                          color: Colors.amberAccent,
                          borderRadius: BorderRadius.circular(4),
                        ),
                        child: const Text(
                          'OK',
                          style: TextStyle(
                            color: Colors.black,
                            fontSize: 9,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}
