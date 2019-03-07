package rec.games.pokemon.teambuilder;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class PokemonItemDetailActivity extends AppCompatActivity
{
	private static final String TAG = PokemonItemDetailActivity.class.getSimpleName();
	private Pokemon mPokeShortDetails;
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
			mPokeShortDetails = (Pokemon)intent.getSerializableExtra(PokeAPIUtils.POKE_ITEM);
			fillLayout();
		}
	}

	private void fillLayout(){
		if(mPokeShortDetails != null)
		{
			mPokemonName.setText(mPokeShortDetails.getName());
			GlideApp.with(this).load(PokeAPIUtils.getArtworkUrl(mPokeShortDetails.getId())).placeholder(R.drawable.ic_poke_unknown).into(mArtwork);
			setTitle(mPokeShortDetails.getName());
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
		if(mPokeShortDetails != null) //fake null - TODO - replace
		{
			String pokeDetails = mPokeShortDetails.getName() + " (" +
				Integer.toString(mPokeShortDetails.getId()) + ")";

			ShareCompat.IntentBuilder.from(this)
				.setType("text/plain")
				.setText(pokeDetails)
				.setChooserTitle(R.string.share_chooser_poke_details)
				.startChooser();
		}
	}

	public void shareToBrowser(){
		if(mPokeShortDetails != null) //placeholder data, need to replace
		{
			Intent intent = new Intent(Intent.ACTION_VIEW,
				PokeAPIUtils.getBulbapediaPage(mPokeShortDetails.getName()));
			if(intent.resolveActivity(getPackageManager())!=null){
				startActivity(intent);
			}
		}
	}
}
