package com.rohitbalan.music;

import java.io.IOException;
import java.util.List;

/**
 * Hello world!
 *
 */
public class MusicManager 
{
    public static void main(String[] args)
    {
    	if(args!=null) {
            for(String arg: args) {
            	try {
    				List<Track> tracks = new Parser(arg).execute();
    				
    				for(final Track track: tracks) {
    					new Downloader().download(track);
    				}
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
            }
    	}
		System.out.println("Exiting");
    }
}
