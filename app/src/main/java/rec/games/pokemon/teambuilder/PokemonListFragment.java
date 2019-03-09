package rec.games.pokemon.teambuilder;

import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;

public class PokemonListFragment extends Fragment implements PokemonListAdapter.OnPokemonClickListener
{
	private static final String TAG = PokemonListFragment.class.getSimpleName();

	private PokeAPIViewModel mViewModel;
	private RecyclerView listRV;

	private ProgressBar mLoadingPB;
	private TextView mLoadingErrorMsgTV;
	private LinearLayout mLoadingErrorLL;
	private Button mLoadingErrorBtn;
	private FloatingActionButton mListFAB;
	private String mTeamToAdd;
	private String searchTerm;

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

		listRV = view.findViewById(R.id.pokemon_list_rv);
		listRV.setLayoutManager(new LinearLayoutManager(getActivity()));
		listRV.setItemAnimator(new DefaultItemAnimator());

		mLoadingPB = view.findViewById(R.id.pb_loading_circle);
		mLoadingPB.setVisibility(View.VISIBLE);

		//loading error message
		mLoadingErrorMsgTV = view.findViewById(R.id.tv_loading_error);
		mLoadingErrorLL = view.findViewById(R.id.ll_loading_error);
		mLoadingErrorBtn = view.findViewById(R.id.btn_loading_error);
		mListFAB = view.findViewById(R.id.pokemon_list_FAB);
		mListFAB.hide();

		if (getArguments() != null){
			//Log.d(TAG, "Got arguments");
			mTeamToAdd = getArguments().getString(getString(R.string.team_id_string));
		}

		final PokemonListAdapter adapter = new PokemonListAdapter(new LiveDataList<Pokemon>(), this, this);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mViewModel.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>()
		{
			@Override
			public void onChanged(@Nullable HashMap<Integer, LiveData<Pokemon>> pokemonCache)
			{
				if(pokemonCache == null)
				{
					Log.d(TAG, "Could not load PokemonList JSON");
					listRV.setVisibility(View.INVISIBLE);

					mLoadingPB.setVisibility(View.INVISIBLE);
					mLoadingErrorMsgTV.setVisibility(View.VISIBLE);
					mLoadingErrorLL.setVisibility(LinearLayout.VISIBLE);
					mLoadingErrorBtn.setVisibility(View.VISIBLE);
					mListFAB.hide();
					return;
				}
				else
				{
					mLoadingPB.setVisibility(View.INVISIBLE);
					mLoadingErrorMsgTV.setVisibility(View.INVISIBLE);
					mLoadingErrorLL.setVisibility(LinearLayout.INVISIBLE);
					mLoadingErrorBtn.setVisibility(View.INVISIBLE);

					listRV.setVisibility(View.VISIBLE);
					mListFAB.show();
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

		mListFAB.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "Fab search clicked");
				searchForPokemon();
			}
		});
		listRV.addOnScrollListener(new RecyclerView.OnScrollListener()
		{
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
			{
				if ( dy > 0 || dy < 0 && mListFAB.isShown())
					mListFAB.hide();							//hide if scrolling
			}

			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
			{
				if (newState == RecyclerView.SCROLL_STATE_IDLE)
					mListFAB.show();
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

		listRV.setAdapter(adapter);

		return view;
	}

	//@Override
	public void onPokemonClicked(int pokemonID)
	{
		Log.d(TAG, "ID of "+ pokemonID+1);

		Intent intent = new Intent(getContext(), PokemonItemDetailActivity.class);
		intent.putExtra(PokeAPIUtils.POKE_ITEM, pokemonID+1); //assign id
		if(mTeamToAdd != null)
			intent.putExtra(getString(R.string.team_id_string), mTeamToAdd);
		startActivity(intent);
	}

	private void searchForPokemon(){
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.action_search_title);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		View itemView = inflater.inflate(R.layout.pokemon_list_search, null);
		builder.setView(itemView);
		final EditText userInputText = itemView.findViewById(R.id.pokemon_list_search);
		builder.setCancelable(true)
			.setPositiveButton(getString(R.string.action_search_submit), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					String input = userInputText.getText().toString();
					searchTerm = input;
					Log.d(TAG, "Searched for: " + input);
					if (!input.isEmpty())
					{
						if (input.matches("\\d+"))
						{
							int pokeSearchID = Integer.parseInt(input);
							Log.d(TAG, "Is int " + pokeSearchID);
						}
						else
							Log.d(TAG, "Is str");
					}
				}
			});
		builder.create().show();

	}
}


