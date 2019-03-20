package rec.games.pokemon.teambuilder.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import rec.games.pokemon.teambuilder.R;

public class SettingsActivity extends AppCompatActivity
{

	private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		if(getSupportActionBar() != null)
		{
			getSupportActionBar().setDisplayHomeAsUpEnabled(true); //show back button
			getSupportActionBar().setTitle("PokéSettings");
		}
	}
}
