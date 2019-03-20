package rec.games.pokemon.teambuilder.view;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.db.SavedTeamRepository;
import rec.games.pokemon.teambuilder.db.TeamUtils;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonMove;
import rec.games.pokemon.teambuilder.model.PokemonMoveResource;
import rec.games.pokemon.teambuilder.model.PokemonResource;
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
	private int numTypes = 1;
	private PokemonType mType1;
	private PokemonType mType2;

	private ImageView mArtwork;
	private ImageView mFrontSprite;
	private ImageView mBackSprite;
	private TextView mPokemonName;
	private TextView mPokemonId;
	private TextView mPokemonType1TV;
	private ImageView mPokemonType1IV;
	private TextView mPokemonTypeSeperator;
	private TextView mPokemonType2TV;
	private ImageView mPokemonType2IV;
	private FloatingActionButton mItemFAB;
	private boolean mItemAdded;
	private LiveData<Boolean> mLiveItemAdded;
	private boolean mAllowMovesSelected;
	private int mTeamId;
	private ProgressBar mMoveLoaderPB;

	private SavedTeamRepository mSavedTeamRepo;
	private RecyclerView mMoveRV;
	private PokemonMoveAdapter mMoveAdapter;

	private PokeAPIViewModel mPokeViewModel;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_item_detail);
		mPokemonName = findViewById(R.id.tv_pokemon_detail_name);
		mPokemonId = findViewById(R.id.tv_pokemon_detail_id);
		mArtwork = findViewById(R.id.iv_pokemon_detail_artwork);
		//mFrontSprite = findViewById(R.id.iv_pokemon_detail_front_small);
		//mBackSprite = findViewById(R.id.iv_pokemon_detail_back_small);
		mPokemonType1TV = findViewById(R.id.tv_pokemon_type1);
		mPokemonType1IV = findViewById(R.id.iv_pokemon_type1);
		mPokemonTypeSeperator = findViewById(R.id.tv_pokemon_type_seperator);
		mPokemonType2TV = findViewById(R.id.tv_pokemon_type2);
		mPokemonType2IV = findViewById(R.id.iv_pokemon_type2);
		mMoveLoaderPB = findViewById(R.id.pb_loading_move);

		mAllowMovesSelected = false; //default to false

		mItemFAB = findViewById(R.id.item_add_FAB);
		mItemFAB.hide();
		mItemAdded = false;

		mMoveRV = findViewById(R.id.rv_moves);
		mMoveRV.setLayoutManager(new LinearLayoutManager(this));
		mMoveRV.setItemAnimator(new DefaultItemAnimator());

		mMoveLoaderPB.setVisibility(View.VISIBLE);
		mMoveRV.setVisibility(View.GONE);

		Intent intent = getIntent();

		mPokeViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		if(intent != null && intent.hasExtra(PokeAPIUtils.POKE_ITEM))
		{
			pokeId = intent.getIntExtra(PokeAPIUtils.POKE_ITEM, 0);

			mPokeViewModel.getLivePokemon(pokeId).observe(this, new Observer<Pokemon>()
			{
				@Override
				public void onChanged(@Nullable Pokemon pokemon)
				{
					if (pokemon != null)
					{
						Log.d(TAG, "mPokemon is loaded is " + pokemon.isLoaded());
						int code = mPokeViewModel.loadPokemon(pokeId);
						Log.d(TAG, String.format(Locale.US, "loading pokemon %d: status %d", pokeId, code)); // load if not deferred
					}
					setPokemon(pokemon);
				}
			});

			if(intent.hasExtra(Team.TEAM_ID))
			{
				mItemFAB.show();
				updateFABStatus(true);
				mTeamId = intent.getIntExtra(Team.TEAM_ID, 0);
				Log.d(TAG, "Have Team " + mTeamId);

				//if team is showing, hide/show FAB and set padding
				mMoveRV.addOnScrollListener(new RecyclerView.OnScrollListener()
				{
					@Override
					public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy)
					{
						if(dy > 0 || dy < 0 && mItemFAB.isShown())
							mItemFAB.hide();                            //hide if scrolling
					}

					@Override
					public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState)
					{
						if(newState == RecyclerView.SCROLL_STATE_IDLE)
							mItemFAB.show();
						super.onScrollStateChanged(recyclerView, newState);
					}
				});

				mMoveRV.setPadding(
					mMoveRV.getPaddingLeft(),
					mMoveRV.getPaddingTop(),
					mMoveRV.getPaddingRight(),
					getResources().getDimensionPixelOffset(R.dimen.rv_fab_padding));
				mMoveRV.setClipToPadding(false);

				mSavedTeamRepo = new SavedTeamRepository(this.getApplication());

				mLiveItemAdded = mSavedTeamRepo.isPokemonInTeam(mTeamId, pokeId);
				mLiveItemAdded.observe(this, new Observer<Boolean>()
				{
					@Override
					public void onChanged(@Nullable Boolean added)
					{
						if (added != null)
						{
							updateFABStatus(added);
						}
					}
				});
			}

			if(intent.hasExtra(TeamFragment.TEAM_MOVE_ENABLE))
			{
				mAllowMovesSelected = false; //true to enable
			}

			// Fill in with some fake data
			mMoveAdapter = new PokemonMoveAdapter(new ArrayList<LiveData<PokemonMove>>(), this, mAllowMovesSelected);
			mMoveRV.setAdapter(mMoveAdapter);
		}
	}

	private void setPokemon(@Nullable Pokemon pokemon) {
		mPokemon = pokemon;

		// set observers on types and moves
		if (pokemon != null && !pokemon.isDeferred()) {
			PokemonResource p = (PokemonResource)pokemon;
			numTypes = p.getTypes().size();

			// TODO: remove old observers
			if (p.getTypes().size() >= 1) {
				 p.getTypes().get(0).observe(this, new Observer<PokemonType>()
				{
					@Override
					public void onChanged(@Nullable PokemonType pokemonType)
					{
						mType1 = pokemonType;
					}
				});
			}
			if (p.getTypes().size() >= 2) {

				p.getTypes().get(1).observe(this, new Observer<PokemonType>()
				{
					@Override
					public void onChanged(@Nullable PokemonType pokemonType)
					{
						mType2 = pokemonType;
					}
				});
			}

			//complete the move chain
			final LifecycleOwner owner = this;
			for(LiveData<PokemonMove> pokemonMove: p.getMoves())
			{
				pokemonMove.observe(this, new Observer<PokemonMove>()
				{
					@Override
					public void onChanged(@Nullable PokemonMove move)
					{
						if(move instanceof PokemonMoveResource)
						{
							//complete the move type chain
							move.getType().observe(owner, new Observer<PokemonType>()
							{
								@Override
								public void onChanged(@Nullable PokemonType pokemonType)
								{
									//maybe do something when the move's type chain completes
								}
							});
						}
						else if(move != null)
							mPokeViewModel.loadMove(move.getId());

						if(mMoveLoaderPB.getVisibility() == View.VISIBLE)
						{
							mMoveLoaderPB.setVisibility(View.GONE);
							mMoveRV.setVisibility(View.VISIBLE);
						}
					}
				});
			}
			mMoveAdapter.updatePokemonMoves(p.getMoves());
		}
		// update the layout
		fillLayout();
	}

	private void fillLayout()
	{
		if(mPokemon != null)
		{
			if(mPokemon instanceof PokemonResource)
			{
				((PokemonResource) mPokemon).getLocaleName("en").observe(this, new Observer<String>()
				{
					@Override
					public void onChanged(@Nullable String s)
					{
						if(s == null)
							mPokemonName.setText(mPokemon.getName());
						else
							mPokemonName.setText(s);
					}
				});
			}
			else
			{
				mPokemonName.setText(mPokemon.getName());
			}


			String pokemonDisplayId = "#" + pokeId;
			mPokemonId.setText(pokemonDisplayId);

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if(prefs.getBoolean(this.getResources().getString(R.string.pref_image_key), true))
			{
				GlideApp.with(this).load(PokeAPIUtils.getArtworkUrl(pokeId))
					.error(GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId))
						.error(R.drawable.ic_poke_unknown))
					.placeholder(R.drawable.ic_poke_unknown).into(mArtwork);

				//sprites
				//GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId)).into(mFrontSprite);
				//GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId)).into(mBackSprite);
				//mBackSprite.setScaleX(-1); //rotates horizontal, could remove
			}
			else
			{
				mArtwork.setImageResource(R.drawable.ic_poke_unknown);
				//mFrontSprite.setImageResource(android.R.color.transparent);
				//mBackSprite.setImageResource(android.R.color.transparent);
			}
			setTitle(mPokemon.getName());


			if(mType1 == null)
			{
				showType(1, "unknown");
			}
			else
			{
				showType(1, mType1.getName());
			}
			if (numTypes < 2) {
				hideType2();
			} else
			{
				if(mType2 == null)
				{
					showType(2, "unknown");
				}
				else
				{
					showType(2, mType2.getName());
				}
			}
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

	private void hideType2() {
		mPokemonType2IV.setVisibility(View.GONE);
		mPokemonType2TV.setVisibility(View.GONE);
		mPokemonTypeSeperator.setVisibility(View.GONE);
	}

	private void showType(int index, String name) {
		TextView tv = mPokemonType1TV;
		ImageView iv = mPokemonType1IV;
		if (index == 2) {
			tv = mPokemonType2TV;
			iv = mPokemonType2IV;
		}

		tv.setText(name);
		AssetManager assets = this.getAssets();
		try {
			if (index == 2)
			{
				mPokemonTypeSeperator.setVisibility(View.VISIBLE);
			}
			tv.setVisibility(View.GONE);
			iv.setVisibility(View.VISIBLE);
			InputStream stream = assets.open(String.format(Locale.US, "types/%s.png", name));
			Drawable drawable = Drawable.createFromStream(stream, name);
			iv.setImageDrawable(drawable);
		} catch (IOException exc) {
			if (index == 2) {
				mPokemonTypeSeperator.setVisibility(View.GONE); //else overlaps type1 in text mode
			}
			iv.setImageResource(R.drawable.ic_poke_unknown); // TODO: display unknown.png instead
			iv.setVisibility(View.GONE);
			tv.setVisibility(View.VISIBLE);
		}
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
			case android.R.id.home:
				finish();
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

	public void updateFABStatus(boolean added) {
		if (added)
		{
			Log.d(TAG, "Added");
			mItemFAB.setImageResource(R.drawable.ic_status_remove); //remove
			mItemFAB.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorNegativeFAB));
			mItemFAB.hide();
			mItemFAB.show(); //fix google bug to show image icon
			mItemAdded = true;
		} else
		{
			Log.d(TAG, "Removed");
			mItemFAB.setImageResource(R.drawable.ic_action_add); //add to SQL
			mItemFAB.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
			mItemFAB.hide();
			mItemFAB.show(); //fix google bug to show image icon
			mItemAdded = false;
		}
	}

	public void addOrRemovePokemonFromTeam()
	{
		//final LiveData<Team> liveTeam = TeamUtils.getCurrentTeam(mPokeViewModel, mSavedTeamDao, prefs);
		if(!mItemAdded)
		{
			Log.d(TAG, String.format(Locale.US, "Adding %s...", mPokemon.getName()));
			TeamUtils.addPokemonToCurrentTeam(mSavedTeamRepo, mTeamId, mPokemon);
		}
		else
		{
			Log.d(TAG,  String.format(Locale.US,"Removing %s...", mPokemon.getName()));
			TeamUtils.removePokemonFromCurrentTeam(mSavedTeamRepo, mTeamId, mPokemon);
		}
	}

	public void onPokemonMoveClicked(int moveID){
		//Log.d(TAG, "Clicked" + moveID);
	}
}
