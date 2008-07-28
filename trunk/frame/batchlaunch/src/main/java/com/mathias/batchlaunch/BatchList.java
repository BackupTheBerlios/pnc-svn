package com.mathias.batchlaunch;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JList;

@SuppressWarnings("serial")
public class BatchList extends JList {
	
	private Map<String, BatchItem> items = new HashMap<String, BatchItem>();
	
	public BatchList(){
		super();
	}

	public BatchList(BatchItem ... items){
		this();
		for (BatchItem item : items) {
			this.items.put(item.getName(), item);
		}
		setListData(items);
	}

	public void add(BatchItem item){
		items.put(item.getName(), item);
		setListData(items.keySet().toArray());
	}

	public BatchItem get(Object key){
		return items.get(key);
	}

}
