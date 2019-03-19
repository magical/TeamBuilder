package rec.games.pokemon.teambuilder.view;

import rec.games.pokemon.teambuilder.model.PokemonTypeResource;

// TypeInfo is used by PokemonTypeAdapter. It associates a type with an rating of how the team stacks up against that type.
public class TypeInfo
{
	PokemonTypeResource type;
	int strong = 0; // number of pokemon on team strong against this element
	int weak = 0; // number of pokemon on team weak against this element
}
