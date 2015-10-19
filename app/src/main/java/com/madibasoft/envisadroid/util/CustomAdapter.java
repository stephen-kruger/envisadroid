package com.madibasoft.envisadroid.util;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.madibasoft.envisadroid.R;

public class CustomAdapter extends ArrayAdapter<String> {

	private final String[] itemname;
	private final Integer[] imgid;
	private boolean enabledList[];
	private int layout;
	private View rowView;
	private LayoutInflater inflater;

	public CustomAdapter(Activity context, int layout, String[] itemname, Integer[] imgid) {
		super(context, layout, itemname);
		this.itemname=itemname;
		this.imgid=imgid;
		this.layout=layout;
		inflater=context.getLayoutInflater();
		enabledList = new boolean[itemname.length];
		for (int i = 0; i < itemname.length; i++)
			enabledList[i] = true;
	}

	public View getView(int position, View view, ViewGroup parent) {
		if (view==null)
			rowView=inflater.inflate(layout, null, true);
		else
			rowView = view;

		TextView txtTitle = (TextView) rowView.findViewById(R.id.drawerText);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.drawerIcon);

		txtTitle.setText(itemname[position]);
		imageView.setImageResource(imgid[position]);
		
		if (enabledList[position]) {
			txtTitle.setTextColor(parent.getResources().getColor(R.color.dark_text));
		}
		else {
			txtTitle.setTextColor(parent.getResources().getColor(R.color.light_text));
		}
		rowView.setEnabled(enabledList[position]);
		return rowView;

	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return enabledList[position];
	}
	
	public void setEnabled(int position, boolean enabled) {
		enabledList[position] = enabled;
	}
	
}
