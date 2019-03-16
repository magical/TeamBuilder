package rec.games.pokemon.teambuilder.db;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;

import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.model.TeamMember;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

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
		return Transformations.map(liveSavedTeam, new Function<SavedTeam, Team>()
			{
				@Override
				public Team apply(SavedTeam savedTeam)
				{
					Team team = new Team();
					if (savedTeam != null && savedTeam.memberIds != null)
					{
						for(int pokemonId : savedTeam.memberIds)
						{
							TeamMember m = new TeamMember();
							m.pokemon = pokeapi.getPokemonById(pokemonId);
							team.members.add(m);
						}
					}
					return team;
				}
			}
		);
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

	public static void addPokemonToCurrentTeam(SavedTeamRepository repo, SharedPreferences prefs, Pokemon pokemon)
	{
		SavedTeam savedTeam = new SavedTeam();
		savedTeam.id = getCurrentTeamId(prefs);
		//repo.createSavedTeam(savedTeam); // in case it hasn't been created yet
		repo.addTeamMember(savedTeam, pokemon.getId());
	}

	public static void removePokemonFromCurrentTeam(SavedTeamRepository repo, SharedPreferences prefs, Pokemon pokemon)
	{
		SavedTeam savedTeam = new SavedTeam();
		savedTeam.id = getCurrentTeamId(prefs);
		//repo.createSavedTeam(savedTeam); // in case it hasn't been created yet
		repo.removeTeamMember(savedTeam, pokemon.getId());
	}
}
