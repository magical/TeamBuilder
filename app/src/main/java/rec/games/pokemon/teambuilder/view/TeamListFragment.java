package rec.games.pokemon.teambuilder.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.db.AppDatabase;
import rec.games.pokemon.teambuilder.db.DBUtils;
import rec.games.pokemon.teambuilder.db.SavedTeamDao;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.model.TeamMember;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

public class TeamListFragment extends Fragment implements TeamAdapter.OnTeamClickListener
{
	private static final String TAG = TeamListFragment.class.getSimpleName();

	private FloatingActionButton mTeamFAB;
	private LiveData<Team> mLiveTeam;

	private TeamAdapter mTeamAdapter;
	private RecyclerView teamRV;
	private PokeAPIViewModel mViewModel;
	private SavedTeamDao mSavedTeamDao;

	private static TeamMember newTeamMember(LiveData<Pokemon> p)
	{
		TeamMember tm = new TeamMember();
		tm.pokemon = p;
		return tm;
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
		View view = inflater.inflate(R.layout.team_list, container, false);

		mTeamAdapter = new TeamAdapter(this, this);
		teamRV = view.findViewById(R.id.rv_team_members);
		teamRV.setAdapter(mTeamAdapter);
		teamRV.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
		teamRV.setItemAnimator(new DefaultItemAnimator());

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mSavedTeamDao = AppDatabase.getDatabase(this.getContext()).savedTeamDao();

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		mLiveTeam = DBUtils.getCurrentTeam(mViewModel, mSavedTeamDao, prefs);

		mLiveTeam.observe(this, new Observer<Team>()
		{
			@Override
			public void onChanged(@Nullable Team team)
			{
				mTeamAdapter.setTeam(team);
			}
		});

		mTeamFAB = view.findViewById(R.id.team_FAB);
		mTeamFAB.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//should be replaced by activity to create a new team, is a placeholder...
				Log.d(TAG, "FAB Clicked");
				Intent intent = new Intent(getActivity(), TeamPokemonActivity.class);
				intent.putExtra(Team.TEAM_ID, "Team1");
				startActivity(intent);
			}
		});
		teamRV.addOnScrollListener(new RecyclerView.OnScrollListener()
		{
			@Override
			public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
			{
				if(dy > 0 || dy < 0 && mTeamFAB.isShown())
					mTeamFAB.hide();                            //hide if scrolling
			}

			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
			{
				if(newState == RecyclerView.SCROLL_STATE_IDLE)
					mTeamFAB.show();
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

		return view;
	}

	public void onTeamMemberClicked(int pokeId)
	{
		Intent intent = new Intent(getContext(), PokemonItemDetailActivity.class);
		intent.putExtra(PokeAPIUtils.POKE_ITEM, pokeId); //temporary assignment
		startActivity(intent);
	}
}