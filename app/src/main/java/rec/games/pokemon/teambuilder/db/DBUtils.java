package rec.games.pokemon.teambuilder.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;

import rec.games.pokemon.teambuilder.PokeAPIViewModel;
import rec.games.pokemon.teambuilder.Pokemon;
import rec.games.pokemon.teambuilder.Team;
import rec.games.pokemon.teambuilder.TeamMember;

public class DBUtils
{
	public static boolean isPokemonInCurrentTeam(SharedPreferences prefs, int pokemonId)
	{
		int currentTeamId = getCurrentTeamId(prefs);
		return isPokemonInTeam(currentTeamId, pokemonId);
	}

	public static boolean isPokemonInTeam(int teamId, int pokemonId)
	{
		return false; // TODO
	}

	public static LiveData<Team> getTeam(PokeAPIViewModel viewModel, SavedTeamDao dao, int teamId)
	{
		final PokeAPIViewModel pokeapi = viewModel;
		LiveData<SavedTeam> liveSavedTeam = dao.getTeamById(teamId);
		return Transformations.map(liveSavedTeam, savedTeam ->
		{
			Team team = new Team();
			for(int pokemonId : savedTeam.memberIds)
			{
				TeamMember m = new TeamMember();
				m.pokemon = pokeapi.getPokemonById(pokemonId);
				team.members.add(m);
			}
			return team;
		});
	}

	public static LiveData<Team> getCurrentTeam(PokeAPIViewModel viewModel, SavedTeamDao dao, SharedPreferences prefs)
	{
		int currentTeamId = getCurrentTeamId(prefs);
		return getTeam(viewModel, dao, currentTeamId);
	}

	private static int getCurrentTeamId(SharedPreferences prefs)
	{
		return 1; // TODO: get from prefs
	}

	public static void addPokemonToCurrentTeam(SavedTeamDao dao, SharedPreferences prefs, Team team, Pokemon pokemon)
	{
		SavedTeam savedTeam = new SavedTeam();
		savedTeam.id = getCurrentTeamId(prefs);
		for(TeamMember m : team.members)
		{
			if(m.pokemon.getValue() != null) // FIXME
			{
				savedTeam.memberIds.add(m.pokemon.getValue().getId());
			}
		}
		savedTeam.memberIds.add(pokemon.getId());
		savedTeam.id = getCurrentTeamId(prefs);
		dao.deleteSavedTeam(savedTeam);
		dao.insertSavedTeam(savedTeam);
	}

	public static void removePokemoFromCurrentTeam(SavedTeamDao dao, SharedPreferences prefs, Team team, Pokemon pokemon)
	{
		SavedTeam savedTeam = new SavedTeam();
		savedTeam.id = getCurrentTeamId(prefs);
		for(TeamMember m : team.members)
		{
			if(m.pokemon.getValue() != null)
			{
				if(m.pokemon.getValue().getId() != pokemon.getId())
				{
					savedTeam.memberIds.add(m.pokemon.getValue().getId());
				}
			}
		}
		savedTeam.id = getCurrentTeamId(prefs);
		// it's probably a little roundabout to delete the hole team and re-add it, but hey
		dao.deleteSavedTeam(savedTeam);
		dao.insertSavedTeam(savedTeam);
	}
}
