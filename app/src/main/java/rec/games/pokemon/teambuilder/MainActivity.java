package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnPokemonClickListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private PokeAPIViewModel mViewModel;
	private RecyclerView rv;

	private ProgressBar mLoadingPB;
	private TextView mLoadingErrorMsgTV;

	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		//https://www.androidhive.info/2015/09/android-material-design-working-with-tabs/
		toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);
		//getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		viewPager = (ViewPager) findViewById(R.id.main_viewpager);
		ViewPagerAdapter adapterVP = new ViewPagerAdapter(getSupportFragmentManager());
		adapterVP.addFragment(new PokemonListFragment(), "Pokemon");
		adapterVP.addFragment(new PokemonListFragment(), "Teams");
		viewPager.setAdapter(adapterVP);

		tabLayout = (TabLayout) findViewById(R.id.main_tabs);
		tabLayout.setupWithViewPager(viewPager);


/*
		RequestOptions requestOptions = new RequestOptions()
			.placeholder(R.drawable.ic_poke_unknown)
			.error(R.drawable.ic_poke_unknown)
			.fallback(R.drawable.ic_poke_unknown)
			.diskCacheStrategy(DiskCacheStrategy.ALL);
		GlideApp.with(this).setDefaultRequestOptions(requestOptions);

		//mLoadingPB = findViewById(R.id.pb_loading_circle);
		//mLoadingErrorMsgTV = findViewById(R.id.tv_loading_error);
		//mLoadingPB.setVisibility(View.VISIBLE);

		final PokemonListAdapter adapter = new PokemonListAdapter(new ArrayList<Pokemon>(), this);

		mViewModel = ViewModelProviders.of(this).get(PokeAPIViewModel.class);
		mViewModel.getPokeListJSON().observe(this, new Observer<String>()
		{
			@Override
			public void onChanged(@Nullable String pokemonListJSON)
			{
				if(pokemonListJSON == null)
				{
					Log.d(TAG, "Could not load PokemonList JSON");
					//mLoadingPB.setVisibility(View.INVISIBLE);
					//mLoadingErrorMsgTV.setVisibility(View.VISIBLE);
					//rv.setVisibility(View.INVISIBLE);
					return;
				}
				else
				{
					//mLoadingPB.setVisibility(View.INVISIBLE);
					//mLoadingErrorMsgTV.setVisibility(View.INVISIBLE);
					//rv.setVisibility(View.VISIBLE);
				}
				//Log.d(TAG, "JSON: " + pokemonListJSON);
				PokeAPIUtils.NamedAPIResourceList apiPokemonList = PokeAPIUtils.parsePokemonListJSON(pokemonListJSON);
				//Log.d(TAG, apiPokemonList.toString());
        		int limit = PokeAPIUtils.getPokeId(apiPokemonList.results[apiPokemonList.results.length-1].url);
				int lastPoke = apiPokemonList.results.length - (limit - 10_000);
				Log.d(TAG, "Count is: " + apiPokemonList.count + " of " + limit + " Last ID = " + lastPoke);

				List<Pokemon> pokemon = new ArrayList<>();
				for(PokeAPIUtils.NamedAPIResource r : apiPokemonList.results)
				{
					Pokemon p = new DeferredPokemonResource(PokeAPIUtils.getPokeId(r.url), r.name, r.url);
					pokemon.add(p);
				}
				adapter.updatePokemon(pokemon);
			}
		});


		//rv = findViewById(R.id.pokemon_list);
		//rv.setAdapter(adapter);
		//rv.setLayoutManager(new LinearLayoutManager(this));
		//rv.setItemAnimator(new DefaultItemAnimator());

		loadPokemonList();

		*/
	}

	class ViewPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragmentList = new ArrayList<>();
		private final List<String> mFragmentTitleList = new ArrayList<>();

		public ViewPagerAdapter(FragmentManager manager){
			super(manager);
		}

		@Override
		public Fragment getItem(int i)
		{
			return mFragmentList.get(i);
		}

		@Override
		public int getCount()
		{
			return mFragmentList.size();
		}

		public void addFragment(Fragment fragment, String title){
			mFragmentList.add(fragment);
			mFragmentTitleList.add(title);
		}

		public CharSequence getPageTitle(int i){
			return mFragmentTitleList.get(i);
		}
	}

	public void loadPokemonList()
	{
		String pokemonListURL = PokeAPIUtils.buildPokemonListURL(10000, 0);
		Log.d(TAG, "URL: " + pokemonListURL);

		mViewModel.loadPokemonListJSON(pokemonListURL);
	}

	@Override
	public void onPokemonClicked(int position)
	{
		// TODO
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
			case R.id.action_settings:
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
