import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:http/http.dart' as http;
import 'package:provider/provider.dart';
import '../providers/wallpaper_service.dart';
import 'tv_media_picker.dart';

class MovieWallpaperItem {
  final String title;
  final String year;
  final String imageUrl;

  const MovieWallpaperItem({
    required this.title,
    required this.year,
    required this.imageUrl,
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
      imageUrl: 'https://image.tmdb.org/t/p/original/xJHokMbljvjADYdit5fK5VQsXEG.jpg',
    ),
    MovieWallpaperItem(
      title: 'Blade Runner 2049',
      year: '2017',
      imageUrl: 'https://image.tmdb.org/t/p/original/ilRyazdMJwN05exqhwK4tMKBYZs.jpg',
    ),
    MovieWallpaperItem(
      title: 'Matrix',
      year: '1999',
      imageUrl: 'https://image.tmdb.org/t/p/original/7u3fhRwFQU7rRN1iwh0T535Z0zK.jpg',
    ),
    MovieWallpaperItem(
      title: 'Star Wars: Uma Nova Esperança',
      year: '1977',
      imageUrl: 'https://image.tmdb.org/t/p/original/zqkmTXzjkAgPnHGzqMYBGJw0c6g.jpg',
    ),
    MovieWallpaperItem(
      title: 'Batman: O Cavaleiro das Trevas',
      year: '2008',
      imageUrl: 'https://image.tmdb.org/t/p/original/hkBaDkMWbLaf8B1rDYRuxkPLCmZ.jpg',
    ),
    MovieWallpaperItem(
      title: 'Pulp Fiction',
      year: '1994',
      imageUrl: 'https://image.tmdb.org/t/p/original/suaEOtk1N1sgg2MTM7oZd2cfVp3.jpg',
    ),
    MovieWallpaperItem(
      title: 'Duna: Parte 2',
      year: '2024',
      imageUrl: 'https://image.tmdb.org/t/p/original/jXJxMcVoEuXzym3vFnjqDW4ifo6.jpg',
    ),
    MovieWallpaperItem(
      title: 'Homem-Aranha no Aranhaverso',
      year: '2018',
      imageUrl: 'https://image.tmdb.org/t/p/original/uUiId6cG32JSRjT6x1VaYCPLkvV.jpg',
    ),
    MovieWallpaperItem(
      title: 'A Viagem de Chihiro',
      year: '2001',
      imageUrl: 'https://image.tmdb.org/t/p/original/Ab8mkHmkYADjU7wQiOkia99GQI.jpg',
    ),
    MovieWallpaperItem(
      title: 'Oppenheimer',
      year: '2023',
      imageUrl: 'https://image.tmdb.org/t/p/original/fm6KqXpk3M2HVveHwCrBSSBaO0V.jpg',
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
            content: Text('Papel de parede de "${movie.title}" aplicado!'),
            backgroundColor: Colors.black87,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        setState(() => _loading = false);
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Erro ao baixar papel de parede. Verifique a conexão.'),
            backgroundColor: Colors.redAccent,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Dialog(
      backgroundColor: const Color(0xFF141414),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Container(
        width: 800,
        height: 480,
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.movie_filter_rounded, color: Colors.amberAccent, size: 28),
                const SizedBox(width: 12),
                const Text(
                  'Papéis de Parede de Cinema',
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.white),
                ),
                const Spacer(),
                TextButton.icon(
                  icon: const Icon(Icons.folder_open_rounded, size: 18),
                  label: const Text('Do Pendrive / PC'),
                  onPressed: () {
                    Navigator.of(context).pop();
                    showDialog(
                      context: context,
                      builder: (_) => TvMediaPicker(
                        title: 'Selecionar Imagem do Pendrive',
                        mediaType: TvMediaType.image,
                        onMediaSelected: (file) {
                          context.read<WallpaperService>().pickWallpaper(file);
                        },
                      ),
                    );
                  },
                ),
                IconButton(
                  icon: const Icon(Icons.close, color: Colors.white70),
                  onPressed: () => Navigator.of(context).pop(),
                ),
              ],
            ),
            const SizedBox(height: 6),
            const Text(
              'Escolha uma arte cinematográfica em 1080p para o fundo do seu projetor:',
              style: TextStyle(fontSize: 14, color: Colors.white60),
            ),
            const SizedBox(height: 16),
            Expanded(
              child: _loading
                  ? Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          const CircularProgressIndicator(color: Colors.amberAccent),
                          const SizedBox(height: 16),
                          Text(
                            'Baixando e aplicando "$_loadingTitle"...',
                            style: const TextStyle(color: Colors.white70, fontSize: 16),
                          ),
                        ],
                      ),
                    )
                  : GridView.builder(
                      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                        crossAxisCount: 5,
                        crossAxisSpacing: 12,
                        mainAxisSpacing: 12,
                        childAspectRatio: 16 / 10,
                      ),
                      itemCount: _movies.length,
                      itemBuilder: (context, index) {
                        final movie = _movies[index];
                        return Focus(
                          child: Builder(
                            builder: (focusContext) {
                              final isFocused = Focus.of(focusContext).hasFocus;
                              return InkWell(
                                onTap: () => _applyWallpaper(movie),
                                borderRadius: BorderRadius.circular(10),
                                child: AnimatedContainer(
                                  duration: const Duration(milliseconds: 150),
                                  decoration: BoxDecoration(
                                    borderRadius: BorderRadius.circular(10),
                                    border: Border.all(
                                      color: isFocused ? Colors.amberAccent : Colors.white24,
                                      width: isFocused ? 3 : 1,
                                    ),
                                    boxShadow: isFocused
                                        ? [
                                            BoxShadow(
                                              color: Colors.amberAccent.withOpacity(0.4),
                                              blurRadius: 10,
                                              spreadRadius: 2,
                                            )
                                          ]
                                        : null,
                                    image: DecorationImage(
                                      image: NetworkImage(movie.imageUrl),
                                      fit: BoxFit.cover,
                                    ),
                                  ),
                                  alignment: Alignment.bottomCenter,
                                  child: Container(
                                    width: double.infinity,
                                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 4),
                                    decoration: const BoxDecoration(
                                      borderRadius: BorderRadius.only(
                                        bottomLeft: Radius.circular(8),
                                        bottomRight: Radius.circular(8),
                                      ),
                                      gradient: LinearGradient(
                                        begin: Alignment.topCenter,
                                        end: Alignment.bottomCenter,
                                        colors: [Colors.transparent, Colors.black87],
                                      ),
                                    ),
                                    child: Text(
                                      movie.title,
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                      style: const TextStyle(
                                        color: Colors.white,
                                        fontSize: 11,
                                        fontWeight: FontWeight.bold,
                                      ),
                                    ),
                                  ),
                                ),
                              );
                            },
                          ),
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
