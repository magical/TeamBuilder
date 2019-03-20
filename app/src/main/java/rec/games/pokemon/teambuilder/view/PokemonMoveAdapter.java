package rec.games.pokemon.teambuilder.view;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import rec.games.pokemon.teambuilder.R;
import rec.games.pokemon.teambuilder.model.CollectionObserver;
import rec.games.pokemon.teambuilder.model.LiveDataList;
import rec.games.pokemon.teambuilder.model.PokemonMove;
import rec.games.pokemon.teambuilder.model.PokemonMoveResource;

public class PokemonMoveAdapter extends RecyclerView.Adapter<PokemonMoveAdapter.PokemonMoveViewHolder>
{
	private static final String TAG = PokemonListAdapter.class.getSimpleName();
	private final CollectionObserver<PokemonMove> cacheNotifier = new CollectionObserver<PokemonMove>()
	{
		@Override
		public void onItemChanged(@Nullable PokemonMove pokemonMove, int index)
		{
			if(pokemonMove instanceof PokemonMoveResource)
				notifyItemChanged(index);
		}
	};

	private Context context;
	private LiveDataList<PokemonMove> mMoveList;
	private OnPokemonMoveClickListener mListener;
	private ArrayList<Integer> selected;
	private boolean mAllowMovesSelected;

	PokemonMoveAdapter(List<LiveData<PokemonMove>> moveList, OnPokemonMoveClickListener l, boolean allowMovesSelected)
	{
		this.mMoveList = new LiveDataList<>(moveList);
		this.mListener = l;
		mAllowMovesSelected = allowMovesSelected;
		selected = new ArrayList<>();

		mMoveList.observeCollection(cacheNotifier);
	}

	public void updatePokemonMoves(List<LiveData<PokemonMove>> moveList)
	{
		this.mMoveList = new LiveDataList<>(moveList);
		notifyDataSetChanged();

		mMoveList.observeCollection(cacheNotifier);
	}

	@Override
	public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView)
	{
		super.onAttachedToRecyclerView(recyclerView);
		context = recyclerView.getContext();
	}

	@NonNull
	@Override
	public PokemonMoveViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
	{
		LayoutInflater inf = LayoutInflater.from(parent.getContext());
		View v = inf.inflate(R.layout.move_list_entry, parent, false);
		return new PokemonMoveViewHolder(v, mListener);
	}

	@Override
	public int getItemCount()
	{
		return mMoveList.size();
	}

	public int getPokemonMoveClickId(int position)
	{
		return position;
	}

	@Override
	public void onBindViewHolder(@NonNull PokemonMoveViewHolder viewHolder, int i)
	{
		if(mAllowMovesSelected)
		{
			if(!selected.contains(i))
				viewHolder.setColor(viewHolder.itemView, R.color.colorNormalBackground, R.color.colorNormalText); //not selected
			else
				viewHolder.setColor(viewHolder.itemView, R.color.colorHighlightBackground, R.color.colorHighlightText); //selected
		}

		viewHolder.bind(mMoveList.getValue(i));
	}

	public interface OnPokemonMoveClickListener
	{
		void onPokemonMoveClicked(int pokeId);
	}

	class PokemonMoveViewHolder extends RecyclerView.ViewHolder
	{
		private OnPokemonMoveClickListener mListener;
		private TextView mName;
		private TextView mType;
		private ImageView mTypeIV;
		private TextView mPower;

		public PokemonMoveViewHolder(View view, OnPokemonMoveClickListener l)
		{
			super(view);
			mName = view.findViewById(R.id.move_name);
			mType = view.findViewById(R.id.move_type);
			mTypeIV = view.findViewById(R.id.move_type_iv);
			mPower = view.findViewById(R.id.move_power);
			mListener = l;

			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					int position = getAdapterPosition();

					if(mAllowMovesSelected)
					{
						if(selected.contains(getAdapterPosition()))
						{
							setColor(v, R.color.colorHighlightBackground, R.color.colorHighlightText);
							selected.remove(Integer.valueOf(position));
						}
						else
						{
							setColor(v, R.color.colorNormalBackground, R.color.colorNormalText);
							selected.add(position);
						}

						notifyItemChanged(position);
					}
					mListener.onPokemonMoveClicked(getPokemonMoveClickId(position));
				}
			});
		}

		private void setColor(View v, int background, int text)
		{
			v.setBackgroundColor(ContextCompat.getColor(context, background));
			mName.setTextColor(ContextCompat.getColor(context, text));
			mType.setTextColor(ContextCompat.getColor(context, text));
			mPower.setTextColor(ContextCompat.getColor(context, text));
		}

		public void bind(PokemonMove pokemonMove)
		{
			if(pokemonMove instanceof PokemonMoveResource)
			{
				PokemonMoveResource move = (PokemonMoveResource) pokemonMove;

				String typeName = move.getType().getValue().getName();
				mName.setText(move.getLocaleName("en"));
				mType.setText(typeName);
				mPower.setText(String.valueOf(move.getPower()));

				AssetManager assets = context.getAssets();

				try
				{
					mType.setVisibility(View.GONE);
					mTypeIV.setVisibility(View.VISIBLE);
					InputStream stream = assets.open(String.format(Locale.US, "types/%s.png", typeName));
					Drawable drawable = Drawable.createFromStream(stream, typeName + ".png");
					mTypeIV.setImageDrawable(drawable);
				}
				catch(IOException exc)
				{
					mTypeIV.setImageResource(R.drawable.ic_poke_unknown);
					mTypeIV.setVisibility(View.GONE);
					mType.setVisibility(View.VISIBLE);
				}
			}
			else
			{
				mName.setText(pokemonMove.getName());
			}
		}
	}
}
