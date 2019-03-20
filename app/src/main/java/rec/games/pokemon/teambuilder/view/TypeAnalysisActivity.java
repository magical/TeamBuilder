package rec.games.pokemon.teambuilder.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
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
import android.widget.TextView;

import java.util.ArrayList;

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

	private LiveData<Team> mTeamLiveData;
	private String actionBarTitle;
	private TextView mTypePower;
	private RecyclerView mTypeRV;
	private PokemonTypeAdapter mAdapter;

	private int loadCount = 0;
	private int totalLoadCount = 0;
	private int teamId;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_type);
		mTypeRV = findViewById(R.id.rv_types);
		mTypePower = findViewById(R.id.tv_type_resistance);

		Intent intent = getIntent();

		if (intent != null && intent.hasExtra(TeamFragment.TEAM_TYPE_ANALYSIS) && intent.hasExtra(Team.TEAM_ID))
		{
			actionBarTitle = intent.getStringExtra(TeamFragment.TEAM_TYPE_ANALYSIS);
			teamId = intent.getIntExtra(Team.TEAM_ID, 1);
			setTitle(actionBarTitle);

			mTypeRV.setLayoutManager(new LinearLayoutManager(this));
			mTypeRV.setItemAnimator(new DefaultItemAnimator());

			mAdapter = new PokemonTypeAdapter(new ArrayList<TypeInfo>(), this);
			mTypeRV.setAdapter(mAdapter);

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
					for(int key : viewModel.getTypeListIds())
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
		final LiveData<Team> savedTeam = TeamUtils.getCurrentTeam(viewModel, savedTeamDao, teamId);
		mediator.addSource(savedTeam, new Observer<Team>()
		{
			@Override
			public void onChanged(@Nullable final Team team)
			{
				if(team != null)
				{
					for(final TeamMember member : team.members)
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
									for(final LiveData<PokemonType> pokemonType : ((PokemonResource) pokemon).getTypes())
									{
										totalLoadCount++;

										mediator.addSource(pokemonType, new Observer<PokemonType>()
										{
											@Override
											public void onChanged(@Nullable PokemonType type)
											{
												if(type instanceof PokemonTypeResource)
												{
													loadCount++;
													mediator.removeSource(pokemonType);
													mediator.setValue(null);
												}
												else
													viewModel.loadType(type.getId());
											}
										});
									}

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
					computeResults(savedTeam.getValue());
					mediator.removeObserver(this); // remove?
				}
			}
		});
	}

	private void computeResults(Team team)
	{
		final PokeAPIViewModel viewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		ArrayList<TypeInfo> typeInfoList = new ArrayList<>();

		// waitForResults has finished, so it should be safe to just grab everything from the cache
		for(PokemonType pokemonType : viewModel.extractTypeListFromCache())
		{
			PokemonTypeResource attackingType = (PokemonTypeResource) pokemonType;
			TypeInfo info = new TypeInfo();
			info.type = attackingType;
			for(TeamMember m : team.members)
			{
				PokemonResource p = (PokemonResource) m.pokemon.getValue();
				double damageMultiplier = 1;
				for(LiveData<PokemonType> liveType : p.getTypes())
				{
					PokemonType defendingType = liveType.getValue();
					damageMultiplier *= attackingType.getDamageMultiplier(defendingType);
				}
				if(damageMultiplier > 1)
				{
					info.weak++;
				}
				else if(damageMultiplier < 1)
				{
					info.strong++;
				}
			}
			typeInfoList.add(info);
		}

		mAdapter.updatePokemonTypes(typeInfoList);
	}
}
