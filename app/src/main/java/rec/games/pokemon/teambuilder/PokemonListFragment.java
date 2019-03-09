package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;

public class PokemonListFragment extends Fragment implements OnPokemonClickListener
{
	private static final String TAG = PokemonListFragment.class.getSimpleName();


	private PokeAPIViewModel mViewModel;
	private RecyclerView rv;

	private ProgressBar mLoadingPB;
	private TextView mLoadingErrorMsgTV;
	private LinearLayout mLoadingErrorLL;
	private Button mLoadingErrorBtn;

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
		mLoadingPB.setVisibility(View.VISIBLE);

		//loading error message
		mLoadingErrorMsgTV = view.findViewById(R.id.tv_loading_error);
		mLoadingErrorLL = view.findViewById(R.id.ll_loading_error);
		mLoadingErrorBtn = view.findViewById(R.id.btn_loading_error);

		final PokemonListAdapter adapter = new PokemonListAdapter(new LiveDataList<Pokemon>(), this);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mViewModel.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>()
		{
			@Override
			public void onChanged(@Nullable HashMap<Integer, LiveData<Pokemon>> pokemonCache)
			{
				if(pokemonCache == null)
				{
					Log.d(TAG, "Could not load PokemonList JSON");
					rv.setVisibility(View.INVISIBLE);

					mLoadingPB.setVisibility(View.INVISIBLE);
					mLoadingErrorMsgTV.setVisibility(View.VISIBLE);
					mLoadingErrorLL.setVisibility(LinearLayout.VISIBLE);
					mLoadingErrorBtn.setVisibility(View.VISIBLE);
					return;
				}
				else
				{
					mLoadingPB.setVisibility(View.INVISIBLE);
					mLoadingErrorMsgTV.setVisibility(View.INVISIBLE);
					mLoadingErrorLL.setVisibility(LinearLayout.INVISIBLE);
					mLoadingErrorBtn.setVisibility(View.INVISIBLE);

					rv.setVisibility(View.VISIBLE);
				}

				LiveDataList<Pokemon> pokemon = mViewModel.extractPokemonListFromCache();
				adapter.updatePokemon(pokemon);
			}
		});

		mLoadingErrorBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "Refreshing");
				mViewModel.getNewPokemonList();
			}
		});

		rv.setAdapter(adapter);

		return view;
	}

	//@Override
	public void onPokemonClicked(int position)
	{
		// TODO - open in new window
		Log.d(TAG, "Position: " + position);
		Intent intent = new Intent(getContext(), PokemonItemDetailActivity.class);
		intent.putExtra(PokeAPIUtils.POKE_ITEM, position); //temporary assignment
		startActivity(intent);
	}

}


