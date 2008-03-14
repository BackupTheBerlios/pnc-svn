package com.mathias.drawutils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Audio {

	public static void play(String file){
	    try {
	        // From file
	        AudioInputStream stream = AudioSystem.getAudioInputStream(new File(file));
	    
	        // At present, ALAW and ULAW encodings must be converted
	        // to PCM_SIGNED before it can be played
	        AudioFormat format = stream.getFormat();
	        if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
	            format = new AudioFormat(
	                    AudioFormat.Encoding.PCM_SIGNED,
	                    format.getSampleRate(),
	                    format.getSampleSizeInBits()*2,
	                    format.getChannels(),
	                    format.getFrameSize()*2,
	                    format.getFrameRate(),
	                    true);        // big endian
	            stream = AudioSystem.getAudioInputStream(format, stream);
	        }
	    
	        // Create the clip
	        DataLine.Info info = new DataLine.Info(
	            Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
	        Clip clip = (Clip) AudioSystem.getLine(info);
	    
	        // This method does not return until the audio file is completely loaded
	        clip.open(stream);
	    
	        // Start playing
	        clip.start();
	    } catch (MalformedURLException e) {
	    	System.out.println(e.getMessage());
	    } catch (IOException e) {
	    	System.out.println(e.getMessage());
	    } catch (LineUnavailableException e) {
	    	System.out.println(e.getMessage());
	    } catch (UnsupportedAudioFileException e) {
	    	System.out.println(e.getMessage());
	    }		
	}
}
