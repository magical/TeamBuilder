package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity
{
	private static final String TAG = MainActivity.class.getSimpleName();

	private PokeAPIViewModel mViewModel;
	private RecyclerView rv;

	private Toolbar toolbar;
	private TabLayout tabLayout;
	private ViewPager viewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		toolbar = findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);

		if (getSupportActionBar() != null)
		{
			ActionBar actionBar = getSupportActionBar();
			actionBar.setElevation(0);
			actionBar.setHomeButtonEnabled(true);
		}

		viewPager = findViewById(R.id.main_viewpager);
		ViewPagerAdapter adapterVP = new ViewPagerAdapter(getSupportFragmentManager());
		adapterVP.addFragment(new TeamListFragment(), "Teams"); //tab
		adapterVP.addFragment(new PokemonListFragment(), "Pokémon"); //tab, title in caps
		viewPager.setAdapter(adapterVP);

		tabLayout = findViewById(R.id.main_tabs);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.tabIndicatorColor)); //b/c API of just getColor() needs >=23
	}

	class ViewPagerAdapter extends FragmentPagerAdapter
	{
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