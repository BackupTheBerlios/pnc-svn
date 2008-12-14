package com.mathias.android.acast.rss;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;

public class RssUtil implements ContentHandler {

	private static final String TAG = RssUtil.class.getSimpleName();
	
	private Feed feed;
	
	private FeedItem currentFeedItem = new FeedItem();
	
	private String characters;
	
	private int level = 0;

	public RssUtil(){
	}
	
	public Feed parse(String uri){
		feed = new Feed();
		feed.setUri(uri);
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			HttpResponse response = client.execute(new HttpGet(uri));
			HttpEntity entity = response.getEntity();
//			Encoding enc = Encoding.valueOf(entity.getContentEncoding().getValue());
			Xml.parse(entity.getContent(), Encoding.UTF_8, this);
		} catch (SAXException e) {
			Log.e(TAG, e.getMessage());
		} catch (IllegalStateException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		return feed;
	}

	@Override
	public void characters(char[] buff, int start, int end) throws SAXException {
		characters = new String(buff).substring(start, end);
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startElement(String arg0, String name, String arg2,
			Attributes attr) throws SAXException {
		switch(level){
		case 3:
			if("enclosure".equalsIgnoreCase(name)){
				for (int i = 0; i < attr.getLength(); i++) {
					String localName = attr.getLocalName(i);
					String value = attr.getValue(i);
					if("url".equalsIgnoreCase(localName)){
						currentFeedItem.setMp3uri(value);
					}else if("length".equalsIgnoreCase(localName)){
						currentFeedItem.setSize(Long.parseLong(value));
					}
				}
			}
			break;
		}
		level++;
	}

	@Override
	public void endElement(String arg0, String name, String arg2)
			throws SAXException {
		level--;
		switch(level){
		case 2:
			if("title".equalsIgnoreCase(name)){
				feed.setTitle(characters);
			}else if("item".equalsIgnoreCase(name)){
				feed.addItem(currentFeedItem);
				currentFeedItem = new FeedItem();
			}
			break;
		case 3:
			if("title".equalsIgnoreCase(name)){
				currentFeedItem.setTitle(characters);
			}
			break;
		}
	}

	@Override
	public void startPrefixMapping(String arg0, String arg1)
			throws SAXException {
		//atom, http://www.w3.org/2005/Atom
		Log.d(TAG, "startPrefixMapping() "+arg0+" "+arg1);
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		Log.d(TAG, "endPrefixMapping() "+arg0);
	}

	@Override
	public void ignorableWhitespace(char[] buff, int start, int end)
			throws SAXException {
		String str = new String(buff).substring(start, end);
		Log.d(TAG, "ignorableWhitespace() "+str+" "+start+" "+end);
	}

	@Override
	public void processingInstruction(String arg0, String arg1)
			throws SAXException {
		Log.d(TAG, "processingInstruction() "+arg0+" "+arg1);
	}

	@Override
	public void setDocumentLocator(Locator arg0) {
		Log.d(TAG, "setDocumentLocator() "+arg0);
	}

	@Override
	public void skippedEntity(String arg0) throws SAXException {
		Log.d(TAG, "skippedEntity() "+arg0);
	}

}
