package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.HashMap;

public class TeamListFragment extends Fragment implements TeamAdapter.OnTeamClickListener
{
	private static final String TAG = TeamListFragment.class.getSimpleName();

	private FloatingActionButton mTeamFAB;
	private Team team;

	private TeamAdapter adapter;
	private RecyclerView teamRV;
	private PokeAPIViewModel mViewModel;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
	}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.team_list, container, false);

        adapter = new TeamAdapter(this, this);
        teamRV = view.findViewById(R.id.rv_team_members);
        teamRV.setAdapter(adapter);
        teamRV.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        teamRV.setItemAnimator(new DefaultItemAnimator());

        mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);

        // Fill in with some fake data
        mViewModel.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>() {
            @Override
            public void onChanged(@Nullable HashMap<Integer, LiveData<Pokemon>> list) {
                team = new Team();

                team.members.add(newTeamMember(list.get(1))); // Bulbasaur
                team.members.add(newTeamMember(list.get(133))); // Eevee
                team.members.add(newTeamMember(list.get(25))); // Pikachu
                team.members.add(newTeamMember(list.get(150))); // Mewtwo
                team.members.add(newTeamMember(list.get(404))); // Not Found
                team.members.add(newTeamMember(list.get(500))); // Internal Server Error
                adapter.setTeam(team);
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
				if ( dy > 0 || dy < 0 && mTeamFAB.isShown())
					mTeamFAB.hide();							//hide if scrolling
			}

			@Override
			public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
			{
				if (newState == RecyclerView.SCROLL_STATE_IDLE)
					mTeamFAB.show();
				super.onScrollStateChanged(recyclerView, newState);
			}
		});

        return view;
    }

    private static TeamMember newTeamMember(LiveData<Pokemon> p) {
        TeamMember tm = new TeamMember();
        tm.pokemon = p;
        return tm;
    }

    public void onTeamMemberClicked(int pokeId)
	{
		Intent intent = new Intent(getContext(), PokemonItemDetailActivity.class);
		intent.putExtra(PokeAPIUtils.POKE_ITEM, pokeId); //temporary assignment
		startActivity(intent);
	}
}