package com.mathias.android.acast.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.mathias.android.acast.podcast.Feed;
import com.mathias.android.acast.podcast.FeedItem;

public class RssUtil implements ContentHandler {
	
	private static final int BITMAP_MAX_WIDTH = 96;

	private static final int BITMAP_MAX_HEIGHT = 96;

	private static final String TAG = RssUtil.class.getSimpleName();
	
	private FeedItem currentFeedItem = new FeedItem();
	
	private String characters;
	
	private int level = 0;
	
	private Map<Integer, String> parent = new HashMap<Integer, String>();
	
	private Feed feed;
	
	private List<FeedItem> items;
	
	public RssUtil(){
	}
	
	public Map<Feed, List<FeedItem>> parse(String uri) throws ClientProtocolException, IOException,
			IllegalStateException, SAXException {
		Log.d(TAG, "Parsing: "+uri);
		feed = new Feed();
		items = new ArrayList<FeedItem>();
		feed.uri = uri;
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(new HttpGet(uri));
		HttpEntity entity = response.getEntity();
//		Encoding enc = Encoding.valueOf(entity.getContentEncoding().getValue());
		Xml.parse(entity.getContent(), Encoding.UTF_8, this);
		Map<Feed, List<FeedItem>> result = new HashMap<Feed, List<FeedItem>>();
		Collections.sort(items, ACastUtil.FEEDITEM_BYDATE);
		if(feed.pubdate == 0){
			if(items.size() > 0 && items.get(0).pubdate != 0){
				feed.pubdate = items.get(0).pubdate;
			}
		}
		result.put(feed, items);
		return result;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		StringBuilder sb = new StringBuilder();
		for (int i = start; i < length; i++) {
			char c = ch[i];
			if('\'' != c){
				sb.append(c);
			}
		}
		characters += sb.toString();
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		parent.put(level, localName);
		switch(level){
		case 3:
			if("enclosure".equalsIgnoreCase(localName)){
				for (int i = 0; i < atts.getLength(); i++) {
					String attsLocalName = atts.getLocalName(i);
					String value = atts.getValue(i);
					if("url".equalsIgnoreCase(attsLocalName)){
						currentFeedItem.mp3uri = value;
						File file = buildFile(value);
						currentFeedItem.mp3file = file.getAbsolutePath();
						currentFeedItem.downloaded = file.exists();
					}else if("length".equalsIgnoreCase(attsLocalName)){
						currentFeedItem.size = Long.parseLong(value.trim());
					}
				}
			}
			break;
		}
		level++;
		characters = "";
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		level--;
		String p = parent.get(level-1);
		switch(level){
		case 2:
			if("channel".equalsIgnoreCase(p)){
				if("title".equalsIgnoreCase(localName)){
					feed.title = characters;
				}else if(Util.isEmpty(uri) && "link".equalsIgnoreCase(localName)){
					feed.link = characters;
				}else if("pubDate".equalsIgnoreCase(localName)){
					try{
						feed.pubdate = new Date(characters).getTime();
					}catch(Exception e){
						Log.e(TAG, e.getMessage(), e);
					}
				}else if("category".equalsIgnoreCase(localName)){
					String category = feed.category;
					if(!Util.isEmpty(category)){
						feed.category = category + ", " + characters;
					}else if(!Util.isEmpty(characters)){
						feed.category = "Category: "+characters;
					}
				}else if("author".equalsIgnoreCase(localName)){
					//itunes:owner, itunes:author
					feed.author = characters;
				}else if("description".equalsIgnoreCase(localName)){
					feed.description = characters;
				}else if("item".equalsIgnoreCase(localName)){
					items.add(currentFeedItem);
					currentFeedItem = new FeedItem();
				}
			}
			break;
		case 3:
			if("image".equalsIgnoreCase(p)){
				if("url".equalsIgnoreCase(localName)){
					try {
						File file = buildFile(characters);
						Util.downloadFile(0, characters, file, null);
						String iconpath = scaleBitmap(file);
						Log.d(TAG, "Icon path: "+iconpath);
						feed.icon = iconpath;
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						throw new SAXException(e);
					}
				}
			}else if("item".equalsIgnoreCase(p)){
				if("title".equalsIgnoreCase(localName)){
					currentFeedItem.title = characters;
				}else if("link".equalsIgnoreCase(localName)){
					currentFeedItem.link = characters;
				}else if("pubDate".equalsIgnoreCase(localName)){
					try{
						currentFeedItem.pubdate = new Date(characters).getTime();
					}catch(Exception e){
						Log.e(TAG, e.getMessage(), e);
					}
				}else if("category".equalsIgnoreCase(localName)){
					String category = currentFeedItem.category;
					if(!Util.isEmpty(category)){
						currentFeedItem.category = category + ", " + characters;
					}else if(!Util.isEmpty(characters)){
						currentFeedItem.category = "Category: "+characters;
					}
				}else if("author".equalsIgnoreCase(localName)){
					//itunes:owner, itunes:author
					currentFeedItem.author = characters;
				}else if("comments".equalsIgnoreCase(localName)){
					currentFeedItem.comments = characters;
				}else if("description".equalsIgnoreCase(localName)){
					currentFeedItem.description = characters;
				}
			}
			break;
		}
		characters = "";
	}
	
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		//atom, http://www.w3.org/2005/Atom
		//Log.d(TAG, "startPrefixMapping() "+prefix+" "+uri);
		//startPrefixMapping() itunes http://www.itunes.com/dtds/podcast-1.0.dtd
		//startPrefixMapping() atom http://www.w3.org/2005/Atom
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		//Log.d(TAG, "endPrefixMapping() "+prefix);
		//endPrefixMapping() atom
		//endPrefixMapping() itunes
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		//String str = new String(ch).substring(start, length);
		//Log.d(TAG, "ignorableWhitespace() "+str+" "+start+" "+length);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		//Log.d(TAG, "processingInstruction() "+target+" "+data);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		//Log.d(TAG, "setDocumentLocator() "+locator);
		//setDocumentLocator() Locator[publicId: null, systemId: null, line: 1, column: 0]
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		//Log.d(TAG, "skippedEntity() "+name);
	}
	
	private static String scaleBitmap(File file){
		Bitmap bitmap = null;
		FileOutputStream out = null;
		String path = file.getAbsolutePath();
		try {
			bitmap = BitmapFactory.decodeFile(path);
			if(bitmap != null){
				int x = bitmap.getWidth();
				int y = bitmap.getHeight();
				int r = x/y;
				if(x <= BITMAP_MAX_WIDTH && y <= BITMAP_MAX_HEIGHT) {
					return path;
				}
				if(x > BITMAP_MAX_WIDTH) {
					x = BITMAP_MAX_WIDTH;
					y = y/r;
				}
				if(y > BITMAP_MAX_HEIGHT) {
					y = BITMAP_MAX_HEIGHT;
					x = x*r;
				}
				String newpath = path+".png";
				out = new FileOutputStream(newpath);
				Bitmap newbitmap = Bitmap.createScaledBitmap(bitmap, x, y, false);
				newbitmap.compress(CompressFormat.PNG, 80, out);
				newbitmap.recycle();
				Log.d(TAG, "x="+x+" y="+y+" path="+newpath);
				return newpath;
			}
		} catch (FileNotFoundException e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			if(bitmap != null){
				bitmap.recycle();
			}
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		Log.e(TAG, "Error: "+path);
		return path;
	}
	
	private File buildFile(String uri){
		String file = File.separator + "sdcard" + File.separator + "acast"
				+ File.separator + feed.title + File.separator
				+ new File(uri).getName();
		return new File(Util.escapeFilename(file));
	}

}
