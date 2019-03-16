package rec.games.pokemon.teambuilder.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import rec.games.pokemon.teambuilder.R;

public class PokemonMoveAdapter extends RecyclerView.Adapter<PokemonMoveAdapter.PokemonViewHolder>
{
	private static final String TAG = PokemonListAdapter.class.getSimpleName();

	private Context context;
	private ArrayList<String> mPokemon;				//temporary placeholder
	private OnPokemonMoveClickListener mListener;
	private ArrayList<Integer> selected;
	private boolean mAllowMovesSelected;

	PokemonMoveAdapter(ArrayList<String> pokemon, OnPokemonMoveClickListener l, boolean allowMovesSelected)
	{
		this.mPokemon = pokemon;
		this.mListener = l;
		mAllowMovesSelected = allowMovesSelected;

		selected = new ArrayList<>();
	}

	public void updatePokemonMoves(ArrayList<String> pokemon)
	{
		this.mPokemon = pokemon;
		notifyDataSetChanged();
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
		View v = inf.inflate(R.layout.move_list_entry, parent, false);
		return new PokemonViewHolder(v, mListener);
	}

	@Override
	public int getItemCount()
	{
		return mPokemon.size();
	}

	public int getPokemonMoveClickId(int position)
	{
		if(mPokemon != null)
		{
			return position; //temporary
		}
		else
			return 0;
	}

	@Override
	public void onBindViewHolder(@NonNull PokemonViewHolder viewHolder, int i)
	{
		if(mAllowMovesSelected)
		{
			if(!selected.contains(i))
				viewHolder.setColor(viewHolder.itemView, R.color.colorNormalBackground, R.color.colorNormalText); //not selected
			else
				viewHolder.setColor(viewHolder.itemView, R.color.colorHighlightBackground, R.color.colorHighlightText); //selected
		}

		viewHolder.bind(mPokemon.get(i));
	}

	public interface OnPokemonMoveClickListener
	{
		void onPokemonMoveClicked(int pokeId);
	}

	class PokemonViewHolder extends RecyclerView.ViewHolder
	{
		private OnPokemonMoveClickListener mListener;
		private TextView mName;
		private TextView mType;
		private TextView mPower;

		public PokemonViewHolder(View view, OnPokemonMoveClickListener l)
		{
			super(view);
			mName = view.findViewById(R.id.move_name);
			mType = view.findViewById(R.id.move_type);
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

		public void bind(String moveName)
		{
			mName.setText(moveName);
			mType.setText("Type1234");
			mPower.setText(String.valueOf(150));
		}
	}
}
