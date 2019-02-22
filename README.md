# Team 9 Final Project - Pokémon Team Builder
Created by: Andrew Ekstedt, Trevor Hammock and Miles Young

## Project Description
Our app will be a Pokémon team builder — a tool to help find analyze a team of 6 Pokémon for weaknesses. Every Pokémon has one or two elemental types; attacks can do between ¼× and 4× as much damage depending on the type of the attack and how it matches up with the defending Pokémon’s types. When building a team it is important for the team to be balanced, such that there are no types that all members are weak to.

## API
We plan to use PokéAPI for our project. PokéAPI is a RESTful API from which one can retrieve a wide variety of data about Pokémon in JSON format. It is a read-only API.

We will need to use at least the following endpoints:

 * /api/v2/pokemon - to get data about a Pokémon species, including its types
	* pokemon/{id or name} - for searching for specific Pokémon
 	* pokemon/?limit=20&offset=0 - to get a list of Pokemon by number
 * /api/v2/types - to get data about type match-ups
 * /api/v2/evolution-chain - to get the evolutions of the Pokémon