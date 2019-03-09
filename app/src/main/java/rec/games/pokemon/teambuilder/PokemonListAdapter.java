package rec.games.pokemon.teambuilder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PokemonListAdapter extends RecyclerView.Adapter<PokemonViewHolder>
{
	private LiveDataList<Pokemon> mPokemon;
	private OnPokemonClickListener mListener;

	private final CollectionObserver<Pokemon> cacheNotifier = new CollectionObserver<Pokemon>()
	{
		@Override
		public void onItemChanged(@Nullable Pokemon pokemon, int index)
		{
			if(pokemon instanceof PokemonResource)
				notifyItemChanged(index);
		}
	};

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

	@Override
	public void onBindViewHolder(@NonNull PokemonViewHolder viewHolder, int i)
	{
		viewHolder.bind(mPokemon.getValue(i));
	}
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
				mListener.onPokemonClicked(getAdapterPosition());
			}
		});
	}

	public void bind(Pokemon p)
	{
		mName.setText(p.getName());
		mId.setText(String.valueOf(p.getId()));
		GlideApp.with(mIcon.getContext())
			.load(PokeAPIUtils.getSpriteUrl(p.getId()))
			.into(mIcon);
	}
}

interface OnPokemonClickListener
{
	void onPokemonClicked(int position);
}