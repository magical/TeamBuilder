package rec.games.pokemon.teambuilder.model;

import android.net.Uri;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.HashMap;

public class PokeAPIUtils
{
	public static final String POKE_ITEM = "rec.games.pokemon.teambuilder.Model.PokeAPIUtils";
	private final static String POKE_API_BASE_URL = "https://pokeapi.co/api/v2/";
	private final static String STATIC_POKE_API_BASE_URL = "https://raw.githubusercontent.com/PokeAPI/api-data/master/data/";

	private final static String POKE_API_POKEMON_ENDPOINT = "api/v2/pokemon";
	private final static String POKE_API_TYPE_ENDPOINT = "api/v2/type";
	private final static String POKE_API_MOVE_ENDPOINT = "api/v2/move";

	private final static String POKE_API_SPRITE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";
	private final static String POKE_API_ARTWORK_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other-sprites/official-artwork/";

	private final static String POKE_API_SPRITE_FILE_TYPE = ".png";
	private final static String STATIC_POKE_API_FILE_NAME = "index.json";

	static String buildNamedAPIResourceListURL(String endPoint)
	{
		return Uri.parse(STATIC_POKE_API_BASE_URL).buildUpon()
			.appendEncodedPath(endPoint)
			.appendPath(STATIC_POKE_API_FILE_NAME)
			.build()
			.toString();
	}

	public static String buildPokemonListURL()
	{
		return buildNamedAPIResourceListURL(POKE_API_POKEMON_ENDPOINT);
	}

	public static String buildTypeListURL()
	{
		return buildNamedAPIResourceListURL(POKE_API_TYPE_ENDPOINT);
	}

	public static String buildMoveListURL()
	{
		return buildNamedAPIResourceListURL(POKE_API_MOVE_ENDPOINT);
	}

	public static NamedAPIResourceList parseNamedAPIResourceListJSON(String namedAPIResourceListJSON)
	{
		Gson gson = new Gson();
		return gson.fromJson(namedAPIResourceListJSON, NamedAPIResourceList.class);
	}

	public static Pokemon parsePokemonJSON(String pokemonJSON)
	{
		Gson gson = new Gson();
		return gson.fromJson(pokemonJSON, Pokemon.class);
	}

	public static PokemonSpecies parsePokemonSpeciesJSON(String pokemonSpeciesJSON)
	{
		Gson gson = new Gson();
		return gson.fromJson(pokemonSpeciesJSON, PokemonSpecies.class);
	}

	public static Move parseMoveJSON(String moveJSON)
	{
		Gson gson = new Gson();
		return gson.fromJson(moveJSON, Move.class);
	}

	public static Type parseTypeJSON(String typeJSON)
	{
		Gson gson = new Gson();
		return gson.fromJson(typeJSON, Type.class);
	}

	public static int getId(String url)
	{
		if(url == null)
			return 0;

		String id = Uri.parse(url).getLastPathSegment();
		if(id != null)
			return Integer.parseInt(id);
		return 0;
	}

	public static String getSpriteUrl(int id)
	{
		return Uri.parse(POKE_API_SPRITE_URL).buildUpon()
			.appendEncodedPath(Integer.toString(id) + POKE_API_SPRITE_FILE_TYPE).build().toString();
	}

	public static String getArtworkUrl(int id)
	{
		return Uri.parse(POKE_API_ARTWORK_URL).buildUpon()
			.appendEncodedPath(Integer.toString(id) + POKE_API_SPRITE_FILE_TYPE).build().toString();
	}

	public static HashMap<String, String> createLocaleMap(Name[] names)
	{
		if(names.length == 0)
			return null;

		//key: locale, value: locale name
		HashMap<String, String> localeMap = new HashMap<>(names.length);
		for(Name name : names)
			localeMap.put(name.language.name, name.name);

		return localeMap;
	}

	public static String fixStaticAPIUrl(String url)
	{
		//the url.substring(1) is to avoid "//" from appearing in the url
		//while that is a valid Uri, don't want to risk a server not processing it correctly
		return Uri.parse(STATIC_POKE_API_BASE_URL).buildUpon()
			.appendEncodedPath(url.substring(1))
			.appendPath(STATIC_POKE_API_FILE_NAME)
			.toString();
	}

	public static class NamedAPIResourceList implements Serializable
	{
		public NamedAPIResource[] results;
		public int count;        //count is available
	}

	public static class NamedAPIResource implements Serializable
	{
		public String name;
		public String url;
	}

	public static class Name implements Serializable
	{
		public String name;
		public NamedAPIResource language;
	}

	public static class Pokemon implements Serializable
	{
		public int id;
		public String name;
		public PokemonMove[] moves;
		public NamedAPIResource species;
		public PokemonSprites sprites;
		public PokemonType[] types;
	}

	public static class PokemonMove implements Serializable
	{
		public NamedAPIResource move;
		//version_group_details is available
	}

	public static class PokemonSprites implements Serializable
	{
		public String front_default;
		public String back_default;
	}

	public static class PokemonType implements Serializable
	{
		public int slot;
		public NamedAPIResource type;
	}

	public static class PokemonSpecies implements Serializable
	{
		public Name[] names;
	}

	public static class Move implements Serializable
	{
		public int id;
		public String name;
		public int power;
		public Name[] names;
		public NamedAPIResource type;
	}

	public static class Type implements Serializable
	{
		public int id;
		public String name;
		public TypeRelations damage_relations;
		public Name[] names;
	}

	public static class TypeRelations implements Serializable
	{
		public NamedAPIResource[] no_damage_to;
		public NamedAPIResource[] half_damage_to;
		public NamedAPIResource[] double_damage_to;
		public NamedAPIResource[] no_damage_from;
		public NamedAPIResource[] half_damage_from;
		public NamedAPIResource[] double_damage_from;
	}
}
