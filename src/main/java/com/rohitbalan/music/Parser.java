package com.rohitbalan.music;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Parser {
	private String siteUrl;

	public Parser(String siteUrl) {
		super();
		this.siteUrl = siteUrl;
	}
	
	public List<Track> execute() throws IOException {
		List<Track> mp3Urls = new ArrayList<Track>();
		CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpGet httpGet = new HttpGet(siteUrl);
		CloseableHttpResponse response1 = httpclient.execute(httpGet);
		try {
		    System.out.println(response1.getStatusLine() + " " + siteUrl);
		    HttpEntity entity = response1.getEntity();
		    String response = EntityUtils.toString(entity, StandardCharsets.UTF_8);
		    //System.out.println(response);
		    EntityUtils.consume(entity);
		    
		    Document html = Jsoup.parse(response);
			
		    int trackNumber = 1;
			for(Element trElement: html.body().getElementsByTag("tr")) {
				Track track = new Track();
				
				track.setTrackNumber(trackNumber);
				trackNumber++;
				
		    	if(trElement.hasAttr("itemprop") && "tracks".equals(trElement.attr("itemprop"))) {
		    		for(Element metaElement: trElement.getElementsByTag("meta")) {
		    			String itemprop = metaElement.attr("itemprop");
		    			if("inAlbum".equals(itemprop)) {
		    				String album = metaElement.attr("content");
		    				track.setAlbum(album);
		    			} else if("byArtist".equals(itemprop)) {
		    				String artist = metaElement.attr("content");
		    				track.setArtist(artist);
		    			}
		    		}
		    		for(Element spanElement: trElement.getElementsByTag("span")) {
		    			String itemprop = spanElement.attr("itemprop");
		    			if("name".equals(itemprop)) {
		    				String title = spanElement.text();
		    				track.setTitle(title);
		    			}
		    		}
		    		for(Element anchor: trElement.getElementsByTag("a")) {
		    			String titleAttr = anchor.attr("title");
		    			if("Play track".equals(titleAttr)) {
		    				String relAttr = anchor.attr("rel");
		    				track.setUrl("https://listen.musicmp3.ru/" + relAttr);
		    			}
		    		}
		    	}
		    	mp3Urls.add(track);
			};
			
		    
		} finally {
		    response1.close();
		}
		System.out.println(mp3Urls.toString().replaceAll(", ", "\r\n"));
		return mp3Urls;
	}
}
