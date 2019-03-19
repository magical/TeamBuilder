package rec.games.pokemon.teambuilder.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
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
import java.util.Locale;

import rec.games.pokemon.teambuilder.R;

public class PokemonTypeAdapter extends RecyclerView.Adapter<PokemonTypeAdapter.PokemonViewHolder>
{
	private static final String TAG = PokemonListAdapter.class.getSimpleName();

	Context context;
	private ArrayList<TypeInfo> mTypeInfo;
	private OnPokemonTypeClickListener mListener;

	PokemonTypeAdapter(ArrayList<TypeInfo> pokemon, OnPokemonTypeClickListener l)
	{
		this.mTypeInfo = pokemon;
		this.mListener = l;
	}

	public void updatePokemonTypes(ArrayList<TypeInfo> pokemon)
	{
		this.mTypeInfo = pokemon;
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
		return mTypeInfo.size();
	}

	public int getPokemonTypeClickId(int position)
	{
		if(mTypeInfo != null)
		{
			return position; //temporary
		}
		else
			return 0;
	}

	@Override
	public void onBindViewHolder(@NonNull PokemonViewHolder viewHolder, int i)
	{
		viewHolder.bind(mTypeInfo.get(i));
	}

	public interface OnPokemonTypeClickListener
	{
		void onPokemonTypeClicked(int pokeId);
	}

	class PokemonViewHolder extends RecyclerView.ViewHolder
	{
		private OnPokemonTypeClickListener mListener;
		private TextView mTypeName;
		private ImageView mTypeImage;
		private TextView mTypeWeakness;
		private TextView mTypeResistance;

		public PokemonViewHolder(View view, OnPokemonTypeClickListener l)
		{
			super(view);
			mTypeName = view.findViewById(R.id.type_name);
			mTypeImage = view.findViewById(R.id.type_name_iv);
			mTypeWeakness = view.findViewById(R.id.type_weakness);
			mTypeResistance = view.findViewById(R.id.type_resistance);
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

		public void bind(TypeInfo typeInfo)
		{
			int typeWeak = typeInfo.weak;
			int typeStrong = typeInfo.strong;
			String typeName = typeInfo.type.getName();

			// TODO: use localeName
			mTypeName.setText(typeName);

			// TODO
			mTypeWeakness.setText(String.valueOf(typeWeak));
			mTypeResistance.setText(String.valueOf(typeStrong));

			if(typeWeak > 2){
				mTypeWeakness.setBackgroundColor(ContextCompat.getColor(context, R.color.colorHighlightBackground));
				mTypeWeakness.setTextColor(ContextCompat.getColor(context, R.color.colorHighlightText));
			}
			else {
				mTypeWeakness.setBackgroundColor(ContextCompat.getColor(context, R.color.colorNormalBackground));
				mTypeWeakness.setTextColor(ContextCompat.getColor(context, R.color.colorNormalText));
			}

			AssetManager assets = context.getAssets();

			try {
				mTypeName.setVisibility(View.GONE);
				mTypeImage.setVisibility(View.VISIBLE);
				InputStream stream = assets.open(String.format(Locale.US, "types/%s.png", typeName));
				Drawable drawable = Drawable.createFromStream(stream, typeName+".png");
				mTypeImage.setImageDrawable(drawable);
			} catch (IOException exc) {
				mTypeImage.setImageResource(R.drawable.ic_poke_unknown);
				mTypeImage.setVisibility(View.GONE);
				mTypeName.setVisibility(View.VISIBLE);
			}
		}
	}

}
