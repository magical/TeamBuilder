package rec.games.pokemon.teambuilder;

import java.io.Serializable;

// Pokemon is a data class representing a pokemon
public class Pokemon implements Serializable
{
	public String url;
	public String identifier;
	public String id;

	public Pokemon()
	{
	}

	// For debugging...
	Pokemon(String ident)
	{
		this.identifier = ident;
	}
}
