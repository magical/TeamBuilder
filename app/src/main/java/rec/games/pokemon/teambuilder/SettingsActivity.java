package rec.games.pokemon.teambuilder;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.ViewParent;
import android.widget.LinearLayout;

public class SettingsActivity extends AppCompatActivity
{

	private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Log.d(TAG, "Creating toolbar");

		if(getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show back button
			getSupportActionBar().setTitle("PokéSettings");
		}
	}
}
