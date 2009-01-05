package com.mathias.android.acast.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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

	public List<SearchItem> parse(File file) throws ClientProtocolException,
			IOException, IllegalStateException, SAXException {
		Log.d(TAG, "Parsing: " + file.getAbsolutePath());
		Xml.parse(new FileInputStream(file), Encoding.UTF_8, this);
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

	// EXPORT
	public static String exportOpml(Opml inp){
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
		sb.append("<opml version=\"1.1\">");
		sb.append(" <head>");
		sb.append("  <title>"+inp.title+"</title>");
		sb.append("  <dateCreated>"+new Date()+"</dateCreated>");
		sb.append("  <dateModified>"+new Date()+"</dateModified>");
		sb.append("  <ownerName>"+inp.ownerName+"</ownerName>");
		sb.append("  <ownerEmail>"+inp.ownerEmail+"</ownerEmail>");
		sb.append("  <expansionState></expansionState>");
		sb.append("  <vertScrollState>1</vertScrollState>");
		sb.append("  <windowTop>20</windowTop>");
		sb.append("  <windowLeft>0</windowLeft>");
		sb.append("  <windowBottom>120</windowBottom>");
		sb.append("  <windowRight>147</windowRight>");
		sb.append(" </head>");
		sb.append(" <body>");
		for (OpmlItem item : inp.items) {
			sb.append("  <outline text=\""+item.text+"\" count=\""+item.count+"\" xmlUrl=\""+item.xmlUri+"\"/>");
		}
		sb.append(" </body>");
		sb.append("</opml>");
		return sb.toString();
	}

	public static class Opml {
		String title;
		String ownerName;
		String ownerEmail;
		List<OpmlItem> items = new ArrayList<OpmlItem>();

		public Opml(String title) {
			this.title = title;
			ownerName = "";
			ownerEmail = "";
		}

		public Opml(String title, String ownerName, String ownerEmail) {
			this.title = title;
			this.ownerName = ownerName;
			this.ownerEmail = ownerEmail;
		}

		public void add(OpmlItem item) {
			items.add(item);
		}
	}

	public static class OpmlItem {
		String text;
		String count;
		String xmlUri;

		public OpmlItem(String text, String xmlUri) {
			this.text = text;
			this.count = "100";
			this.xmlUri = xmlUri;
		}

		public OpmlItem(String text, String count, String xmlUri) {
			this.text = text;
			this.count = count;
			this.xmlUri = xmlUri;
		}
	}

}
