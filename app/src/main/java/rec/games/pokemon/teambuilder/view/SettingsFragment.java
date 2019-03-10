package rec.games.pokemon.teambuilder.view;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import rec.games.pokemon.teambuilder.R;

public class SettingsFragment extends PreferenceFragmentCompat
	implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = SettingsFragment.class.getSimpleName(); //string name of class

	@Override
	public void onCreatePreferences(Bundle bundle, String s)
	{
		Log.d(TAG, "Inflated preferences");
		addPreferencesFromResource(R.xml.prefs);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		Log.d(TAG, "Settings updated");
	}

	@Override
	public void onResume()
	{
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this); //start listener
	}

	@Override
	public void onPause()
	{
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this); //stop listener
	}
}
