package rec.games.pokemon.teambuilder;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TeamListFragment extends Fragment
{
	private static final String TAG = TeamListFragment.class.getSimpleName();

	FloatingActionButton mTeamFAB;

	public TeamListFragment()
	{

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

		mTeamFAB = view.findViewById(R.id.team_FAB);
		mTeamFAB.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				//should be replaced by activity to create a new team, is a placeholder...
				Log.d(TAG, "FAB Clicked");
				Intent intent = new Intent(getActivity(), TeamPokemonActivity.class);
				startActivity(intent);
			}
		});

		return view;
	}
}