package com.madibasoft.envisadroid;

import android.annotation.TargetApi;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
	public static final String ENVISA_PREFS = "com.madibasoft.envisadroid.preferences";
	public static final String PANEL = "panelPref";
	public static final String HOSTNAME = "hostname";
	public static final String PORT = "port";
	public static final String ENVISAUSER = "envisauser";
	public static final String ENVISAPASSWORD = "envisapassword";
	public static final String PASSCODE = "passcode";
	public static final String AUTOCONNECT = "autoconnect";
	//	/**
	//	 * Determines whether to always show the simplified settings UI, where
	//	 * settings are presented in a single list. When false, settings are shown
	//	 * as a master/detail two-pane view on tablets. When true, a single pane is
	//	 * shown on tablets.
	//	 */
	//	private static final boolean ALWAYS_SIMPLE_PREFS = true;

	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
				setupActionBar();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		private void setupActionBar() {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				// Show the Up button in the action bar.
				getActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}

	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		setupSimplePreferencesScreen();
	}

	/**
	 * Shows the simplified settings UI if the device configuration if the
	 * device configuration dictates that a simplified, single-pane UI should be
	 * shown.
	 */
	@SuppressWarnings("deprecation")
	private void setupSimplePreferencesScreen() {
		//		if (!isSimplePreferences(this)) {
		//			return;
		//		}

		// In the simplified UI, fragments are not used at all and we instead
		// use the older PreferenceActivity APIs.

		// Add 'general' preferences.
		addPreferencesFromResource(R.xml.pref_general);

		// Add security preferences, and a corresponding header.
		PreferenceCategory securityHeader = new PreferenceCategory(this);
		securityHeader = new PreferenceCategory(this);
		securityHeader.setTitle(R.string.pref_header_security);
		getPreferenceScreen().addPreference(securityHeader);
		addPreferencesFromResource(R.xml.pref_security);

		// Add security preferences, and a corresponding header.
		PreferenceCategory miscHeader = new PreferenceCategory(this);
		miscHeader = new PreferenceCategory(this);
		miscHeader.setTitle(R.string.pref_header_misc);
		getPreferenceScreen().addPreference(miscHeader);
		addPreferencesFromResource(R.xml.pref_misc);
	}

	//	/** {@inheritDoc} */
	//	
	//	public boolean onIsMultiPane() {
	//		return isXLargeTablet(this) && !isSimplePreferences(this);
	//	}

	//	/**
	//	 * Helper method to determine if the device has an extra-large screen. For
	//	 * example, 10" tablets are extra-large.
	//	 */
	//	@SuppressLint("InlinedApi")
	//	private static boolean isXLargeTablet(Context context) {
	//		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	//	}

	//	/**
	//	 * Determines whether the simplified settings UI should be shown. This is
	//	 * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
	//	 * doesn't have newer APIs like {@link PreferenceFragment}, or the device
	//	 * doesn't have an extra-large screen. In these cases, a single-pane
	//	 * "simplified" settings UI should be shown.
	//	 */
	//	private static boolean isSimplePreferences(Context context) {
	//		return ALWAYS_SIMPLE_PREFS
	//				|| Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
	//				|| !isXLargeTablet(context);
	//	}

	//	/** {@inheritDoc} */
	//	
	//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	//	public void onBuildHeaders(List<Header> target) {
	//		if (!isSimplePreferences(this)) {
	//			loadHeadersFromResource(R.xml.pref_headers, target);
	//		}
	//	}

	/**
	 * A preference value change listener that updates the preference's summary
	 * to reflect its new value.
	 */
	@SuppressWarnings("unused")
	private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
		
		public boolean onPreferenceChange(Preference preference, Object value) {
			//			String stringValue = value.toString();
			return true;
		}
	};

	//	/**
	//	 * This fragment shows general preferences only. It is used when the
	//	 * activity is showing a two-pane settings UI.
	//	 */
	//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	//	public static class GeneralPreferenceFragment extends PreferenceFragment {
	//		
	//		public void onCreate(Bundle savedInstanceState) {
	//			super.onCreate(savedInstanceState);
	//			addPreferencesFromResource(R.xml.pref_general);
	//		}
	//	}

	//	/**
	//	 * This fragment shows data and sync preferences only. It is used when the
	//	 * activity is showing a two-pane settings UI.
	//	 */
	//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	//	public static class SecurityPreferenceFragment extends PreferenceFragment {
	//		
	//		public void onCreate(Bundle savedInstanceState) {
	//			super.onCreate(savedInstanceState);
	//			addPreferencesFromResource(R.xml.pref_security);
	//		}
	//	}
}
