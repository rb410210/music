package com.rohitbalan.music;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Component
public class MusicManager {
    private final Logger logger = LoggerFactory.getLogger(MusicManager.class);

    @Autowired
    private Downloader downloader;
    @Autowired
    private AlbumArtDownloader albumArtDownloader;

    @Value("${com.rohitbalan.music.MusicManager.bulkImport}")
    private boolean bulkImport;


    public void execute(String[] args) {
        if (args != null) {
            for (String arg : args) {
                try {
                    final List<Track> tracks = new Parser(arg).execute();

                    ArtWrapper albumArt = null;
                    try {
                        albumArt = albumArtDownloader.getAlbumArt(tracks.get(0).getArtist(), tracks.get(0).getAlbum());
                    } catch (Exception e) {
                        logger.error("Unable to download album art", e);
                    }

                    for (final Track track : tracks) {
                        track.setAlbumArt(albumArt);
                        downloader.download(track);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        if(bulkImport) {
            logger.info("Starting bulk import");
            bulkUpdateAlbumArt();
        }

        logger.info("Exiting");
    }

    private void bulkUpdateAlbumArt() {
        final File bulkImportFolder = new File("/mnt/data/Mp3");
        final File[] artistFolders = bulkImportFolder.listFiles(pathname -> pathname.isDirectory());
        for(final File artistFolder: artistFolders) {
            final String artist = artistFolder.getName();
            final File[] albumFolders = artistFolder.listFiles(pathname -> pathname.isDirectory());
            for(final File albumFolder: albumFolders) {
                final String album = albumFolder.getName();
                final File[] artFiles = albumFolder.listFiles((dir, name) -> name.startsWith("cover."));
                if(artFiles.length == 0) {
                    logger.info("Artist {} - Album {}", artist, album);
                    try {
                        final ArtWrapper albumArt = albumArtDownloader.getAlbumArt(artist, album);
                        final String ext = albumArt.getImageUrl().substring(albumArt.getImageUrl().lastIndexOf("."));
                        FileCopyUtils.copy(albumArt.getImage(), new File(albumFolder, "cover" + ext));
                        final File[] mp3Files = albumFolder.listFiles((dir, name) -> name.endsWith(".mp3"));
                        for(final File mp3File: mp3Files) {
                            new Tagger().addImage(mp3File, albumArt.getThumbnail());
                        }
                        logger.debug("albumArt {}", albumArt);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
