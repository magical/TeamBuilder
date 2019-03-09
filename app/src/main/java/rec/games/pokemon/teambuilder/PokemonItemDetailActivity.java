package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.request.RequestOptions;

import java.util.HashMap;

public class PokemonItemDetailActivity extends AppCompatActivity
{
	private static final String TAG = PokemonItemDetailActivity.class.getSimpleName();

	private int pokeId;
	private Pokemon mPokemon;
	private ImageView mArtwork;
	private TextView mPokemonName;
	private FloatingActionButton mItemFAB;
	private boolean mItemAdded;
	private String mTeamName;

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

		if (intent != null && intent.hasExtra(PokeAPIUtils.POKE_ITEM))
		{
			pokeId = intent.getIntExtra(PokeAPIUtils.POKE_ITEM, pokeId);

			PokeAPIViewModel model = ViewModelProviders.of(this).get(PokeAPIViewModel.class);

			// Fill in with some fake data
			model.getPokemonCache().observe(this, new Observer<HashMap<Integer, LiveData<Pokemon>>>() {
				@Override
				public void onChanged(@Nullable HashMap<Integer, LiveData<Pokemon>> list) {
					Log.d(TAG, "Got value");
					if(list != null)
						mPokemon = list.get(pokeId).getValue();

					fillLayout();
				}
			});

			if (intent.hasExtra(getString(R.string.team_id_string))){
				mItemFAB.show();
				mTeamName = intent.getStringExtra(getString(R.string.team_id_string));
				Log.d(TAG, "Have Team " + mTeamName);

			}

		}
	}

	private void fillLayout(){
		if(pokeId > 0)
		{
			mPokemonName.setText(mPokemon.getName());

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			if (prefs.getBoolean(this.getResources().getString(R.string.pref_image_key), true))
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
				ifItemAddedFAB();
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
		switch (item.getItemId()){
			case R.id.action_share_poke_details:
				sharePokeDetails();
				return true;
			case R.id.action_browser:
				shareToBrowser();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public void sharePokeDetails(){
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

	public void shareToBrowser(){
		if(mPokemon != null) //placeholder data, need to replace
		{
			Intent intent = new Intent(Intent.ACTION_VIEW,
				PokeAPIUtils.getBulbapediaPage(mPokemon.getName()));
			if(intent.resolveActivity(getPackageManager())!=null){
				startActivity(intent);
			}
		}
	}

	public void ifItemAddedFAB(){
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
}
