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
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonMove;
import rec.games.pokemon.teambuilder.model.PokemonType;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

public class PokemonItemDetailActivity extends AppCompatActivity implements PokemonMoveAdapter.OnPokemonMoveClickListener
{
	private static final String TAG = PokemonItemDetailActivity.class.getSimpleName();

	private final static String POKE_BULBAPEDIA_URL = "https://bulbapedia.bulbagarden.net/wiki/";
	private final static String POKE_BULBAPEDIA_END = "_(Pokémon)";
	private final static String VEEKUN_POKEMON_URL = "https://veekun.com/dex/pokemon/";

	private int pokeId;
	private Pokemon mPokemon;
	private ImageView mArtwork;
	private ImageView mFrontSprite;
	private ImageView mBackSprite;
	private TextView mPokemonName;
	private TextView mPokemonId;
	private TextView mPokemonType;
	private FloatingActionButton mItemFAB;
	private boolean mItemAdded;
	private String mTeamName;

	private RecyclerView mMoveRV;

	private PokeAPIViewModel mPokeViewModel;

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
		mPokemonName = findViewById(R.id.tv_pokemon_detail_name);
		mPokemonId = findViewById(R.id.tv_pokemon_detail_id);
		mArtwork = findViewById(R.id.iv_pokemon_detail_artwork);
		mFrontSprite = findViewById(R.id.iv_pokemon_detail_front_small);
		mBackSprite = findViewById(R.id.iv_pokemon_detail_back_small);
		mPokemonType = findViewById(R.id.tv_pokemon_type);

		mItemFAB = findViewById(R.id.item_add_FAB);
		mItemFAB.hide();
		mItemAdded = false;

		mMoveRV = findViewById(R.id.rv_moves);
		mMoveRV.setLayoutManager(new LinearLayoutManager(this));
		mMoveRV.setItemAnimator(new DefaultItemAnimator());

		Intent intent = getIntent();

		if(intent != null && intent.hasExtra(PokeAPIUtils.POKE_ITEM))
		{
			pokeId = intent.getIntExtra(PokeAPIUtils.POKE_ITEM, pokeId);

			mPokeViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);

			// Fill in with some fake data
			mPokeViewModel.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>()
			{
				@Override
				public void onChanged(@Nullable HashMap<Integer, LiveData<Pokemon>> list)
				{
					Log.d(TAG, "Got value");
					if(list != null)
					{
						//mPokemon = list.get(pokeId).getValue();
						mPokemon = mPokeViewModel.getPokemonReferenceFromCache(pokeId).getValue();
						Log.d(TAG, "mPokemon is loaded is "+ mPokemon.isLoaded());
						mPokeViewModel.getTypeCache();
						mPokeViewModel.getMoveCache();
						fillLayout();
					}
				}
			});

			mPokeViewModel.getMoveCache().observe(this, new Observer<HashMap<Integer, LiveData<PokemonMove>>>()
			{
				@Override
				public void onChanged(@Nullable HashMap<Integer, LiveData<PokemonMove>> list)
				{
					Log.d(TAG, "Got a value");
					if(mPokemon != null)
					{
						//mPokemon = list.get(pokeId).getValue();
						//mPokemon = mPokeViewModel.getPokemonReferenceFromCache(pokeId).getValue();
						//fillLayout();
						ArrayList mPokemonMoves = mPokemon.getMoves();
						if(mPokemonMoves != null)
						{
							for(int i = 0; i < mPokemonMoves.size(); i++)
							{
								if(mPokemonMoves.get(i) != null)
								{
									String output = mPokemonMoves.get(i).toString();
									Log.d(TAG, "Output: " + output);
								}
							}
							Log.d(TAG, "Size " + mPokemonMoves.size());
						}
						else{
							Log.d(TAG, "No moves");
						}
					}
					else
						Log.d(TAG, "is move null");
				}
			});

			mPokeViewModel.getTypeCache().observe(this, new Observer<HashMap<Integer, LiveData<PokemonType>>>()
			{
				@Override
				public void onChanged(@Nullable HashMap<Integer, LiveData<PokemonType>> list)
				{
					Log.d(TAG, "Got type value");
					if(mPokemon != null)
					{
						//mPokemon = list.get(pokeId).getValue();
						//mPokemon = mPokeViewModel.getPokemonReferenceFromCache(pokeId).getValue();
						//fillLayout();
						ArrayList mPokemonTypes = mPokemon.getTypes();
						if(mPokemonTypes != null)
						{
							for(int i = 0; i < mPokemonTypes.size(); i++)
							{
								if(mPokemonTypes.get(i) != null)
								{
									String output = mPokemonTypes.get(i).toString();
									Log.d(TAG, "Output: " + output);
								}
							}
							Log.d(TAG, "Size " + mPokemonTypes.size());
						}
						else
						{
							Log.d(TAG, "No types");
						}
					}
					else
						Log.d(TAG, "is type null");
				}
			});

			if(intent.hasExtra(Team.TEAM_ID))
			{
				mItemFAB.show();
				mTeamName = intent.getStringExtra(Team.TEAM_ID);
				Log.d(TAG, "Have Team " + mTeamName);
			}
			else
				Log.d(TAG, "Hiding FAB");

			final PokemonMoveAdapter adapter = new PokemonMoveAdapter(new ArrayList<String>(), this);

			ArrayList<String> moves = new ArrayList<>();
			//temp setup
			for(int i=0; i<10; i++)
			{
				moves.add("Move " + String.valueOf(i));
			}
			adapter.updatePokemonMoves(moves);
			mMoveRV.setAdapter(adapter);
		}
	}

	private void fillLayout()
	{
		if(pokeId > 0)
		{
			mPokemonName.setText(mPokemon.getName());
			mPokemonId.setText(String.valueOf(pokeId));

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if(prefs.getBoolean(this.getResources().getString(R.string.pref_image_key), true))
			{
				GlideApp.with(this).load(PokeAPIUtils.getArtworkUrl(pokeId))
					.error(GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId))
						.error(R.drawable.ic_poke_unknown))
					.placeholder(R.drawable.ic_poke_unknown).into(mArtwork);

				//sprites
				GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId)).into(mFrontSprite);
				GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId)).into(mBackSprite);
				mBackSprite.setScaleX(-1); //rotates horizontal, could remove
			}
			else
			{
				GlideApp.with(this).load(R.drawable.ic_poke_unknown).into(mArtwork);
				mFrontSprite.setImageResource(android.R.color.transparent);
				mBackSprite.setImageResource(android.R.color.transparent);
			}
			setTitle(mPokemon.getName());

			mPokemonType.setText("Type: Type1 / Type2");
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
		if(!mItemAdded)
		{
			Log.d(TAG, "Added");
			mItemFAB.setImageResource(R.drawable.ic_status_added); //add to SQL
			mItemAdded = true;
		}
		else
		{
			Log.d(TAG, "Removed");
			mItemFAB.setImageResource(R.drawable.ic_action_add); //remove
			mItemAdded = false;
		}
	}

	public void onPokemonMoveClicked(int moveID){
		Log.d(TAG, "Clicked" + moveID);
	}
}
