package com.rohitbalan.music;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class AlbumArtDownloader {
    public static final int SLEEP_TIME = 100;
    private final Logger logger = LoggerFactory.getLogger(AlbumArtDownloader.class);

    private Map<String, List<String>> artistsCache = new HashMap<>();
    private Map<String, Map<String, String>> artistsReleaseGroupCache = new HashMap<>();

    public ArtWrapper getAlbumArt(final String artist, final String album) throws IOException, InterruptedException {
        Thread.sleep(SLEEP_TIME);
        final List<String> artistMbids = getArtistMbid(artist);

        for(final String artistMbid: artistMbids) {
            try {
                Thread.sleep(SLEEP_TIME);
                final String albumMbid = getAlbumMbid(album, artistMbid);
                Thread.sleep(SLEEP_TIME);
                logger.debug("artist MBID {} album MBID {}", artistMbid, albumMbid);
                final ArtWrapper artWrapper = getAlbumArtUrl(albumMbid);

                downloadImages(artWrapper);
                return artWrapper;
            } catch (Exception e) {
                logger.error("Didnt work out");
            }
        }
        throw new RuntimeException("Nope");

    }

    private void downloadImages(final ArtWrapper artWrapper) throws IOException {
        artWrapper.setImage(downloadImage(artWrapper.getImageUrl()));
        artWrapper.setThumbnail(downloadImage(artWrapper.getThumbnailUrl()));
    }

    private File downloadImage(final String url) throws IOException {
        final String ext = url.substring(url.lastIndexOf("."));
        final File imageFile = File.createTempFile("art", ext);
        FileUtils.copyURLToFile(new URL(url), imageFile);
        return imageFile;
    }

    private List<String> getArtistMbid(final String artist) throws IOException, InterruptedException {
        if(artistsCache.containsKey(artist)) {
            return artistsCache.get(artist);
        }

        final List<String> mbids = new ArrayList<>();

        Thread.sleep(2000);
        final Client client = ClientBuilder.newClient();
        final String url = "https://musicbrainz.org/ws/2/artist/?limit=5&query=" +
                URLEncoder.encode(artist, String.valueOf(StandardCharsets.UTF_8));
        final WebTarget webTarget = client.target(url);
        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        final String response = invocationBuilder.get(String.class);
        final Map<String, List<Map<String, String>>> responseObject = new ObjectMapper().readValue(response, Map.class);
        for(final Map<String, String> artistMap : responseObject.get("artists")) {
            mbids.add(artistMap.get("id"));
        }

        artistsCache.put(artist, mbids);

        return mbids;
    }

    private Map<String, String> getArtistReleaseGroups(final String mbid) throws IOException, InterruptedException {
        if(artistsReleaseGroupCache.containsKey(mbid)) {
            return artistsReleaseGroupCache.get(mbid);
        }

        final Map<String, String> artistReleaseGroups = new HashMap<>();

        Thread.sleep(2000);
        final Client client = ClientBuilder.newClient();
        final String url = "https://musicbrainz.org/ws/2/artist/" +
                mbid +
                "?inc=release-groups";
        final WebTarget webTarget = client.target(url);
        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        final String response = invocationBuilder.get(String.class);
        final Map<String, List<Map<String, String>>> responseObject = new ObjectMapper().readValue(response, Map.class);
        for(final Map<String, String> albumInfo: responseObject.get("release-groups")) {
            String title = cleanSymbols(albumInfo.get("title"));
            String titlePlusDisambiguation = cleanSymbols(albumInfo.get("title") + " " + albumInfo.get("disambiguation"));
            String albumId = albumInfo.get("id");
            if(!artistReleaseGroups.containsKey(title)) {
                artistReleaseGroups.put(title, albumId);
            }
            if(!artistReleaseGroups.containsKey(titlePlusDisambiguation)) {
                artistReleaseGroups.put(titlePlusDisambiguation, albumId);
            }
        }

        artistsReleaseGroupCache.put(mbid, artistReleaseGroups);
        return artistReleaseGroups;
    }

    private String cleanSymbols(final String input) {
        final String replacement = input.replaceAll("[^a-zA-Z0-9\\s]"," ");
        return clearMultipleSpaces(replacement).toLowerCase(Locale.US);
    }

    private String clearMultipleSpaces(final String input) {
        if(input.contains("  ")) {
            return clearMultipleSpaces(input.replace("  ", " "));
        } else {
            return input;
        }
    }

    private String getAlbumMbid(final String album, String artistMbid) throws IOException, InterruptedException {
        final Map<String, String> artistAlbums = getArtistReleaseGroups(artistMbid);
        return artistAlbums.get(cleanSymbols(album));
    }

    private ArtWrapper getAlbumArtUrl(final String albumMbid) throws IOException {
        if(albumMbid!=null) {
            final ArtWrapper artWrapper = new ArtWrapper();
            final Client client = ClientBuilder.newClient();
            final String url = "https://coverartarchive.org/release-group/" + albumMbid;
            final WebTarget webTarget = client.target(url);
            final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
            final String response = invocationBuilder.get(String.class);
            final Map<String, ?> responseObject = new ObjectMapper().readValue(response, Map.class);
            final String imageUrl = ((Map<String, List<Map<String, String>>>) responseObject).get("images").get(0).get("image");
            final String thumbnailUrl = ((Map<String, List<Map<String, Map<String, String>>>>) responseObject).get("images").get(0).get("thumbnails").get("large");
            artWrapper.setImageUrl(imageUrl);
            artWrapper.setThumbnailUrl(thumbnailUrl);
            return artWrapper;
        } else {
            throw new RuntimeException("No image found");
        }

    }

}
