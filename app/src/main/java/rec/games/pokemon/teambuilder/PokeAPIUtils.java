package rec.games.pokemon.teambuilder;

import android.net.Uri;

import com.google.gson.Gson;

import java.io.Serializable;

public class PokeAPIUtils
{
	private final static String POKE_API_BASE_URL = "https://pokeapi.co/api/v2/";
	private final static String POKE_API_POKEMON_ENDPOINT = "pokemon";
	private final static String POKE_API_LIMIT_PARAM = "limit";
	private final static String POKE_API_OFFSET_PARAM = "offset";

	static class NamedAPIResourceList implements Serializable
	{
		//count is available
		NamedAPIResource[] results;
	}

	static class NamedAPIResource implements Serializable
	{
		String name;
		String url;
	}

	static class Name implements Serializable
	{
		String name;
		NamedAPIResource language;
	}

	static class Pokemon implements Serializable
	{
		int id;
		String name;
		PokemonMove[] moves;
		PokemonSprites sprites;
		PokemonType[] types;
	}

	static class PokemonMove implements Serializable
	{
		NamedAPIResource move;
		//version_group_details is available
	}

	static class PokemonSprites implements Serializable
	{
		String front_default;
		String back_default;
	}

	static class PokemonType implements Serializable
	{
		int slot;
		NamedAPIResource type;
	}

	static class Move implements Serializable
	{
		int id;
		String name;
		int power;
		Name[] names;
		NamedAPIResource type;
	}

	static class Type implements Serializable
	{
		int id;
		String name;
		TypeRelations damage_relations;
		Name[] names;
	}

	static class TypeRelations implements Serializable
	{
		NamedAPIResource[] no_damage_to;
		NamedAPIResource[] half_damage_to;
		NamedAPIResource[] double_damage_to;
		NamedAPIResource[] no_damage_from;
		NamedAPIResource[] half_damage_from;
		NamedAPIResource[] double_damage_from;
	}

	static String buildNamedAPIResourceListURL(String endPoint, int limit, int offset)
	{
		return Uri.parse(POKE_API_BASE_URL).buildUpon()
			.appendPath(endPoint)
			.appendQueryParameter(POKE_API_LIMIT_PARAM, String.valueOf(limit))
			.appendQueryParameter(POKE_API_OFFSET_PARAM, String.valueOf(offset))
			.build()
			.toString();
	}

	static String buildPokemonListURL(int limit, int offset)
	{
		return buildNamedAPIResourceListURL(POKE_API_POKEMON_ENDPOINT, limit, offset);
	}

	static NamedAPIResourceList parsePokemonListJSON(String pokemonListJSON)
	{
		Gson gson = new Gson();
		return gson.fromJson(pokemonListJSON, NamedAPIResourceList.class);
	}
}
