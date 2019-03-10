package rec.games.pokemon.teambuilder.Model;

import android.net.Uri;

import com.google.gson.Gson;

import java.io.Serializable;

public class PokeAPIUtils
{
	public static final String POKE_ITEM = "rec.games.pokemon.teambuilder.Model.PokeAPIUtils";
	private final static String POKE_API_BASE_URL = "https://pokeapi.co/api/v2/";
	private final static String POKE_API_LIMIT_PARAM = "limit";
	private final static String POKE_API_OFFSET_PARAM = "offset";

	private final static String POKE_API_POKEMON_ENDPOINT = "pokemon";
	private final static String POKE_API_TYPE_ENDPOINT = "type";
	private final static String POKE_API_MOVE_ENDPOINT = "move";

	private final static String POKE_API_SPRITE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/";
	private final static String POKE_API_ARTWORK_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other-sprites/official-artwork/";
	private final static String POKE_API_SPRITE_FILE_TYPE = ".png";


	private final static String POKE_BULBAPEDIA_URL = "https://bulbapedia.bulbagarden.net/wiki/";
	private final static String POKE_BULBAPEDIA_END = "_(Pokémon)";

	public static String buildNamedAPIResourceListURL(String endPoint, int limit, int offset)
	{
		return Uri.parse(POKE_API_BASE_URL).buildUpon()
			.appendPath(endPoint)
			.appendQueryParameter(POKE_API_LIMIT_PARAM, String.valueOf(limit))
			.appendQueryParameter(POKE_API_OFFSET_PARAM, String.valueOf(offset))
			.build()
			.toString();
	}

	public static String buildPokemonListURL(int limit, int offset)
	{
		return buildNamedAPIResourceListURL(POKE_API_POKEMON_ENDPOINT, limit, offset);
	}

	public static String buildTypeListURL(int limit, int offset)
	{
		return buildNamedAPIResourceListURL(POKE_API_TYPE_ENDPOINT, limit, offset);
	}

	public static String buildMoveListURL(int limit, int offset)
	{
		return buildNamedAPIResourceListURL(POKE_API_MOVE_ENDPOINT, limit, offset);
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

	public static Uri getBulbapediaPage(String poke)
	{
		//takes in string of Pokemon name
		return Uri.parse(POKE_BULBAPEDIA_URL).buildUpon()
			.appendEncodedPath(poke + POKE_BULBAPEDIA_END).build();
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
