package com.rohitbalan.music;

import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.FileInputStream;

public class Tagger {
    private final Logger logger = LoggerFactory.getLogger(Tagger.class);

    public void addTag(final File sourceFile, final File destinationFile, final Track track)  {
        try {
            final Mp3File mp3file = new Mp3File(sourceFile.getAbsoluteFile());
            final ID3v2 id3v2Tag;
            if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
            } else {
                // mp3 does not have an ID3v2 tag, let's create one..
                id3v2Tag = new ID3v24Tag();
                mp3file.setId3v2Tag(id3v2Tag);
            }
            id3v2Tag.setTrack("" + track.getTrackNumber());
            id3v2Tag.setArtist(track.getArtist());
            id3v2Tag.setTitle(track.getTitle());
            id3v2Tag.setAlbum(track.getAlbum());
            id3v2Tag.setUrl(track.getUrl());
            mp3file.save(destinationFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void addImage(final File sourceFile, final File image)  {
        try {
            final File tempFile = File.createTempFile(sourceFile.getName(), "mp3");
            FileCopyUtils.copy(sourceFile, tempFile);
            final Mp3File mp3file = new Mp3File(tempFile.getAbsoluteFile());
            final ID3v2 id3v2Tag;
            if (mp3file.hasId3v2Tag()) {
                id3v2Tag = mp3file.getId3v2Tag();
            } else {
                id3v2Tag = new ID3v24Tag();
                mp3file.setId3v2Tag(id3v2Tag);
            }
            id3v2Tag.setAlbumImage(IOUtils.toByteArray(new FileInputStream(image)), "jpg");
            mp3file.save(sourceFile.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
