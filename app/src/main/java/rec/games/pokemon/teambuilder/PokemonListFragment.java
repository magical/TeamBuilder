package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class PokemonListFragment extends Fragment implements OnPokemonClickListener
{
	private static final String TAG = PokemonListFragment.class.getSimpleName();


	private PokeAPIViewModel mViewModel;
	private RecyclerView rv;

	private ProgressBar mLoadingPB;
	private TextView mLoadingErrorMsgTV;

	public PokemonListFragment()
	{

	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.pokemon_list, container, false);

		RequestOptions requestOptions = new RequestOptions()
			.placeholder(R.drawable.ic_poke_unknown)
			.error(R.drawable.ic_poke_unknown)
			.fallback(R.drawable.ic_poke_unknown)
			.diskCacheStrategy(DiskCacheStrategy.ALL);
		GlideApp.with(this).setDefaultRequestOptions(requestOptions);

		rv = view.findViewById(R.id.pokemon_list_rv);
		rv.setLayoutManager(new LinearLayoutManager(getActivity()));
		rv.setItemAnimator(new DefaultItemAnimator());

		mLoadingPB = view.findViewById(R.id.pb_loading_circle);
		mLoadingErrorMsgTV = view.findViewById(R.id.tv_loading_error);
		mLoadingPB.setVisibility(View.VISIBLE);

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

		rv.setAdapter(adapter);

		loadPokemonList();

		return view;
	}

	public void loadPokemonList()
	{
		String pokemonListURL = PokeAPIUtils.buildPokemonListURL(10000, 0);
		Log.d(TAG, "URL: " + pokemonListURL);

		mViewModel.loadPokemonListJSON(pokemonListURL);
	}

	//@Override
	public void onPokemonClicked(int position)
	{
		// TODO - open in new window
		Intent intent = new Intent(getContext(), PokemonItemDetailActivity.class);
		startActivity(intent);
	}

}


