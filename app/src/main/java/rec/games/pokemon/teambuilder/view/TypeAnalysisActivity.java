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
import java.util.Arrays;

import rec.games.pokemon.teambuilder.R;

public class TypeAnalysisActivity extends AppCompatActivity implements PokemonTypeAdapter.OnPokemonTypeClickListener
{
	private static final String TAG = TypeAnalysisActivity.class.getSimpleName();

	private String actionBarTitle;
	private TextView mTypePower;
	private RecyclerView mTypeRV;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_team_type);
		mTypeRV = findViewById(R.id.rv_types);
		mTypePower = findViewById(R.id.tv_type_resistance);

		Intent intent = getIntent();

		if (intent != null && intent.hasExtra(TeamListFragment.TEAM_TYPE_ANALYSIS))
		{
			actionBarTitle = intent.getStringExtra(TeamListFragment.TEAM_TYPE_ANALYSIS);
			setTitle(actionBarTitle);

			mTypeRV.setLayoutManager(new LinearLayoutManager(this));
			mTypeRV.setItemAnimator(new DefaultItemAnimator());

			final PokemonTypeAdapter adapter = new PokemonTypeAdapter(new ArrayList<String>(), this);

			String typeNames[] = {"bug","dark","dragon","electric","fairy",
				"fighting","fire","flying","ghost","grass","ground","ice",
				"normal","poison","psychic","rock","shadow","steel","unknown","water",
			}; //very temporary

			ArrayList<String> types = new ArrayList<>(Arrays.asList(typeNames));

			adapter.updatePokemonTypes(types);
			mTypeRV.setAdapter(adapter);
		}

	}

	public void onPokemonTypeClicked(int typeID)
	{
		Log.d(TAG, "Clicked");
	}
}
