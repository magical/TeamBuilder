package rec.games.pokemon.teambuilder.db;

import android.app.Application;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import java.util.List;

public class SavedTeamRepository
{
	private SavedTeamDao mDao;

	public SavedTeamRepository(Application app)
	{
		AppDatabase db = AppDatabase.getDatabase(app);
		mDao = db.savedTeamDao();
	}

	public void addTeamMember(SavedTeam team, int pokemonId)
	{
		SavedTeamMemberEntity tm = new SavedTeamMemberEntity();
		tm.teamId = team.id;
		tm.pokemonId = pokemonId;
		new InsertTeamMemberAsyncTask(mDao).execute(tm);
	}

	public void removeTeamMember(SavedTeam team, int pokemonId)
	{
		SavedTeamMemberEntity tm = new SavedTeamMemberEntity();
		tm.teamId = team.id;
		tm.pokemonId = pokemonId;
		new DeleteTeamMemberAsyncTask(mDao).execute(tm);
	}

	public void createSavedTeam(SavedTeam team) { new CreateSavedTeamAsyncTask(mDao).execute(team); }
	public void deleteSavedTeam(SavedTeam team) { new DeleteSavedTeamAsyncTask(mDao).execute(team); }

	public LiveData<SavedTeam> getTeamById(int id) { return mDao.getTeamById(id); }
	public LiveData<List<SavedTeam>> getAllTeams() { return mDao.getAllTeams(); }

	public LiveData<Boolean> isPokemonInTeam(int teamId, int pokemonId) { return mDao.isPokemonInTeam(teamId, pokemonId); }
	public boolean isPokemonInTeamSync(int teamId, int pokemonId) { return mDao.isPokemonInTeamSync(teamId, pokemonId); }

	private static class CreateSavedTeamAsyncTask extends AsyncTask<SavedTeam, Void, Void>
	{
		private SavedTeamDao mDao;

		CreateSavedTeamAsyncTask(SavedTeamDao dao)
		{
			mDao = dao;
		}

		@Override
		protected Void doInBackground(SavedTeam... savedTeams)
		{
			mDao.createSavedTeam(savedTeams[0]);
			return null;
		}
	}

	private static class DeleteSavedTeamAsyncTask extends AsyncTask<SavedTeam, Void, Void>
	{
		private SavedTeamDao mDao;

		DeleteSavedTeamAsyncTask(SavedTeamDao dao)
		{
			mDao = dao;
		}

		@Override
		protected Void doInBackground(SavedTeam... savedTeams)
		{
			mDao.deleteSavedTeam(savedTeams[0]);
			return null;
		}
	}


	private static class InsertTeamMemberAsyncTask extends AsyncTask<SavedTeamMemberEntity, Void, Void>
	{
		private SavedTeamDao mDao;

		InsertTeamMemberAsyncTask(SavedTeamDao dao)
		{
			mDao = dao;
		}

		@Override
		protected Void doInBackground(SavedTeamMemberEntity... savedTeamMemberEntities)
		{
			SavedTeamMemberEntity tm = savedTeamMemberEntities[0];
			mDao.insertSavedTeamMemberEntity(tm);
			return null;
		}
	}

	private static class DeleteTeamMemberAsyncTask extends AsyncTask<SavedTeamMemberEntity, Void, Void>
	{
		private SavedTeamDao mDao;

		DeleteTeamMemberAsyncTask(SavedTeamDao dao)
		{
			mDao = dao;
		}

		@Override
		protected Void doInBackground(SavedTeamMemberEntity... savedTeamMemberEntities)
		{
			SavedTeamMemberEntity tm = savedTeamMemberEntities[0];
			mDao.removePokemonFromTeamById(tm.teamId, tm.pokemonId);
			return null;
		}
	}
}
