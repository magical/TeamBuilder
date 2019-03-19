package rec.games.pokemon.teambuilder.db;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.content.SharedPreferences;

import rec.games.pokemon.teambuilder.model.Pokemon;
import rec.games.pokemon.teambuilder.model.Team;
import rec.games.pokemon.teambuilder.model.TeamMember;
import rec.games.pokemon.teambuilder.viewmodel.PokeAPIViewModel;

/**
 * TeamUtils provides some helper methods for dealing with saved teams.
 * TODO: convert to a ViewModel?
 */
public class TeamUtils
{
	public static boolean isPokemonInCurrentTeam(SavedTeamRepository repo, int teamId, int pokemonId)
	{
		return repo.isPokemonInTeamSync(teamId, pokemonId);
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
							m.pokemon = pokeapi.getLivePokemon(pokemonId);
							team.members.add(m);
						}
					}
					return team;
				}
			}
		);
	}

	public static LiveData<Team> getCurrentTeam(PokeAPIViewModel viewModel, SavedTeamDao dao, int teamId)
	{
		return getTeam(viewModel, dao, teamId);
	}

	public static void addPokemonToCurrentTeam(SavedTeamRepository repo, int teamId, Pokemon pokemon)
	{
		SavedTeam savedTeam = new SavedTeam();
		savedTeam.id = teamId;
		repo.createSavedTeam(savedTeam); // in case it hasn't been created yet
		repo.addTeamMember(savedTeam, pokemon.getId());
	}

	public static void removePokemonFromCurrentTeam(SavedTeamRepository repo, int teamId, Pokemon pokemon)
	{
		SavedTeam savedTeam = new SavedTeam();
		savedTeam.id = teamId;
		repo.removeTeamMember(savedTeam, pokemon.getId());
	}
}
