package com.rohitbalan.music;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class MusicManager {
    private final Logger logger = LoggerFactory.getLogger(MusicManager.class);

    @Autowired
    private Downloader downloader;

    public void execute(String[] args) {
        if (args != null) {
            for (String arg : args) {
                try {
                    final List<Track> tracks = new Parser(arg).execute();

                    for (final Track track : tracks) {
                        downloader.download(track);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        logger.info("Exiting");
    }
}
