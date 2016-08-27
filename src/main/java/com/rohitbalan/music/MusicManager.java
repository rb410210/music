package com.rohitbalan.music;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class MusicManager 
{
    public static void main( String[] args )
    {
        for(String arg: args) {
        	try {
				List<Track> tracks = new Parser(arg).execute();
				
				for(final Track track: tracks) {
					/*new Thread(){
						@Override
						public void run() {
							try {
								Thread.sleep(10000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							new Downloader().download(track);
						}
						
					}.start();*/
					new Downloader().download(track);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
}
