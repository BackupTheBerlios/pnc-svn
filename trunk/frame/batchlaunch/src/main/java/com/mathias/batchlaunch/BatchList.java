package com.mathias.batchlaunch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;

@SuppressWarnings("serial")
public class BatchList extends JList {
	
	private Map<String, BatchItem> batchItems = new HashMap<String, BatchItem>();
	
	public BatchList(){
		super();
	}

	public BatchList(BatchItem ... items){
		this();
		for (BatchItem item : items) {
			batchItems.put(item.getName(), item);
		}
		Collections.sort(Arrays.asList(items));
		setListData(items);
	}

	public void add(BatchItem item){
		batchItems.put(item.getName(), item);
		setListData(batchItems.keySet().toArray());
	}

	public BatchItem get(Object key){
		return batchItems.get(key);
	}

}
