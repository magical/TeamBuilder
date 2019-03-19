package rec.games.pokemon.teambuilder.model;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class LiveDataListIterator<E> implements Iterator<E>
{
	private Iterator<LiveData<E>> iterator;

	LiveDataListIterator(Iterator<LiveData<E>> iterator)
	{
		this.iterator = iterator;
	}

	@Override
	public boolean hasNext()
	{
		return iterator.hasNext();
	}

	@Override
	public E next()
	{
		if(!hasNext())
			throw new NoSuchElementException();

		return iterator.next().getValue();
	}
}

class LiveDataMapValue<E>
{
	int index;
	HashSet<Observer<E>> itemObservers;

	LiveDataMapValue(int index)
	{
		this.index = index;
		itemObservers = new HashSet<>();
	}
}

public class LiveDataList<E> implements Iterable<E>
{
	private List<LiveData<E>> list;
	private HashMap<LiveData<E>, LiveDataMapValue<E>> liveDataMap = new HashMap<>();
	private HashSet<CollectionObserver<E>> collectionObserverSet = new HashSet<>();
	private MediatorLiveData<LiveData<E>> mediator = new MediatorLiveData<>();

	public LiveDataList()
	{
		list = new ArrayList<>();

		//mediator cannot observe its sources if it doesn't have its own active observer
		mediator.observeForever(new Observer<LiveData<E>>()
		{
			@Override
			public void onChanged(@Nullable LiveData<E> LiveData)
			{
			}
		});
	}

	public LiveDataList(Collection<LiveData<E>> collection)
	{
		list = new ArrayList<>(collection);
		for(int i = 0; i < list.size(); i++)
		{
			final LiveData<E> item = list.get(i);

			liveDataMap.put(item, new LiveDataMapValue<E>(i));
			mediator.addSource(item, new Observer<E>()
			{
				@Override
				public void onChanged(@Nullable E e)
				{
					LiveDataMapValue<E> liveDataMapValue = liveDataMap.get(item);
					if(liveDataMapValue == null)
						return;

					for(Observer<E> observer : liveDataMapValue.itemObservers)
						observer.onChanged(e);

					for(CollectionObserver<E> collectionObserver : collectionObserverSet)
						collectionObserver.onItemChanged(e, liveDataMapValue.index);
				}
			});
		}

		//mediator cannot observe its sources if it doesn't have its own active observer
		mediator.observeForever(new Observer<LiveData<E>>()
		{
			@Override
			public void onChanged(@Nullable LiveData<E> LiveData)
			{
			}
		});
	}

	public E getValue(int index)
	{
		return list.get(index).getValue();
	}

	public void add(final LiveData<E> item)
	{
		liveDataMap.put(item, new LiveDataMapValue<E>(list.size()));
		mediator.addSource(item, new Observer<E>()
		{
			@Override
			public void onChanged(@Nullable E e)
			{
				LiveDataMapValue<E> liveDataMapValue = liveDataMap.get(item);
				if(liveDataMapValue == null)
					return;

				for(Observer<E> observer : liveDataMapValue.itemObservers)
					observer.onChanged(e);

				for(CollectionObserver<E> collectionObserver : collectionObserverSet)
					collectionObserver.onItemChanged(e, liveDataMapValue.index);
			}
		});

		list.add(item);
	}

	public int size()
	{
		return list.size();
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return new LiveDataListIterator<>(list.iterator());
	}

	public void sort(final Comparator<? super E> comparator)
	{
		Collections.sort(list, new Comparator<LiveData<E>>()
		{
			@Override
			public int compare(LiveData<E> o1, LiveData<E> o2)
			{
				return comparator.compare(o1.getValue(), o2.getValue());
			}
		});

		//cleanup LiveDataMap index pointers
		for(int i = 0; i < list.size(); i++)
		{
			LiveDataMapValue<E> liveDataMapValue = liveDataMap.get(list.get(i));
			if(liveDataMapValue == null)
				continue;

			liveDataMapValue.index = i;
		}
	}

	//adds an observer to an item, which this class manages as a itemObserver
	public void observeItem(int index, Observer<E> observer)
	{
		if(index >= list.size())
			return;

		LiveData<E> item = list.get(index);
		LiveDataMapValue<E> liveDataMapValue = liveDataMap.get(item);
		if(liveDataMapValue == null)
			return;

		liveDataMapValue.itemObservers.add(observer);
	}

	//remove the itemObserver and the observer associated with it
	public void removeItemObserver(int index, Observer<E> observer)
	{
		if(index >= list.size())
			return;

		LiveData<E> item = list.get(index);
		LiveDataMapValue<E> liveDataMapValue = liveDataMap.get(item);
		if(liveDataMapValue == null)
			return;

		liveDataMapValue.itemObservers.remove(observer);

	}

	//adds an observer to all items in the collection, which this class manages as a collectionObserver
	public void observeCollection(CollectionObserver<E> collectionObserver)
	{
		collectionObserverSet.add(collectionObserver);
	}

	//remove the collectionObserver and the observer associated with it
	public void removeCollectionObserver(CollectionObserver<E> collectionObserver)
	{
		collectionObserverSet.remove(collectionObserver);
	}

	//if the search criteria returns true, then that given item is added to the sublist
	public LiveDataList<E> searchSubList(SearchCriteria<E> searchCriteria)
	{
		LiveDataList<E> subList = new LiveDataList<>();

		for(LiveData<E> item: list)
			if(searchCriteria.match(item.getValue()))
				subList.add(item);

		return subList;
	}
}
