package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

public class PokemonItemDetailActivity extends AppCompatActivity
{
	private static final String TAG = PokemonItemDetailActivity.class.getSimpleName();
	private int pokeId;
	private Pokemon mPokemon;
	private ImageView mArtwork;
	private TextView mPokemonName;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_item_detail);
		mArtwork = findViewById(R.id.iv_pokemon_detail_artwork);
		mPokemonName = findViewById(R.id.tv_pokemon_detail_name);

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
		}
	}

	private void fillLayout(){
		if(pokeId > 0)
		{
			mPokemonName.setText(mPokemon.getName());
			GlideApp.with(this).load(PokeAPIUtils.getArtworkUrl(pokeId))
				.error(GlideApp.with(this).load(PokeAPIUtils.getSpriteUrl(pokeId)))
				.placeholder(R.drawable.ic_poke_unknown).into(mArtwork);
			setTitle(mPokemon.getName());
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
}
