package com.rohitbalan.music;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class Downloader {
	public void download(Track track) {
		String folderStr = "/home/rohit/Music/" + track.getArtist() + "/" + track.getAlbum();
		File folder = new File(folderStr);
		folder.mkdirs();
		
		downloadBinary(track, folder, 6);
	}

	private void downloadBinary(Track track, File folder, int retryCount) {
		if(retryCount==0)
			return;
		
		try {
			System.out.println("Starting Download: " + track.getUrl());
			int timeout = 10;
			RequestConfig config = RequestConfig.custom()
					.setConnectTimeout(timeout * 1000)
					.setConnectionRequestTimeout(timeout * 1000)
					.setSocketTimeout(timeout * 1000).build();
			CloseableHttpClient httpclient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
			HttpGet httpGet = new HttpGet(track.getUrl());
			httpGet.addHeader("User-Agent", "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:47.0) Gecko/20100101 Firefox/47.0");
			CloseableHttpResponse response = httpclient.execute(httpGet);
			try {
			    HttpEntity entity = response.getEntity();
			    int responseCode = response.getStatusLine().getStatusCode();
			    try {
			    	if(responseCode >= 200 && responseCode <300) {
					    InputStream content = entity.getContent();
					    File mp3File = new File(folder, track.getTrackNumber() + ". " + track.getTitle() + ".mp3");
					    if(mp3File.exists()) {
					    	mp3File.delete();
					    }
					    Path destination = Paths.get(mp3File.getAbsolutePath());
					    Files.copy(content, destination);
					    System.out.println("Download Complete");
			    	} else {
			    		throw new Exception("Response Code: " + responseCode);
			    	}
			    } finally {
				    EntityUtils.consume(entity);
			    }
			} finally {
			    response.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			downloadBinary(track, folder, retryCount-1);
		}
	}
}
