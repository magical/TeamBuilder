package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.ViewHolder> {
    private LifecycleOwner mLifecycleOwner;
    private Team team;
    private Object mListener; // TODO

    public TeamAdapter(LifecycleOwner lifecycleOwner) { mLifecycleOwner = lifecycleOwner; }

    @Override
    public int getItemCount() {
        if (team != null && team.members != null) {
            return team.members.size();
        }
        return 0;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View v = inf.inflate(R.layout.team_list_entry, parent, false);
        return new ViewHolder(v, mListener, mLifecycleOwner);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder vh, int position) {
        vh.bind(team.members.get(position));
    }

    public void setTeam(Team team) {
        this.team = team;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final LifecycleOwner lifecycleOwner;
        Observer<Pokemon> mObserver;
        TeamMember mSavedTeamMember;
        private ImageView image;
        private TextView name;
        private Object listener; // TODO

        ViewHolder(View v, Object l, LifecycleOwner lifecycleOwner) {
            super(v);
            listener = l;
            this.lifecycleOwner = lifecycleOwner;
            image = v.findViewById(R.id.pokemon_sprite);
            name = v.findViewById(R.id.pokemon_name);
        }

        void bind(TeamMember member) {
            if (mObserver != null) {
                mSavedTeamMember.pokemon.removeObserver(mObserver);
            }
            // TODO: set image to ic_poke_unknown
            name.setText("Loading...");
            mObserver = new Observer<Pokemon>() {
                @Override
                public void onChanged(@Nullable Pokemon pokemon) {
                    if (pokemon != null) {
                        name.setText(pokemon.getName());
                        GlideApp.with(itemView.getContext()).load(PokeAPIUtils.getSpriteUrl(pokemon.getId())).into(image);
                    }
                }
            };
            member.pokemon.observe(lifecycleOwner, mObserver);
            mSavedTeamMember = member;
        }
    }
}
