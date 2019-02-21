package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPokemonClickListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private PokeAPIViewModel mViewModel;
	private RecyclerView rv;

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

		List<Pokemon> tempPokemon = Arrays.asList(
			new Pokemon("bulbasaur"),
			new Pokemon("squirtle")
		);

		PokemonListAdapter adapter = new PokemonListAdapter(tempPokemon, this);
		rv = findViewById(R.id.pokemon_list);
		rv.setAdapter(adapter);
		rv.setLayoutManager(new LinearLayoutManager(this));
		rv.setItemAnimator(new DefaultItemAnimator());

		loadPokemonList();
	}

	public void loadPokemonList()
	{
		String pokemonListURL = PokeAPIUtils.buildPokemonListURL(10000, 0);
		Log.d(TAG, "URL: " + pokemonListURL);

		mViewModel.loadPokemonListJSON(pokemonListURL);
	}

	@Override
	public void onPokemonClicked(int position) {
		// TODO
	}
}
