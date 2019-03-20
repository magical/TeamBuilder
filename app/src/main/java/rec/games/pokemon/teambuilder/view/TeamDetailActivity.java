package rec.games.pokemon.teambuilder.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.model.Team;

public class TeamDetailActivity extends AppCompatActivity
{
	private static final String TAG = TeamDetailActivity.class.getSimpleName();

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_detail);

		setTitle(getString(R.string.activity_team_detail_title));
		if(getSupportActionBar() != null)
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		Bundle bundle = new Bundle();
		Intent intent = getIntent();
		if(intent != null && intent.hasExtra(Team.TEAM_ID))
		{
			//Log.d(TAG, "Passing Team info");
			bundle.putInt(Team.TEAM_ID, intent.getIntExtra(Team.TEAM_ID, 0));
		}

		TeamFragment teamFragment = new TeamFragment();
		teamFragment.setArguments(bundle);
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.replace(R.id.team_detail_fragment, teamFragment);
		transaction.commit();
	}
}
