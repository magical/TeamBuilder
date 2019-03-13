package rec.games.pokemon.teambuilder.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

	Context context;
	private ArrayList<String> mPokemon;				//temporary placeholder
	private OnPokemonMoveClickListener mListener;

	PokemonMoveAdapter(ArrayList<String> pokemon, OnPokemonMoveClickListener l)
	{
		this.mPokemon = pokemon;
		this.mListener = l;
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

	public int getPokemonClickedId(int position)
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
		private TextView mPP;
		private TextView mPower;
		private TextView mAccuracy;

		public PokemonViewHolder(View view, OnPokemonMoveClickListener l)
		{
			super(view);
			mName = view.findViewById(R.id.move_name);
			mType = view.findViewById(R.id.move_type);
			mPP = view.findViewById(R.id.move_pp);
			mPower = view.findViewById(R.id.move_power);
			mAccuracy = view.findViewById(R.id.move_accuracy);
			mListener = l;

			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mListener.onPokemonMoveClicked(getPokemonClickedId(getAdapterPosition()));
				}
			});
		}

		public void bind(String moveName)
		{
			mName.setText(moveName);
			mType.setText("Type1234");
			mPP.setText(String.valueOf(35));
			mPower.setText(String.valueOf(150));
			String outputAccuracy = String.valueOf(100) + "%";
			mAccuracy.setText(outputAccuracy);
		}
	}
}
