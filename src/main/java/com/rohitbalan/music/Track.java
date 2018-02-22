package com.rohitbalan.music;

public class Track {
	private String artist, album, title, url;
	private int trackNumber;
	private ArtWrapper albumArt;

	public int getTrackNumber() {
		return trackNumber;
	}

	public void setTrackNumber(int trackNumber) {
		this.trackNumber = trackNumber;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public ArtWrapper getAlbumArt() {
		return albumArt;
	}

	public void setAlbumArt(ArtWrapper albumArt) {
		this.albumArt = albumArt;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return artist 
				+ " - " + album
				+ " - " + title
				+ " - " +  url
				;
	}
	
}
