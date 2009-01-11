package com.mathias.android.acast.common;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChoiceArrayAdapter<T> extends ArrayAdapter<T> {
	
	private static final String TAG = ChoiceArrayAdapter.class.getSimpleName();

    private int mFieldId = 0;
    private LayoutInflater mInflater;
    private int mResource;
    private String mChoiceRenderer;

    public ChoiceArrayAdapter(Context context, int textViewResourceId,
			List<T> objects, String choiceRenderer) {
		super(context, textViewResourceId, objects);
        mResource = textViewResourceId;
        mFieldId = 0;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mChoiceRenderer = choiceRenderer;
	}

    public ChoiceArrayAdapter(Context context, int resource, int textViewResourceId,
			List<T> objects, String choiceRenderer) {
		super(context, resource, textViewResourceId, objects);
        mFieldId = textViewResourceId;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mResource = resource;
        mChoiceRenderer = choiceRenderer;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View view;
		TextView text;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}

		try {
			if (mFieldId == 0) {
				// If no custom field is assigned, assume the whole resource is
				// a TextView
				text = (TextView) view;
			} else {
				// Otherwise, find the TextView field within the layout
				text = (TextView) view.findViewById(mFieldId);
			}
		} catch (ClassCastException e) {
			Log.e("ArrayAdapter",
					"You must supply a resource ID for a TextView");
			throw new IllegalStateException(
					"ArrayAdapter requires the resource ID to be a TextView", e);
		}

		text.setText(getProperty(getItem(position)));

		return view;
	}

	private String getProperty(Object obj){
		Field[] fields = obj.getClass().getFields();
		for (Field field : fields) {
			String name = field.getName();
			if(name.equalsIgnoreCase(mChoiceRenderer)){
				try {
					return field.get(obj).toString();
				} catch (IllegalArgumentException e) {
					Log.e(TAG, e.getMessage(), e);
				} catch (IllegalAccessException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		Method[] methods = obj.getClass().getMethods();
		for (Method method : methods) {
			String name = method.getName();
			if(name.equalsIgnoreCase("get"+mChoiceRenderer)){
				try {
					Object ret = method.invoke(obj, new Object[0]);
					return ret.toString();
				} catch (IllegalArgumentException e) {
					Log.e(TAG, e.getMessage(), e);
				} catch (IllegalAccessException e) {
					Log.e(TAG, e.getMessage(), e);
				} catch (InvocationTargetException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}
		return obj.toString();
	}

}
