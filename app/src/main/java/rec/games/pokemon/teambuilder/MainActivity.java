package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
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
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPokemonClickListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private PokeAPIViewModel mViewModel;
	private RecyclerView rv;

	private ProgressBar mLoadingPB;
	private TextView mLoadingErrorMsgTV;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RequestOptions requestOptions = new RequestOptions()
			.placeholder(R.drawable.ic_poke_unknown)
			.error(R.drawable.ic_poke_unknown)
			.fallback(R.drawable.ic_poke_unknown)
			.diskCacheStrategy(DiskCacheStrategy.ALL);
		GlideApp.with(this).setDefaultRequestOptions(requestOptions);

		mLoadingPB = findViewById(R.id.pb_loading_circle);
		mLoadingErrorMsgTV = findViewById(R.id.tv_loading_error);
		mLoadingPB.setVisibility(View.VISIBLE);

		final PokemonListAdapter adapter = new PokemonListAdapter(new ArrayList<Pokemon>(), this);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mViewModel.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>()
		{
			@Override
			public void onChanged(@Nullable HashMap<Integer, LiveData<Pokemon>> pokemonCache)
			{
				if(pokemonCache == null)
				{
					Log.d(TAG, "Could not load PokemonList JSON");
					mLoadingPB.setVisibility(View.INVISIBLE);
					mLoadingErrorMsgTV.setVisibility(View.VISIBLE);
					rv.setVisibility(View.INVISIBLE);
					return;
				}
				else
				{
					mLoadingPB.setVisibility(View.INVISIBLE);
					mLoadingErrorMsgTV.setVisibility(View.INVISIBLE);
					rv.setVisibility(View.VISIBLE);
				}

				List<Pokemon> pokemon = mViewModel.extractPokemonListFromCache();
				adapter.updatePokemon(pokemon);
			}
		});

		rv = findViewById(R.id.pokemon_list);
		rv.setAdapter(adapter);
		rv.setLayoutManager(new LinearLayoutManager(this));
		rv.setItemAnimator(new DefaultItemAnimator());
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