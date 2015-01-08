package com.madibasoft.envisadroid.zone;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.madibasoft.envisadroid.R;
import com.madibasoft.envisadroid.SettingsActivity;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent.State;
import com.madibasoft.envisadroid.application.EnvisadroidApplication;
import com.madibasoft.envisadroid.sync.SyncHelper;
import com.madibasoft.envisadroid.util.ColloquialDateFormat;
import com.madibasoft.envisadroid.util.Util;

public class ZoneExpandableListAdapter extends BaseExpandableListAdapter implements ExpandableListAdapter {
	//	private static SharedPreferences settings;
	private SyncHelper sh;
	private Context context;
	private ZoneDataSource data;
	private ColloquialDateFormat dateFormatter;
	SimpleDateFormat formatter = new SimpleDateFormat("hh:mm MM/dd/yy",Locale.getDefault());

	public ZoneExpandableListAdapter(Context context, ZoneDataSource data) {
		this.context = context;
		this.data = data;
		dateFormatter = new ColloquialDateFormat(context);
		sh = new SyncHelper(context,Util.getPreference(context,SettingsActivity.HOSTNAME,"192.168.1.25"));
	}

	public Object getChild(int groupPosition, int childPosition) {
		ZoneEvent.State groupState = data.getGroupState(groupPosition);		
		ZoneEvent ze = data.getZoneAt(groupState,childPosition);
		return ze;
	}

	public long getChildId(int groupPosition, int childPosition) {
		return Integer.parseInt(Integer.toString(groupPosition+1)+Integer.toString(childPosition+1));
	}

	public int getChildrenCount(int groupPosition) {
		return data.getChildCount(data.getGroupState(groupPosition));
	}

	public View getStatusView(final ZoneEvent.State status, final int countVal) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vi= inflater.inflate(R.layout.group_listitem, null);
		vi.setPadding(50, 0, 0, 0);
		TextView title = (TextView)vi.findViewById(R.id.group_title); 
		title.setText(context.getString(getStateResourceString(status)));

		TextView count = (TextView)vi.findViewById(R.id.group_count);
		if (countVal==1)
			count.setText(countVal+" "+context.getString(R.string.zoneevent_zoneCountSingular));
		else
			count.setText(countVal+" "+context.getString(R.string.zoneevent_zoneCountPlural));

		ImageView thumb_image=(ImageView)vi.findViewById(R.id.group_image);
		Button action = (Button)vi.findViewById(R.id.group_action_button); 

		switch (status) {
		case Restored :
			thumb_image.setImageResource(R.drawable.ic_action_locked);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
			break;
		case Fault_Restored :
			thumb_image.setImageResource(R.drawable.ic_action_locked);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
			break;
		case Open :
			thumb_image.setImageResource(R.drawable.ic_warning);
			action.setVisibility(View.VISIBLE);
			action.setText("Bypass all");
			action.setOnClickListener(new OnClickListener() {

				
				public void onClick(View vw) {
					try {
						List<ZoneEvent> zone = data.getAllZones(status);
						int[] zones = new int[zone.size()];
						for (int i = 0; i < zones.length;i++)
							zones[i] = zone.get(i).getZone();
						((EnvisadroidApplication)context.getApplicationContext()).getSession().bypass(1, zones);
					} 
					catch (Throwable e) {
						e.printStackTrace();
					}
				}
			});
			break;
		case Fault :
			thumb_image.setImageResource(R.drawable.ic_error);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
			break;
		case Tamper :
			thumb_image.setImageResource(R.drawable.ic_error);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
			break;
		case Tamper_Restore :
			thumb_image.setImageResource(R.drawable.ic_action_report);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
			break;
		case Alarm :
			thumb_image.setImageResource(R.drawable.ic_error);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
			break;
		case Alarm_Restore :
			thumb_image.setImageResource(R.drawable.ic_action_report);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
			break;
		default : 
			thumb_image.setImageResource(R.drawable.ic_action_locked);
			action.setText("");
			action.setVisibility(View.INVISIBLE);
		}

		return vi;
	}

	private int getStateResourceString(State status) {
		switch (status) {
		case Restored : return R.string.Restored;
		case Fault_Restored : return R.string.Fault_Restored;
		case Open : return R.string.Open;
		case Fault : return R.string.Fault;
		case Tamper : return R.string.Tamper;
		case Tamper_Restore : return R.string.Tamper_Restore;
		case Alarm : return R.string.Alarm;
		case Alarm_Restore : return R.string.Alarm_Restore;
		default : return R.string.error;
		}
	}

	public View getEventView(final ZoneEvent ze) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View vi= inflater.inflate(R.layout.zoneevent_listitem, null);
		if (ze==null)
			return vi;
		vi.setPadding(15, 0, 0, 0);
		TextView title = (TextView)vi.findViewById(R.id.zoneevent_title); 
		title.setText(sh.getZoneName(context,ze));

		TextView partition = (TextView)vi.findViewById(R.id.zoneevent_zonepartition); 
		partition.setText(Integer.toString(ze.getPartition()));

		TextView zone = (TextView)vi.findViewById(R.id.zoneevent_zone); 
		zone.setText(Integer.toString(ze.getZone()));

		TextView zoneDate = (TextView)vi.findViewById(R.id.zoneevent_zonedate); 
		String dtStr = dateFormatter.format(ze.getZoneDate());
		switch (ze.getState()) {
		case Alarm: 
		case Tamper: 
		case Fault: 
		case Open: 
			zoneDate.setText(context.getString(R.string.last_seen_open)+" "+dtStr);
			break;
		case Fault_Restored:
		case Tamper_Restore: 
		case Alarm_Restore:
		case Restored :
			zoneDate.setText(context.getString(R.string.last_seen_closed)+" "+dtStr);
			break;
		}

		TextView eventDate = (TextView)vi.findViewById(R.id.zoneevent_eventdate);
		eventDate.setText(formatter.format(ze.getEventDate()));
		return vi;
	}

//	public String getZoneName(Context c, ZoneEvent ze) {
//		return getZoneName(c, ze, c.getString(R.string.zoneevent_zoneCountSingular)+" "+ze.getZone());
//	}
//
//	public String getZoneName(Context c, ZoneEvent ze, String defaultValue) {
//		//		if (settings==null)
//		//			settings = c.getSharedPreferences(SettingsActivity.ENVISA_PREFS, Context.MODE_PRIVATE);
//		//		return settings.getString(ZoneEvent.class.getName()+ze.getPartition()+":"+ze.getZone(), defaultValue);
//		String key = ze.getPartition()+":"+ze.getZone();
//		if (zoneNames.has(key)) {
//			try {
//				return zoneNames.getString(key);
//			} 
//			catch (JSONException e) {
//				e.printStackTrace();
//			}
//		}
//		return defaultValue;
//	}

	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		View textView = getEventView((ZoneEvent)getChild(groupPosition, childPosition));
		return textView;
	}

	public Object getGroup(int groupPosition) {
		return data.getGroup(groupPosition);
	}

	public List<ZoneEvent> getGroupChildren(int groupPosition) {
		List<ZoneEvent> children = data.getAllZones(data.getGroupState(groupPosition));
		return children;
	}

	public int getGroupCount() {
		return data.getGroupCount();
	}

	public long getGroupId(int groupPosition) {
		return groupPosition+999;
	}

	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		View textView = getStatusView(data.getGroupState(groupPosition),getChildrenCount(groupPosition));
		return textView;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

	public boolean hasStableIds() {
		return true;
	}
}
