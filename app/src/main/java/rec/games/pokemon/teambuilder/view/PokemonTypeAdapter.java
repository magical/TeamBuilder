package rec.games.pokemon.teambuilder.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import rec.games.pokemon.teambuilder.R;

public class PokemonTypeAdapter extends RecyclerView.Adapter<PokemonTypeAdapter.PokemonViewHolder>
{
	private static final String TAG = PokemonListAdapter.class.getSimpleName();

	Context context;
	private ArrayList<String> mPokemon;				//temporary placeholder
	private OnPokemonTypeClickListener mListener;

	PokemonTypeAdapter(ArrayList<String> pokemon, OnPokemonTypeClickListener l)
	{
		this.mPokemon = pokemon;
		this.mListener = l;
	}

	public void updatePokemonTypes(ArrayList<String> pokemon)
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
		View v = inf.inflate(R.layout.type_list, parent, false);
		return new PokemonViewHolder(v, mListener);
	}

	@Override
	public int getItemCount()
	{
		return mPokemon.size();
	}

	public int getPokemonTypeClickId(int position)
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

	public interface OnPokemonTypeClickListener
	{
		void onPokemonTypeClicked(int pokeId);
	}

	class PokemonViewHolder extends RecyclerView.ViewHolder
	{
		private OnPokemonTypeClickListener mListener;
		private TextView mTypeName;
		private TextView mTypePower;

		public PokemonViewHolder(View view, OnPokemonTypeClickListener l)
		{
			super(view);
			mTypeName = view.findViewById(R.id.type_name);
			mTypePower = view.findViewById(R.id.type_power);
			mListener = l;

			view.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					mListener.onPokemonTypeClicked(getPokemonTypeClickId(getAdapterPosition()));
				}
			});
		}

		public void bind(String typeName)
		{
			mTypeName.setText(typeName);
			mTypePower.setText("Strong");
		}
	}

}
