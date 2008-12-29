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

public class OpmlUtil implements ContentHandler {

	private static final String TAG = OpmlUtil.class.getSimpleName();
	
	private List<SearchItem> items = new ArrayList<SearchItem>();
	
	private SearchItem currentItem = new SearchItem("", "", "");

	private int level = 0;
	
	private Map<Integer, String> parent = new HashMap<Integer, String>();

	public OpmlUtil(){
	}
	
	public List<SearchItem> parse(String uri) throws ClientProtocolException, IOException,
			IllegalStateException, SAXException {
		Log.d(TAG, "Parsing: "+uri);
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(new HttpGet(uri));
		HttpEntity entity = response.getEntity();
//		Encoding enc = Encoding.valueOf(entity.getContentEncoding().getValue());
		Xml.parse(entity.getContent(), Encoding.UTF_8, this);
		return items;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
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
		String p = parent.get(level);
		level++;
		parent.put(level, localName);
		switch(level){
		case 3:
			if("body".equalsIgnoreCase(p)){
				if("outline".equalsIgnoreCase(localName)){
					for (int i = 0; i < atts.getLength(); i++) {
						String attsLocalName = atts.getLocalName(i);
						String value = atts.getValue(i);
						if("text".equalsIgnoreCase(attsLocalName)){
							currentItem.setTitle(value);
						}else if("xmlUrl".equalsIgnoreCase(attsLocalName)){
							currentItem.setUri(value);
						}
					}
					items.add(currentItem);
					currentItem = new SearchItem("", "", "");
				}
			}
			break;
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		level--;
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

}
