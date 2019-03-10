package rec.games.pokemon.teambuilder;

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

	public void insertSavedTeam(SavedTeam team)
	{
		new InsertAsyncTask(mDao).execute(team);
	}

	public void deleteSavedTeam(SavedTeam team)
	{
		new DeleteAsyncTask(mDao).execute(team);
	}

	public LiveData<SavedTeam> getTeamById(int id)
	{
		return mDao.getTeamById(id);
	}

	public LiveData<List<SavedTeam>> getAllTeams()
	{
		return mDao.getAllTeams();
	}

	private static class InsertAsyncTask extends AsyncTask<SavedTeam, Void, Void>
	{
		private SavedTeamDao mDao;

		InsertAsyncTask(SavedTeamDao dao)
		{
			mDao = dao;
		}

		@Override
		protected Void doInBackground(SavedTeam... savedTeams)
		{
			mDao.insertSavedTeam(savedTeams[0]);
			return null;
		}
	}


	private static class DeleteAsyncTask extends AsyncTask<SavedTeam, Void, Void>
	{
		private SavedTeamDao mDao;

		DeleteAsyncTask(SavedTeamDao dao)
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
}
