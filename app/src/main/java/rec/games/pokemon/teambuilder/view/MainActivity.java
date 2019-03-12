package rec.games.pokemon.teambuilder.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

public class MainActivity extends AppCompatActivity
	implements SharedPreferences.OnSharedPreferenceChangeListener
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private PokeAPIViewModel mViewModel;
	private RecyclerView rv;

	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private ViewPagerAdapter adapterVP;

	private SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);

		if(getSupportActionBar() != null)
		{
			ActionBar actionBar = getSupportActionBar();
			actionBar.setElevation(0);
			actionBar.setHomeButtonEnabled(true);
		}

		RequestOptions requestOptions = new RequestOptions()
			.placeholder(R.drawable.ic_poke_unknown)
			.error(R.drawable.ic_poke_unknown)
			.fallback(R.drawable.ic_poke_unknown)
			.diskCacheStrategy(DiskCacheStrategy.ALL);
		GlideApp.with(this).setDefaultRequestOptions(requestOptions);

		viewPager = findViewById(R.id.main_viewpager);
		adapterVP = new ViewPagerAdapter(getSupportFragmentManager());
		adapterVP.addFragment(new TeamListFragment(), "Teams"); //tab
		adapterVP.addFragment(new PokemonListFragment(), "Pokémon"); //tab, title in caps
		viewPager.setAdapter(adapterVP);

		tabLayout = findViewById(R.id.main_tabs);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.tabIndicatorColor)); //b/c API of just getColor() needs >=23

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		preferences.registerOnSharedPreferenceChangeListener(this);
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

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
	{
		adapterVP.refreshFragment(); //refreshes all fragments
	}

	@Override
	protected void onDestroy()
	{
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	class ViewPagerAdapter extends FragmentPagerAdapter
	{
		private final List<Fragment> mFragmentList = new ArrayList<>();
		private final List<String> mFragmentTitleList = new ArrayList<>();

		public ViewPagerAdapter(FragmentManager manager)
		{
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

		public void addFragment(Fragment fragment, String title)
		{
			mFragmentList.add(fragment);
			mFragmentTitleList.add(title);
		}

		public void refreshFragment()
		{
			for(Fragment fragment:mFragmentList)
			{
				getSupportFragmentManager().beginTransaction().setAllowOptimization(false)
					.detach(fragment).attach(fragment).commitAllowingStateLoss();
			}

		}

		public CharSequence getPageTitle(int i)
		{
			return mFragmentTitleList.get(i);
		}
	}
}