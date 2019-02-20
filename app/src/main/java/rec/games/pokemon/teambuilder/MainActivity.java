package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private PokeAPIViewModel mViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mViewModel.getPokeListJSON().observe(this, new Observer<String>()
		{
			@Override
			public void onChanged(@Nullable String pokemonListJSON)
			{
				if(pokemonListJSON != null)
				{
					Log.d(TAG, "JSON: " + pokemonListJSON);
					PokeAPIUtils.NamedAPIResourceList pokemonList = PokeAPIUtils.parsePokemonListJSON(pokemonListJSON);
					Log.d(TAG, pokemonList.toString());
				}
				else
				{
					Log.d(TAG, "Could not load PokemonList JSON");
				}
			}
		});

		loadPokemonList();
	}

	public void loadPokemonList()
	{
		String pokemonListURL = PokeAPIUtils.buildPokemonListURL(10000, 0);
		Log.d(TAG, "URL: " + pokemonListURL);

		mViewModel.loadPokemonListJSON(pokemonListURL);
	}
}
