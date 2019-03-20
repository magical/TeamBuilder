package rec.games.pokemon.teambuilder.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.db.AppDatabase;
import rec.games.pokemon.teambuilder.db.SavedTeamDao;
import rec.games.pokemon.teambuilder.db.TeamUtils;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.model.TeamMember;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

public class TeamFragment extends Fragment implements TeamAdapter.OnTeamClickListener
{
	private static final String TAG = TeamFragment.class.getSimpleName();

	private LiveData<Team> mLiveTeam;

	public static final String TEAM_TYPE_ANALYSIS = "rec.games.pokemon.teambuilder.view.TeamFragment";
	public static final String TEAM_MOVE_ENABLE = "rec.games.pokemon.teambuilder.view.TeamFragment.Move.Enable";

	private TeamAdapter mTeamAdapter;
	private RecyclerView teamRV;
	private PokeAPIViewModel mViewModel;
	private SavedTeamDao mSavedTeamDao;

	private TextView mLoadingErrorMsgTV;
	private LinearLayout mLoadingErrorLL;
	private Button mLoadingErrorBtn;

	private FloatingActionButton mTeamFAB;
	private BottomAppBar mAppBar;
	private LinearLayout mActionTypeAnalysis;

	private int teamId;

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
		View view = inflater.inflate(R.layout.team_detail, container, false);

		mTeamAdapter = new TeamAdapter(this, this);
		teamRV = view.findViewById(R.id.rv_team_members);
		teamRV.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
		teamRV.setItemAnimator(new DefaultItemAnimator());
		teamRV.setNestedScrollingEnabled(false);
		teamRV.setAdapter(mTeamAdapter);

		//error button
		mLoadingErrorMsgTV = view.findViewById(R.id.tv_loading_error);
		mLoadingErrorLL = view.findViewById(R.id.ll_loading_error);
		mLoadingErrorBtn = view.findViewById(R.id.btn_loading_error);

		mTeamFAB = view.findViewById(R.id.team_FAB);
		mTeamFAB.hide();
		mAppBar = view.findViewById(R.id.team_BottomAppBar);
		mAppBar.setVisibility(View.INVISIBLE);
		mActionTypeAnalysis = view.findViewById(R.id.action_type_analysis);
		mActionTypeAnalysis.setClickable(true);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mSavedTeamDao = AppDatabase.getDatabase(this.getContext()).savedTeamDao();

		teamId = getArguments().getInt(Team.TEAM_ID, 0);
		mLiveTeam = TeamUtils.getCurrentTeam(mViewModel, mSavedTeamDao, teamId);

		mLiveTeam.observe(this, new Observer<Team>()
		{
			@Override
			public void onChanged(@Nullable Team team)
			{
				if(team == null)
				{
					Log.d(TAG, "Could not load team member info");
					teamRV.setVisibility(View.GONE);

					mLoadingErrorMsgTV.setVisibility(View.VISIBLE);
					mLoadingErrorLL.setVisibility(LinearLayout.VISIBLE);
					mLoadingErrorBtn.setVisibility(View.VISIBLE);

					mTeamFAB.hide();
					mAppBar.setVisibility(View.INVISIBLE);
				}
				else
				{
					teamRV.setVisibility(View.VISIBLE);

					mLoadingErrorMsgTV.setVisibility(View.INVISIBLE);
					mLoadingErrorLL.setVisibility(LinearLayout.INVISIBLE);
					mLoadingErrorBtn.setVisibility(View.INVISIBLE);

					mTeamFAB.show();
					mAppBar.setVisibility(View.VISIBLE);

					mTeamAdapter.setTeam(team);

				}
			}
		});

		mLoadingErrorBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				mViewModel.getNewPokemonList();
			}
		});

		mTeamFAB.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//should be replaced by activity to create a new team, is a placeholder...
				Intent intent = new Intent(getActivity(), TeamPokemonActivity.class);
				intent.putExtra(Team.TEAM_ID, teamId);
				intent.putExtra(TeamFragment.TEAM_MOVE_ENABLE, true); //allow access to change moves
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

		mActionTypeAnalysis.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				Intent intent = new Intent(getContext(), TypeAnalysisActivity.class);
				String title = "Team " + teamId + " analysis";
				intent.putExtra(TeamFragment.TEAM_TYPE_ANALYSIS, title);
				intent.putExtra(Team.TEAM_ID, teamId);
				startActivity(intent);
			}
		});

		return view;
	}

	public void onTeamMemberClicked(int pokeId)
	{
		if (pokeId > 0)
		{
			Intent intent = new Intent(getContext(), PokemonItemDetailActivity.class);
			intent.putExtra(PokeAPIUtils.POKE_ITEM, pokeId); //temporary assignment
			intent.putExtra(Team.TEAM_ID, teamId);
			intent.putExtra(TeamFragment.TEAM_MOVE_ENABLE, true); //allow access to change moves
			startActivity(intent);
		}
	}
}