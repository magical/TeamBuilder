package rec.games.pokemon.teambuilder.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Comparator;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.model.CollectionObserver;
import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonResource;
import rec.games.pokemon.teambuilder.model.SearchCriteria;

public class PokemonListAdapter extends RecyclerView.Adapter<PokemonListAdapter.PokemonViewHolder>
{
	private static final String TAG = PokemonListAdapter.class.getSimpleName();
	private final CollectionObserver<Pokemon> cacheNotifier = new CollectionObserver<Pokemon>()
	{
		@Override
		public void onItemChanged(@Nullable Pokemon pokemon, int index)
		{
			if(pokemon instanceof PokemonResource)
				notifyItemChanged(index);
		}
	};

	Context context;
	private LiveDataList<Pokemon> mPokemonList;
	private LiveDataList<Pokemon> mCurrentPokemonList;
	private OnPokemonClickListener mListener;

	PokemonListAdapter(LiveDataList<Pokemon> pokemon, OnPokemonClickListener l)
	{
		this.mPokemonList = pokemon;
		this.mListener = l;
		mCurrentPokemonList = new LiveDataList<>();
		this.mCurrentPokemonList = mPokemonList;

		mPokemonList.observeCollection(cacheNotifier);
	}

	public void updatePokemon(LiveDataList<Pokemon> pokemon)
	{
		this.mPokemonList = pokemon;
		this.mCurrentPokemonList = pokemon;
		notifyDataSetChanged();

		mPokemonList.observeCollection(cacheNotifier);
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		context = recyclerView.getContext();
	}

	@NonNull
	@Override
	public PokemonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		LayoutInflater inf = LayoutInflater.from(parent.getContext());
		View v = inf.inflate(R.layout.pokemon_list_entry, parent, false);
		return new PokemonViewHolder(v, mListener);
	}

	@Override
	public int getItemCount()
	{
		if (mCurrentPokemonList != null && mCurrentPokemonList.size() > 0)
			return mCurrentPokemonList.size();
		else
			return mPokemonList.size();
	}

	public int getPokemonClickedId(int position)
	{
		Log.d(TAG, "position: " + position);
		if(position > 0 && mCurrentPokemonList != null)
		{
			Log.d(TAG, Integer.toString(mCurrentPokemonList.getValue(position).getId()));
			return mCurrentPokemonList.getValue(position).getId(); //mPokemonList ids start at 1
		}
		else
			return 1;
	}

	private boolean checkDisplayImages()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getResources().getString(R.string.pref_image_key), true);
	}

	public void sortPokemonByName()
	{
		mPokemonList.sort(new Comparator<Pokemon>()
		{
			@Override
			public int compare(Pokemon pokemon1, Pokemon pokemon2)
			{
				return pokemon1.getName().compareTo(pokemon2.getName());
			}
		});
		mCurrentPokemonList.sort(new Comparator<Pokemon>()
		{
			@Override
			public int compare(Pokemon pokemon1, Pokemon pokemon2)
			{
				return pokemon1.getName().compareTo(pokemon2.getName());
			}
		});
		notifyDataSetChanged();
	}

	public void sortPokemonById()
	{
		mPokemonList.sort(new Comparator<Pokemon>()
		{
			@Override
			public int compare(Pokemon pokemon1, Pokemon pokemon2)
			{
				return pokemon1.getId() - pokemon2.getId();
			}
		});
		mCurrentPokemonList.sort(new Comparator<Pokemon>()
		{
			@Override
			public int compare(Pokemon pokemon1, Pokemon pokemon2)
			{
				return pokemon1.getId() - pokemon2.getId();
			}
		});
		notifyDataSetChanged();
	}

	public boolean searchPokemonName(final String searchTerm)
	{
		SearchCriteria searchCriteria = new SearchCriteria()
		{
			@Override
			public boolean match(@Nullable Object o)
			{
				if(o instanceof Pokemon)
					return (((Pokemon) o).getName().toLowerCase().contains(searchTerm));

				return false;
			}
		};
		return searchPokemon(searchCriteria);
	}

	public boolean searchPokemonId(final String searchTerm)
	{
		SearchCriteria searchCriteria = new SearchCriteria()
		{
			@Override
			public boolean match(@Nullable Object o)
			{
				if(o instanceof Pokemon)
					return (Integer.toString(((Pokemon) o).getId()).contains(searchTerm));

				return false;
			}
		};
		return searchPokemon(searchCriteria);
	}

	private boolean searchPokemon(SearchCriteria searchCriteria)
	{
		mCurrentPokemonList = mPokemonList.searchSubList(searchCriteria);
		if(mCurrentPokemonList !=null && mCurrentPokemonList.size() > 0)
		{
			Log.d(TAG, "new search view, size " + mCurrentPokemonList.size() + " vs "+ mPokemonList.size());
			notifyDataSetChanged();
			return true; //search successful
		}
		else
		{
			Toast.makeText(context, context.getString(R.string.pokemon_search_not_found), Toast.LENGTH_LONG).show();
			mCurrentPokemonList = mPokemonList;
			return false; //search unsuccessful
		}
	}

	public void clearSearchPokemon()
	{
		mCurrentPokemonList = mPokemonList;
		notifyDataSetChanged();
	}

	@Override
	public void onBindViewHolder(@NonNull PokemonViewHolder viewHolder, int i)
	{
		viewHolder.bind(mCurrentPokemonList.getValue(i));
	}

	public interface OnPokemonClickListener
	{
		void onPokemonClicked(int pokeId);
	}

	class PokemonViewHolder extends RecyclerView.ViewHolder
	{
		private OnPokemonClickListener mListener;
		private TextView mName;
		private TextView mId;
		private ImageView mIcon;

		public PokemonViewHolder(View view, OnPokemonClickListener l)
		{
			super(view);
			mName = view.findViewById(R.id.pokemon_name);
			mIcon = view.findViewById(R.id.pokemon_icon);
			mId = view.findViewById(R.id.pokemon_id);
			mListener = l;

			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mListener.onPokemonClicked(getPokemonClickedId(getAdapterPosition()));
				}
			});
		}

		public void bind(Pokemon p)
		{
			mName.setText(p.getName());
			mId.setText(String.valueOf(p.getId()));
			if(checkDisplayImages())
			{
				GlideApp.with(mIcon.getContext())
					.load(PokeAPIUtils.getSpriteUrl(p.getId()))
					.into(mIcon);
			}
			else
				GlideApp.with(mIcon.getContext()).load(R.drawable.ic_poke_unknown).into(mIcon);
		}
	}
}
