package com.cameleon.common.android.adapter;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class BaseImageAdapter extends ArrayAdapter<Integer> {

	private Activity context;
	private int resource;
	private int textViewResourceId;
	private ViewBinder viewBinder;

	public BaseImageAdapter(Activity context, int resource, int textViewResourceId, Integer[] objects) {
		super(context, resource, textViewResourceId, objects);
		this.context = context;
		this.resource = resource;
		this.textViewResourceId = textViewResourceId;
	}

	public BaseImageAdapter(Activity context, int resource, int textViewResourceId, List<Integer> objects) {
		super(context, resource, textViewResourceId, objects);
		this.context = context;
		this.resource = resource;
		this.textViewResourceId = textViewResourceId;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Integer iDrawable = getItem(position);
		View rowView = convertView;
		if (rowView == null) {
			LayoutInflater inflater = context.getLayoutInflater();
			rowView = inflater.inflate(resource, null);
		}
		if (viewBinder!=null) {
			viewBinder.setViewValue(position, rowView);
		}

		ImageView ivStatus = (ImageView) rowView.findViewById(textViewResourceId);
		ivStatus.setImageDrawable(context.getResources().getDrawable(iDrawable));

		return rowView;
	}
	
	public ViewBinder getViewBinder() {
		return viewBinder;
	}

	public void setViewBinder(ViewBinder viewBinder) {
		this.viewBinder = viewBinder;
	}

	public static interface ViewBinder {
		boolean setViewValue(int position, View view);
	}
}