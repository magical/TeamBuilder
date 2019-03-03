package rec.games.pokemon.teambuilder;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

public class PokemonItemDetailActivity extends AppCompatActivity
{
	private static final String TAG = PokemonItemDetailActivity.class.getSimpleName();

	private int ArrayId;
	private ImageView mArtwork;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_item_detail);
		mArtwork = findViewById(R.id.iv_pokemon_detail_artwork);

		Intent intent = getIntent();

		if (intent != null && intent.hasExtra(PokeAPIUtils.POKE_ITEM)){
			ArrayId = intent.getIntExtra(PokeAPIUtils.POKE_ITEM, 25); //default to pikachu
			GlideApp.with(this).load(PokeAPIUtils.getArtworkUrl(ArrayId+1)).placeholder(R.drawable.ic_poke_unknown).into(mArtwork);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.pokemon_list_detail, menu);
		return true;
	}
}
