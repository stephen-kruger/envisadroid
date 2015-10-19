package com.madibasoft.envisadroid.sync;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.BasicManagedEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.madibasoft.envisadroid.R;
import com.madibasoft.envisadroid.SettingsActivity;
import com.madibasoft.envisadroid.api.tpi.event.ZoneEvent;
import com.madibasoft.envisadroid.application.EnvisadroidApplication;
import com.madibasoft.envisadroid.log.LogActivity;
import com.madibasoft.envisadroid.util.Util;

@SuppressLint("DefaultLocale")
public class SyncHelper {
	public static final String SETTINGS_TITLE = "Envisadroid Zone Settings";
	public String deviceName;
	private SharedPreferences settings;

	public SyncHelper(Context c, String deviceName) {
		this.deviceName = deviceName.toLowerCase();
		settings = c.getSharedPreferences(SettingsActivity.ENVISA_PREFS, Context.MODE_PRIVATE);
	}

	public void sendSettings(Context c, JSONObject zoneData) {
		sendSettingsByMongoLabs(c,zoneData);
	}

	public JSONArray receiveSettings(Context c) {
		try {
			LogActivity.log(c, "Syncing settings for "+ deviceName);
			String url = "https://api.mongolab.com/api/1/databases/webhiker/collections/"+ deviceName.toLowerCase()+"?apiKey=ZhgXJYOCoR1h4LnU3hWIUgfGgnXcG_Om";
			HttpClient client = new DefaultHttpClient();
			HttpGet restApi = new HttpGet(url);
			HttpResponse response = client.execute(restApi);
			BasicManagedEntity entity = (BasicManagedEntity) response.getEntity();
			String content = Util.slurp(entity.getContent(),1024);
			LogActivity.log(c, "Received settings for "+ deviceName);
			JSONArray settings = new JSONArray(content);
			if (settings.length()==0)
				throw new RuntimeException("No settings found for "+ deviceName);
			LogActivity.log(c, "Found "+settings.length()+" settings versions for "+ deviceName);
			return settings;

		}
		catch (Throwable e) {
			e.printStackTrace();
			LogActivity.log(c, "Error reading settings "+e.toString());
			throw new RuntimeException(e);
		}
	}

	private void sendSettingsByMongoLabs(Context c, JSONObject zoneData) {
		//		.ajax( { url: "https://api.mongolab.com/api/1/databases/my-db/collections/my-coll?apiKey=myAPIKey",
		//	          data: JSON.stringify( { "x" : 1 } ),
		//	          type: "POST",
		//	          contentType: "application/json" } );
		try {
			DateFormat formatter = DateFormat.getDateTimeInstance(
                    DateFormat.SHORT, 
                    DateFormat.SHORT, 
                    Locale.getDefault());
			zoneData.put("date", formatter.format(new Date()));
			LogActivity.log(c, "Syncing settings for "+ deviceName);
			String url = "https://api.mongolab.com/api/1/databases/webhiker/collections/"+ deviceName.toLowerCase()+"?apiKey=ZhgXJYOCoR1h4LnU3hWIUgfGgnXcG_Om";
			HttpClient client = new DefaultHttpClient();
			HttpPost restApi = new HttpPost(url);restApi.setHeader("Content-Type", "application/json");
			restApi.setEntity(new StringEntity(zoneData.toString()));
			client.execute(restApi);
			LogActivity.log(c, "Sent settings for "+ deviceName);
		}
		catch (Throwable e) {
			LogActivity.log(c, "Error sending settings "+e.toString());
			throw new RuntimeException(e);
		}
	}

	/*
	 * Return any user configured customisations of zone names. Default zone names will not be returned to save on unneccesary
	 * settings preferences being stored.
	 */
	public JSONObject getSettingsData(Context c) throws JSONException {
		JSONObject zoneData = new JSONObject();
		String zoneName;
		// support partitions 1-4
		for (int j = 1; j < 5; j++) {
			// scroll through each zone
			for (int i = 1; i < 65; i++) {
				zoneName = getZoneName(c, new ZoneEvent(1,i,ZoneEvent.State.Restored));
				if (zoneName!=null)
					zoneData.put(Integer.toString(j)+':'+Integer.toString(i), zoneName);
			}
		}
		return zoneData;
	}

	public String setSettingsData(Context c, JSONObject zoneData) {
		try {
			String key, desc;
			for (int partition = 1; partition < 5; partition++) {
				for (int zone = 1; zone <65; zone++) {
					key = Integer.toString(partition)+':'+Integer.toString(zone);
					if (zoneData.has(key)) {
						desc = zoneData.getString(key);
						setZoneName(c, partition, zone, desc);
						Log.d(EnvisadroidApplication.LOG, "Found setting "+partition+" "+zone+" ("+desc+")");
					}
					else {
						Log.d(EnvisadroidApplication.LOG, "Missing entry for partition "+partition+" zone "+zone);	
					}
				}
			}
			Log.i("xxx", zoneData.toString());
			return "";
		}
		catch (Throwable t) {
			t.printStackTrace();
			return t.getMessage();
		}
	}

	public void setZoneName(Context c, int zonePartition, int zoneId, String zoneName) {
		// check bounds
		if ((zonePartition<1)||(zonePartition>4)||(zoneId<1)||(zoneId>=64)) 
			throw new RuntimeException("Invalid zone or partition specified partition="+zonePartition+" zone="+zoneId);
		String key = zonePartition+":"+zoneId;
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, zoneName);
		editor.commit();
	}

	public String getZoneName(Context c, ZoneEvent ze) {
		return getZoneName(ze.getPartition(),ze.getZone(),c.getString(R.string.zoneevent_zoneCountSingular)+" "+ze.getZone());
	}
	
//	public String getZoneName(Context c, ZoneEvent ze, String defaultValue) {
//		return getZoneName(ze.getPartition(),ze.getZone(),defaultValue);
//	}
	
	public String getZoneName(int partition, int zone, String defaultValue) {
		String key = partition+":"+zone;
		return settings.getString(key, defaultValue);
	}
	
	public static String getZoneName(SharedPreferences settings, int partition, int zone, String defaultValue) {
		String key = partition+":"+zone;
		return settings.getString(key, defaultValue);
	}

	/*
	 * Utility method to clear all stored zone name descriptions from the preferences where they are persisted.
	 */
	public void clearZoneNames(Context c) {
		SharedPreferences.Editor editor = settings.edit();
		for (int partition = 1; partition < 5; partition++) {
			for (int zone=1; zone<65;zone++) {
				editor.remove(ZoneEvent.class.getName()+partition+":"+zone);
			}
		}
		editor.commit();
	}

}
