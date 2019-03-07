package rec.games.pokemon.teambuilder;

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

public class TeamFragment extends Fragment {
    TeamAdapter adapter;
    RecyclerView teamRV;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_team, container, false);

        adapter = new TeamAdapter(this);
        teamRV = view.findViewById(R.id.rv_team_members);
        teamRV.setAdapter(adapter);
        teamRV.setLayoutManager(new GridLayoutManager(view.getContext(), 2));
        teamRV.setItemAnimator(new DefaultItemAnimator());

        return view;
    }


}
