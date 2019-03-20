package rec.games.pokemon.teambuilder.view;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.db.SavedTeam;

interface OnTeamClickListener
{
	void onTeamClicked(SavedTeam savedTeam);
}

public class TeamListAdapter extends RecyclerView.Adapter<TeamListAdapter.ViewHolder>
{
	List<SavedTeam> mTeamList;
	OnTeamClickListener mListener;

	public TeamListAdapter(OnTeamClickListener l)
	{
		mTeamList = new ArrayList<>();
		mListener = l;
	}

	public void setTeamList(List<SavedTeam> teams)
	{
		Log.d(this.getClass().getName(), "team length: " + teams.size());
		mTeamList = teams;
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount()
	{
		return mTeamList.size();
	}

	@NonNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position)
	{
		LayoutInflater inf = LayoutInflater.from(parent.getContext());
		View v = inf.inflate(R.layout.team_list_entry, parent, false);
		return new TeamListAdapter.ViewHolder(v, mListener);
	}

	@Override
	public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position)
	{
		viewHolder.bind(mTeamList.get(position));
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		OnTeamClickListener mListener;
		SavedTeam mSavedTeam;
		TextView tvName;
		TextView tvCount;

		public ViewHolder(View itemView, OnTeamClickListener l)
		{
			super(itemView);
			mListener = l;
			tvName = itemView.findViewById(R.id.tv_team_name);
			tvCount = itemView.findViewById(R.id.tv_team_count);

			itemView.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mListener.onTeamClicked(mSavedTeam);
				}
			});
		}

		public void bind(SavedTeam savedTeam)
		{
			mSavedTeam = savedTeam;

			if(!TextUtils.isEmpty(savedTeam.name))
			{
				tvName.setText(savedTeam.name);
			}
			else
			{
				tvName.setText(String.format(Locale.US, "Team %d", savedTeam.id));
			}

			tvCount.setText(Integer.toString(savedTeam.memberIds.size()));
		}
	}

	SavedTeam getTeam(int position) {
		return mTeamList.get(position);
	}
}
