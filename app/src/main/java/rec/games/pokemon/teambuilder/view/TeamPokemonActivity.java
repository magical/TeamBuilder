package rec.games.pokemon.teambuilder.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.model.Team;

public class TeamPokemonActivity extends AppCompatActivity
{
	private static final String TAG = TeamPokemonActivity.class.getSimpleName();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle bundle = new Bundle();
		Intent intent = getIntent();
		if(intent != null && intent.hasExtra(Team.TEAM_ID))
		{
			//Log.d(TAG, "Passing Team info");
			bundle.putInt(Team.TEAM_ID, intent.getIntExtra(Team.TEAM_ID, 0));
		}

		setContentView(R.layout.activity_team_pokemon_list);
		//ProgressBar mLoadingPB = onCreateView().findViewById()
		setTitle(getString(R.string.action_select_poke));
		if(getSupportActionBar() != null)                //no null pointer exception
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		PokemonListFragment pokemonListFragment = new PokemonListFragment();
		pokemonListFragment.setArguments(bundle);
		FragmentTransaction listFragment = getSupportFragmentManager().beginTransaction();
		listFragment.replace(R.id.team_pokemon_list_fragment, pokemonListFragment);
		listFragment.commit();
	}
}
