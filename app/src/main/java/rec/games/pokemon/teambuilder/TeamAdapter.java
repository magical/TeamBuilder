package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

	private static final String TAG = TeamAdapter.class.getSimpleName();
    private LifecycleOwner mLifecycleOwner;
    private Team team;
    private OnTeamClickListener mListener;
	Context context;

    public interface OnTeamClickListener
	{
		void onTeamMemberClicked(int pokeId);
	}

    public TeamAdapter(LifecycleOwner lifecycleOwner, OnTeamClickListener l)
	{
    	mLifecycleOwner = lifecycleOwner;
    	mListener = l;
	}

	public void setTeam(Team team) {
		this.team = team;
		notifyDataSetChanged();
	}

    @Override
    public int getItemCount() {
        if (team != null && team.members != null) {
            return team.members.size();
        }
        return 0;
    }

    public int getTeamMemberId(int position){
    	if (position >= 0 && team.members != null)
    		return team.members.get(position).pokemon.getValue().getId();
    	else
    		return 0;
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		context = recyclerView.getContext();
	}

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        View v = inf.inflate(R.layout.team_list_entry, parent, false);
        return new TeamViewHolder(v, mListener, mLifecycleOwner);
    }

	private boolean checkDisplayImages(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getResources().getString(R.string.pref_image_key), true);
	}

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder vh, int position) {
        vh.bind(team.members.get(position));
    }

    public class TeamViewHolder extends RecyclerView.ViewHolder
	{
		private OnTeamClickListener mListener;
        private final LifecycleOwner lifecycleOwner;
        Observer<Pokemon> mObserver;
        TeamMember mSavedTeamMember;
        private ImageView image;
        private TextView name;

        TeamViewHolder(View v, OnTeamClickListener l, LifecycleOwner lifecycleOwner) {
            super(v);
            mListener = l;
            this.lifecycleOwner = lifecycleOwner;
            image = v.findViewById(R.id.pokemon_sprite);
            name = v.findViewById(R.id.pokemon_name);

            v.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mListener.onTeamMemberClicked(getTeamMemberId(getAdapterPosition()));
					//Log.d(TAG, "Clicked + " + getTeamMemberId(getAdapterPosition()));
				}
			});
        }

        void bind(TeamMember member) {
            if (mObserver != null) {
                mSavedTeamMember.pokemon.removeObserver(mObserver);
            }
            // set image to ic_poke_unknown - done, auto does it
            name.setText(R.string.loading_msg_short);
            mObserver = new Observer<Pokemon>() {
                @Override
                public void onChanged(@Nullable Pokemon pokemon) {
                    if (pokemon != null) {
                        name.setText(pokemon.getName());
						if(checkDisplayImages())
						{
							GlideApp.with(itemView.getContext()).
								load(PokeAPIUtils.getSpriteUrl(pokemon.getId())).into(image);
						}
						else
						{
							GlideApp.with(itemView.getContext())
								.load(R.drawable.ic_poke_unknown).into(image);
						}
                    }
                }
            };
            member.pokemon.observe(lifecycleOwner, mObserver);
            mSavedTeamMember = member;
        }
    }
}
