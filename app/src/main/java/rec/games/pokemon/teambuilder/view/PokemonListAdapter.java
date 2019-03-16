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

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.model.CollectionObserver;
import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.PokeAPIUtils;
import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.PokemonResource;

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
	private LiveDataList<Pokemon> mPokemon;
	private OnPokemonClickListener mListener;

	PokemonListAdapter(LiveDataList<Pokemon> pokemon, OnPokemonClickListener l)
	{
		this.mPokemon = pokemon;
		this.mListener = l;

		mPokemon.observeCollection(cacheNotifier);
	}

	public void updatePokemon(LiveDataList<Pokemon> pokemon)
	{
		this.mPokemon = pokemon;
		notifyDataSetChanged();

		mPokemon.observeCollection(cacheNotifier);
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
		return mPokemon.size();
	}

	public int getPokemonClickedId(int position)
	{
		Log.d(TAG, "position: " + position);
		if(position > 0 && mPokemon != null)
		{
			Log.d(TAG, Integer.toString(mPokemon.getValue(position).getId()));
			return mPokemon.getValue(position).getId(); //mPokemon ids start at 1
		}
		else
			return 1;
	}

	private boolean checkDisplayImages()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(context.getResources().getString(R.string.pref_image_key), true);
	}

	@Override
	public void onBindViewHolder(@NonNull PokemonViewHolder viewHolder, int i)
	{
		viewHolder.bind(mPokemon.getValue(i));
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
