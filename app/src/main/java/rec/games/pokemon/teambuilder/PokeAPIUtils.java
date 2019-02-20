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
