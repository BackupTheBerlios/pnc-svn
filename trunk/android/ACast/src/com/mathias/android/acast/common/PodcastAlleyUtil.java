package com.mathias.android.acast.common;

import java.io.IOException;
import java.util.ArrayList;
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

import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

public class PodcastAlleyUtil implements ContentHandler {

	private static final String TAG = PodcastAlleyUtil.class.getSimpleName();
	
	private static final String URI_TOP_50 = "http://podcastalley.com/feeds/PodcastAlleyTop50.xml";

	private List<SearchItem> items = new ArrayList<SearchItem>();
	
	private SearchItem currentItem = new SearchItem("", "", "");

	private int level = 0;
	
	private String characters;
	
	private Map<Integer, String> parent = new HashMap<Integer, String>();

	public PodcastAlleyUtil(){
	}
	
	public List<SearchItem> parseTop50() throws ClientProtocolException, IOException,
			IllegalStateException, SAXException {
		Log.d(TAG, "Getting top 50 podcasts");

		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(new HttpGet(URI_TOP_50));
		HttpEntity entity = response.getEntity();
//		Encoding enc = Encoding.valueOf(entity.getContentEncoding().getValue());
		Xml.parse(entity.getContent(), Encoding.UTF_8, this);
		return items;
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
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		level++;
		parent.put(level, localName);
		characters = "";
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		level--;
		String p = parent.get(level);
		if(2 == level){
			if("channel".equalsIgnoreCase(p)){
				if("item".equalsIgnoreCase(localName)){
					items.add(currentItem);
					currentItem = new SearchItem("", "", "");
				}
			}
		}else if(3 == level){
			if("item".equalsIgnoreCase(p)){
				if("title".equalsIgnoreCase(localName)){
					currentItem.setTitle(characters);
				}else if("link".equalsIgnoreCase(localName)){
					currentItem.setUri(characters);
				}else if("description".equalsIgnoreCase(localName)){
					currentItem.setDescription(characters);
				}
			}
		}
		characters = "";
	}
	
	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		//atom, http://www.w3.org/2005/Atom
		Log.d(TAG, "startPrefixMapping() "+prefix+" "+uri);
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		Log.d(TAG, "endPrefixMapping() "+prefix);
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		String str = new String(ch).substring(start, length);
		Log.d(TAG, "ignorableWhitespace() "+str+" "+start+" "+length);
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		Log.d(TAG, "processingInstruction() "+target+" "+data);
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		Log.d(TAG, "setDocumentLocator() "+locator);
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		Log.d(TAG, "skippedEntity() "+name);
	}

	@Override
	public void startDocument() throws SAXException {
	}

	@Override
	public void endDocument() throws SAXException {
	}
	
}
