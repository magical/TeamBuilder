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

public class PokemonItemDetailActivity extends AppCompatActivity
{
	private static final String TAG = PokemonItemDetailActivity.class.getSimpleName();

	private int tempArrayId; //replace with actual details of pokemon, is placeholder
	private ImageView mArtwork;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_item_detail);
		mArtwork = findViewById(R.id.iv_pokemon_detail_artwork);
		tempArrayId = -1;

		Intent intent = getIntent();

		if (intent != null && intent.hasExtra(PokeAPIUtils.POKE_ITEM))
		{
			tempArrayId = intent.getIntExtra(PokeAPIUtils.POKE_ITEM, 25); //default to pikachu
			GlideApp.with(this).load(PokeAPIUtils.getArtworkUrl(tempArrayId +1)).placeholder(R.drawable.ic_poke_unknown).into(mArtwork);

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
		if(tempArrayId > -1) //fake null - TODO - replace
		{
			String pokeDetails = Integer.toString(tempArrayId);

			ShareCompat.IntentBuilder.from(this)
				.setType("text/plain")
				.setText(pokeDetails)
				.setChooserTitle(R.string.share_chooser_poke_details)
				.startChooser();
		}
	}

	public void shareToBrowser(){
		if(tempArrayId > 0) //placeholder data, need to replace
		{
			Intent intent = new Intent(Intent.ACTION_VIEW, PokeAPIUtils.getBulbapediaPage(Integer.toString(tempArrayId)));
			if(intent.resolveActivity(getPackageManager())!=null){
				startActivity(intent);
			}
		}
	}
}
