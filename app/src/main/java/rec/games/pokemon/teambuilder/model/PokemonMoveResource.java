package rec.games.pokemon.teambuilder.model;

import android.arch.lifecycle.LiveData;

import java.util.HashMap;

public class PokemonMoveResource extends PokemonMove
{
	//TODO: for now use the resource name, but later we should use the locale specific names
	protected String resourceName;
	//similar to the ones described in PokemonTypes
	protected HashMap<String, String> localeNames;
	protected LiveData<PokemonType> type;
	protected Integer power;

	public PokemonMoveResource(int id, String resourceName, int power, LiveData<PokemonType> type, HashMap<String, String> localeNames)
	{
		super(id);

		this.resourceName = resourceName;
		this.power = power;
		this.type = type;
		this.localeNames = localeNames;
	}

	@Override
	public String getName()
	{
		return resourceName;
	}

	@Override
	public LiveData<PokemonType> getType()
	{
		return type;
	}

	@Override
	public Integer getPower()
	{
		return power;
	}

	@Override
	public boolean isLoaded()
	{
		if(type.getValue() == null)
			return false;

		return !type.getValue().isDeferred();
	}

	public String getLocaleName(String locale)
	{
		return localeNames.get(locale);
	}
}
