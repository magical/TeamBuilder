package rec.games.pokemon.teambuilder.view;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.db.DBUtils;
import rec.games.pokemon.teambuilder.db.SavedTeamRepository;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

public class PokemonItemDetailActivity extends AppCompatActivity
{
	private static final String TAG = PokemonItemDetailActivity.class.getSimpleName();

	private final static String POKE_BULBAPEDIA_URL = "https://bulbapedia.bulbagarden.net/wiki/";
	private final static String POKE_BULBAPEDIA_END = "_(Pokémon)";
	private final static String VEEKUN_POKEMON_URL = "https://veekun.com/dex/pokemon/";

	private int pokeId;
	private Pokemon mPokemon;
	private ImageView mArtwork;
	private TextView mPokemonName;
	private FloatingActionButton mItemFAB;
	private boolean mItemAdded;
	private String mTeamName;

	private SavedTeamRepository mSavedTeamRepo;
	private PokeAPIViewModel mViewModel;

	/**
	 * Constructs a url to the Bulbapedia page for a Pokémon
	 *
	 * @param name the pokemon's resource name
	 */
	private static Uri getBulbapediaPage(String name)
	{
		return Uri.parse(POKE_BULBAPEDIA_URL).buildUpon()
			.appendEncodedPath(name + POKE_BULBAPEDIA_END).build();
	}

	/**
	 * Constructs a url to the veekun page for a Pokémon
	 *
	 * @param name the pokemon's resource name
	 */
	private static Uri getVeekunUrl(String name)
	{
		return Uri.parse(VEEKUN_POKEMON_URL).buildUpon()
			.appendPath(name)
			.build();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_item_detail);
		mArtwork = findViewById(R.id.iv_pokemon_detail_artwork);
		mPokemonName = findViewById(R.id.tv_pokemon_detail_name);
		mItemFAB = findViewById(R.id.item_add_FAB);
		mItemFAB.hide();
		mItemAdded = false;

		Intent intent = getIntent();

		if(intent != null && intent.hasExtra(PokeAPIUtils.POKE_ITEM))
		{
			pokeId = intent.getIntExtra(PokeAPIUtils.POKE_ITEM, pokeId);

			PokeAPIViewModel model = ViewModelProviders.of(this).get(PokeAPIViewModel.class);

			// Fill in with some fake data
			model.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>()
			{
				@Override
				public void onChanged(@Nullable HashMap<Integer, LiveData<Pokemon>> list)
				{
					Log.d(TAG, "Got value");
					if(list != null)
						mPokemon = list.get(pokeId).getValue();

					fillLayout();
				}
			});

			if(intent.hasExtra(Team.TEAM_ID))
			{
				mItemFAB.show();
				mTeamName = intent.getStringExtra(Team.TEAM_ID);
				Log.d(TAG, "Have Team " + mTeamName);

			}
		}

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mSavedTeamRepo = new SavedTeamRepository(this.getApplication());
	}

	private void fillLayout()
	{
		if(pokeId > 0)
		{
			mPokemonName.setText(mPokemon.getName());

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if(prefs.getBoolean(this.getResources().getString(R.string.pref_image_key), true))
			{
				GlideApp.with(this).load(PokeAPIUtils.getArtworkUrl(pokeId))
					.error(GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId))
						.error(R.drawable.ic_poke_unknown))
					.placeholder(R.drawable.ic_poke_unknown).into(mArtwork);
			}
			else
			{
				GlideApp.with(this).load(R.drawable.ic_poke_unknown).into(mArtwork);
			}
			setTitle(mPokemon.getName());
		}

		mItemFAB.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				addOrRemovePokemonFromTeam();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.pokemon_list_detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_share_poke_details:
				sharePokeDetails();
				return true;
			case R.id.action_browser:
				shareToBrowser();
				return true;
			case R.id.action_veekun:
				openInVeekun();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void sharePokeDetails()
	{
		if(mPokemon != null) //fake null - TODO - replace
		{
			String pokeDetails = mPokemon.getName() + " (" +
				Integer.toString(mPokemon.getId()) + ")";

			ShareCompat.IntentBuilder.from(this)
				.setType("text/plain")
				.setText(pokeDetails)
				.setChooserTitle(R.string.share_chooser_poke_details)
				.startChooser();
		}
	}

	public void shareToBrowser()
	{
		if(mPokemon != null) //placeholder data, need to replace
		{
			Intent intent = new Intent(Intent.ACTION_VIEW,
				getBulbapediaPage(mPokemon.getName()));
			if(intent.resolveActivity(getPackageManager()) != null)
			{
				startActivity(intent);
			}
		}
	}

	public void openInVeekun()
	{
		if(mPokemon != null) //placeholder data, need to replace
		{
			Intent intent = new Intent(Intent.ACTION_VIEW,
				getVeekunUrl(mPokemon.getName()));
			if(intent.resolveActivity(getPackageManager()) != null)
			{
				startActivity(intent);
			}
		}
	}

	public void addOrRemovePokemonFromTeam()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		//final LiveData<Team> liveTeam = DBUtils.getCurrentTeam(mViewModel, mSavedTeamDao, prefs);
		if(!mItemAdded)
		{
			Log.d(TAG, "Added");
			DBUtils.addPokemonToCurrentTeam(mSavedTeamRepo, prefs, mPokemon);
			mItemFAB.setImageResource(R.drawable.ic_status_added); //add to SQL
			mItemAdded = true;
		}
		else
		{
			Log.d(TAG, "Removed");
			DBUtils.removePokemonFromCurrentTeam(mSavedTeamRepo, prefs, mPokemon);
			mItemFAB.setImageResource(R.drawable.ic_action_add); //remove
			mItemAdded = false;
		}
	}
}
