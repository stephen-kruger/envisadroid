package com.madibasoft.envisadroid.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import com.madibasoft.envisadroid.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Util {

	public static String slurp(final InputStream is, final int bufferSize)
	{
		final char[] buffer = new char[bufferSize];
		final StringBuilder out = new StringBuilder();
		try {
			final Reader in = new InputStreamReader(is, "UTF-8");
			try {
				for (;;) {
					int rsz = in.read(buffer, 0, buffer.length);
					if (rsz < 0)
						break;
					out.append(buffer, 0, rsz);
				}
			}
			finally {
				in.close();
			}
		}
		catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return out.toString();
	}

	public static void dialog(Context c, String title, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setMessage(msg)
		.setTitle(title);
		builder.setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	//	public static void alert(Context context, String message) {
	//		AlertDialog.Builder builder = new AlertDialog.Builder(context);
	//		builder.setMessage(message)
	//		.setCancelable(false)
	//		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
	//			public void onClick(DialogInterface dialog, int id) {
	//			}
	//		});
	//		AlertDialog alert = builder.create();
	//		alert.show();
	//	}

	/**
	 * Returns MAC address of the given interface name.
	 * @param interfaceName eth0, wlan0 or NULL=use first interface 
	 * @return  mac address or empty string
	 */
	public static String getMACAddress() {
		String ipv4 = "192.168.1.123";
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					//                    System.out.println("ip1--:" + inetAddress);
					//                    System.out.println("ip2--:" + inetAddress.getHostAddress());

					// for getting IPV4 format
					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress())) {

						//                        String ip = inetAddress.getHostAddress().toString();
						//                        EditText tv = (EditText) findViewById(R.id.ipadd);
						//                        tv.setText(ip);
						// return inetAddress.getHostAddress().toString();
						return ipv4;
					}
				}
			}
		} catch (Exception ex) {
			Log.e("IP Address", ex.toString());
		}
		return ipv4;
	}

	public static String getPreference(Context c, String key, String defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		try {
			return sp.getString(key, ""+defValue);
		}
		catch (Throwable t) {
			return defValue;
		}
	}

	public static boolean getBoolPreference(Context c, String key) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		try {
			return sp.getBoolean(key, false);
		}
		catch (Throwable t) {
			return false;
		}
	}

	public static int getIntPreference(Context c, String key, int defValue) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		try {
			return Integer.parseInt(sp.getString(key, Integer.toString(defValue)));
		}
		catch (Throwable t) {
			return defValue;
		}
	}
}
