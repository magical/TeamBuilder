package rec.games.pokemon.teambuilder.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import rec.games.pokemon.teambuilder.R;

public class TeamTypeActivity extends AppCompatActivity implements PokemonTypeAdapter.OnPokemonTypeClickListener
{
	private static final String TAG = TeamTypeActivity.class.getSimpleName();

	private String displayAtkDef;
	private TextView mTypePower;
	private RecyclerView mTypeRV;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_type);
		mTypeRV = findViewById(R.id.rv_types);
		mTypePower = findViewById(R.id.tv_type_str);

		Intent intent = getIntent();

		if (intent != null && intent.hasExtra(TeamListFragment.TEAM_ATK_DEF))
		{
			displayAtkDef = intent.getStringExtra(TeamListFragment.TEAM_ATK_DEF);
			setTitle(displayAtkDef);
			mTypePower.setText(displayAtkDef);

			mTypeRV.setLayoutManager(new LinearLayoutManager(this));
			mTypeRV.setItemAnimator(new DefaultItemAnimator());

			final PokemonTypeAdapter adapter = new PokemonTypeAdapter(new ArrayList<String>(), this);

			ArrayList<String> types = new ArrayList<>();
			for (int i=1; i<12; i++)
			{
				types.add("Type" + String.valueOf(i));
			}
			adapter.updatePokemonTypes(types);
			mTypeRV.setAdapter(adapter);
		}

	}

	public void onPokemonTypeClicked(int typeID)
	{
		Log.d(TAG, "Clicked");
	}
}
