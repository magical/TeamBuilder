package rec.games.pokemon.teambuilder.model;

import okhttp3.Callback;

public abstract class PriorityCallback implements Callback
{
	public abstract boolean onStart();
}
