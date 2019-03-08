package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;

public class TeamListFragment extends Fragment {
    private Team team;

    private TeamAdapter adapter;
    private RecyclerView teamRV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.team_list, container, false);

        adapter = new TeamAdapter(this);
        teamRV = view.findViewById(R.id.rv_team_members);
        teamRV.setAdapter(adapter);
        teamRV.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        teamRV.setItemAnimator(new DefaultItemAnimator());

        PokeAPIViewModel model = ViewModelProviders.of(this).get(PokeAPIViewModel.class);

        // Fill in with some fake data
        model.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>() {
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

        return view;
    }

    private static TeamMember newTeamMember(LiveData<Pokemon> p) {
        TeamMember tm = new TeamMember();
        tm.pokemon = p;
        return tm;
    }
}