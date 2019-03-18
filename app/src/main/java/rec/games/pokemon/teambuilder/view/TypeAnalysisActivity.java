package rec.games.pokemon.teambuilder.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.db.AppDatabase;
import rec.games.pokemon.teambuilder.db.SavedTeamDao;
import rec.games.pokemon.teambuilder.db.TeamUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonResource;
import rec.games.pokemon.teambuilder.model.PokemonType;
import rec.games.pokemon.teambuilder.model.PokemonTypeResource;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.model.TeamMember;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

public class TypeAnalysisActivity extends AppCompatActivity implements PokemonTypeAdapter.OnPokemonTypeClickListener
{
	private static final String TAG = TypeAnalysisActivity.class.getSimpleName();

	private String actionBarTitle;
	private TextView mTypePower;
	private RecyclerView mTypeRV;

	private int loadCount = 0;
	private int totalLoadCount = 0;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_type);
		mTypeRV = findViewById(R.id.rv_types);
		mTypePower = findViewById(R.id.tv_type_resistance);

		Intent intent = getIntent();

		if (intent != null && intent.hasExtra(TeamListFragment.TEAM_TYPE_ANALYSIS))
		{
			actionBarTitle = intent.getStringExtra(TeamListFragment.TEAM_TYPE_ANALYSIS);
			setTitle(actionBarTitle);

			mTypeRV.setLayoutManager(new LinearLayoutManager(this));
			mTypeRV.setItemAnimator(new DefaultItemAnimator());

			final PokemonTypeAdapter adapter = new PokemonTypeAdapter(new ArrayList<String>(), this);

			String typeNames[] = {"bug","dark","dragon","electric","fairy",
				"fighting","fire","flying","ghost","grass","ground","ice",
				"normal","poison","psychic","rock","shadow","steel","unknown","water",
			}; //very temporary

			ArrayList<String> types = new ArrayList<>(Arrays.asList(typeNames));

			adapter.updatePokemonTypes(types);
			mTypeRV.setAdapter(adapter);

			waitForTeamToLoad();
		}

	}

	public void onPokemonTypeClicked(int typeID)
	{
		Log.d(TAG, "Clicked");
	}

	public void waitForTeamToLoad()
	{
		final PokeAPIViewModel viewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		final SavedTeamDao savedTeamDao = AppDatabase.getDatabase(this.getApplicationContext()).savedTeamDao();
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

		final MediatorLiveData<Object> mediator = new MediatorLiveData<>();

		totalLoadCount++;
		final LiveData<Boolean> typeListObserver = viewModel.getTypeListCache();
		mediator.addSource(typeListObserver, new Observer<Boolean>()
		{
			@Override
			public void onChanged(@Nullable Boolean listStatus)
			{
				if(listStatus != null && listStatus)
				{
					for(int key: viewModel.getTypeListIds())
					{
						totalLoadCount++;
						viewModel.loadType(key);

						final LiveData<PokemonType> liveType = viewModel.getLiveType(key);
						mediator.addSource(liveType, new Observer<PokemonType>()
						{
							@Override
							public void onChanged(@Nullable PokemonType pokemonType)
							{
								if(pokemonType instanceof PokemonTypeResource)
								{
									loadCount++;
									mediator.removeSource(liveType);
									mediator.setValue(null);
								}
							}
						});
					}

					loadCount++;
					mediator.removeSource(typeListObserver);
					mediator.setValue(null);
				}
			}
		});

		totalLoadCount++;
		final LiveData<Team> savedTeam = TeamUtils.getCurrentTeam(viewModel, savedTeamDao, prefs);
		mediator.addSource(savedTeam, new Observer<Team>()
		{
			@Override
			public void onChanged(@Nullable Team team)
			{
				if(team != null)
				{
					for(final TeamMember member: team.members)
					{
						totalLoadCount++;

						//because member.pokemon is a chained observer, have to observe it first before use
						//which is why the loading of it happens inside this new observer
						mediator.addSource(member.pokemon, new Observer<Pokemon>()
						{
							@Override
							public void onChanged(@Nullable Pokemon pokemon)
							{
								if(pokemon instanceof PokemonResource)
								{
									loadCount++;
									mediator.removeSource(member.pokemon);
									mediator.setValue(null);
								}
								else
									viewModel.loadPokemon(pokemon.getId());
							}
						});
					}

					loadCount++;
					mediator.removeSource(savedTeam);
					mediator.setValue(null);
				}
			}
		});

		mediator.observe(this, new Observer<Object>()
		{
			@Override
			public void onChanged(@Nullable Object o)
			{
				if(loadCount == totalLoadCount)
				{
					Log.d(TAG, "Finished loading all of the dependencies");
					mediator.removeObserver(this);
				}
			}
		});
	}
}
