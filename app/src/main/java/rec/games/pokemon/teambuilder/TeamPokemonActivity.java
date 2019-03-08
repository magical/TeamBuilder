package rec.games.pokemon.teambuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

public class TeamPokemonActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//Fragment newFragment = new PokemonListFragment();
		setContentView(R.layout.activity_team_pokemon_list);
		//ProgressBar mLoadingPB = onCreateView().findViewById()
		setTitle(getString(R.string.action_select_poke));
		if(getSupportActionBar() != null) 				//no null pointer exception
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}
}
