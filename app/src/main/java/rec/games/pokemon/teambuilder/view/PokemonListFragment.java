package rec.games.pokemon.teambuilder.view;

import android.app.AlertDialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

public class PokemonListFragment extends Fragment
	implements PokemonListAdapter.OnPokemonClickListener, SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = PokemonListFragment.class.getSimpleName();

	private PokeAPIViewModel mViewModel;
	private RecyclerView listRV;
	private Parcelable mListRVState;
	private PokemonListAdapter mPokemonListAdapter;

	private ProgressBar mLoadingPB;
	private TextView mLoadingErrorMsgTV;
	private LinearLayout mLoadingErrorLL;
	private Button mLoadingErrorBtn;
	private FloatingActionButton mListFAB;
	private int mTeamToAdd;
	private String searchTerm;

	private SharedPreferences preferences;

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
		mLoadingErrorMsgTV.setVisibility(View.GONE);
		mLoadingErrorLL.setVisibility(LinearLayout.GONE);
		mLoadingErrorBtn.setVisibility(View.GONE);
		mListFAB.hide();

		if(getArguments() != null)
		{
			//Log.d(TAG, "Got arguments");
			mTeamToAdd = getArguments().getInt(Team.TEAM_ID, 0);
		}

		preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		preferences.registerOnSharedPreferenceChangeListener(this);

		mPokemonListAdapter = new PokemonListAdapter(new LiveDataList<Pokemon>(), this);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mViewModel.getPokemonListCache().observe(this, new Observer<Boolean>()
		{
			@Override
			public void onChanged(@Nullable Boolean listStatus)
			{
				if(listStatus == null)
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
				mPokemonListAdapter.updatePokemon(pokemon);
				sortPokemonList();
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
				if(searchTerm==null)
					searchForPokemon();
				else
					clearSearch();
			}
		});
		listRV.addOnScrollListener(new RecyclerView.OnScrollListener()
		{
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
			{
				if(dy > 0 || dy < 0 && mListFAB.isShown())
					mListFAB.hide();                            //hide if scrolling
			}

			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
			{
				if(newState == RecyclerView.SCROLL_STATE_IDLE)
					mListFAB.show();
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

		listRV.setAdapter(mPokemonListAdapter);

		return view;
	}

	//@Override
	public void onPokemonClicked(int pokemonID)
	{
		Log.d(TAG, "ID of " + pokemonID);

		Intent intent = new Intent(getContext(), PokemonItemDetailActivity.class);
		intent.putExtra(PokeAPIUtils.POKE_ITEM, pokemonID); //assign id
		if(mTeamToAdd > 0)
		{
			intent.putExtra(Team.TEAM_ID, mTeamToAdd);
			intent.putExtra(TeamListFragment.TEAM_MOVE_ENABLE, true); //allow access to change moves
		}
		startActivity(intent);
	}

	private void searchForPokemon()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		builder.setTitle(R.string.action_search_title);
		builder.setCancelable(true);

		LayoutInflater inflater = LayoutInflater.from(getContext());
		View itemView = inflater.inflate(R.layout.pokemon_list_search, null);
		builder.setView(itemView);
		final EditText userInputText = itemView.findViewById(R.id.pokemon_list_search);

		builder.setPositiveButton(getString(R.string.action_search_submit), new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					String input = userInputText.getText().toString();
					searchTerm = input;
					Log.d(TAG, "Searched for: " + input);
					if(!input.isEmpty())
					{
						boolean ifSearchSuccess = false;
						if(input.matches("\\d+"))
							ifSearchSuccess = mPokemonListAdapter.searchPokemonId(searchTerm);
						else
							ifSearchSuccess = mPokemonListAdapter.searchPokemonName(searchTerm);

						if(ifSearchSuccess)
						{
							if(listRV.getLayoutManager() != null)
								mListRVState = listRV.getLayoutManager().onSaveInstanceState(); //save position

							mListFAB.setImageResource(R.drawable.ic_action_clear); //add to SQL
							mListFAB.hide();
							mListFAB.show(); //fix google bug to show image icon
							if(getActivity() != null)
								mListFAB.setBackgroundTintList(
									ContextCompat.getColorStateList(getActivity(), R.color.colorNegativeFAB));
						}

					}
				}
			});
		builder.setNegativeButton(getString(R.string.action_search_cancel), new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				clearSearch();
			}
		});

		builder.create().show();
	}

	private void clearSearch()
	{
		searchTerm = null;
		mPokemonListAdapter.clearSearchPokemon();
		if(listRV.getLayoutManager() !=null && mListRVState != null)
			listRV.getLayoutManager().onRestoreInstanceState(mListRVState); //restore current position
		mListFAB.setImageResource(R.drawable.ic_action_search); //add to SQL
		mListFAB.hide();
		mListFAB.show(); //fix google bug to show image icon
		mListFAB.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorPrimary));
	}

	@Override
	public void onDestroy()
	{
		PreferenceManager.getDefaultSharedPreferences(getContext()).unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		sortPokemonList();
	}

	private void sortPokemonList()
	{
		String sortPreference = preferences.getString(
			getString(R.string.pref_sort_key),
			getString(R.string.pref_sort_default)
		);

		if(sortPreference.equals(getString(R.string.pref_sort_value_sort_by_name)))
			mPokemonListAdapter.sortPokemonByName();
		else //default
			mPokemonListAdapter.sortPokemonById();

	}
}


