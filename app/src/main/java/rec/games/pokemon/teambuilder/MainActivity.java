package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
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

		final PokemonListAdapter adapter = new PokemonListAdapter(new ArrayList<Pokemon>(), this);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mViewModel.getPokeListJSON().observe(this, new Observer<String>()
		{
			@Override
			public void onChanged(@Nullable String pokemonListJSON)
			{
				if(pokemonListJSON == null)
				{
					Log.d(TAG, "Could not load PokemonList JSON");
					return;
				}
				//Log.d(TAG, "JSON: " + pokemonListJSON);
				PokeAPIUtils.NamedAPIResourceList apiPokemonList = PokeAPIUtils.parsePokemonListJSON(pokemonListJSON);
				//Log.d(TAG, apiPokemonList.toString());
				int limit = PokeAPIUtils.getPokeId(apiPokemonList.results[apiPokemonList.results.length-1].url);
				int lastPoke = apiPokemonList.results.length - (limit - 10_000);
				Log.d(TAG, "Count is: " + apiPokemonList.count + " of " + limit + " Last ID = " + lastPoke);

				List<Pokemon> pokemon = new ArrayList<>();
				for(PokeAPIUtils.NamedAPIResource r : apiPokemonList.results)
				{
					Pokemon p = new DeferredPokemonResource(PokeAPIUtils.getPokeId(r.url), r.name, r.url);
					pokemon.add(p);
				}
				adapter.updatePokemon(pokemon);
			}
		});


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
	public void onPokemonClicked(int position)
	{
		// TODO
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
