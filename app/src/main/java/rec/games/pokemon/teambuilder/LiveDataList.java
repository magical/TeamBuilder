package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

interface ItemObserver<T>
{
	void onItemChanged(@Nullable T t, int index);
}

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
	ArrayList<Observer<E>> itemObservers;

	LiveDataMapValue(int index)
	{
		this.index = index;
		itemObservers = new ArrayList<>();
	}
}

class LiveDataList<E> implements Iterable<E>
{
	private List<LiveData<E>> list;
	private HashMap<LiveData<E>, LiveDataMapValue<E>> liveDataMap = new HashMap<>();
	private HashSet<ItemObserver<E>> collectionObserverSet = new HashSet<>();
	private MediatorLiveData<LiveData<E>> mediator = new MediatorLiveData<>();

	LiveDataList()
	{
		list = new ArrayList<>();
	}

	LiveDataList(Collection<LiveData<E>> collection)
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

					for(Observer<E> observer: liveDataMapValue.itemObservers)
						observer.onChanged(e);

					for(ItemObserver<E> itemObserver: collectionObserverSet)
						itemObserver.onItemChanged(e, liveDataMapValue.index);
				}
			});
		}
	}

	E getValue(int index)
	{
		return list.get(index).getValue();
	}

	void add(final LiveData<E> item)
	{
		liveDataMap.put(item, new LiveDataMapValue<E>(list.size()));
		mediator.addSource(item, new Observer<E>()
		{
			@Override
			public void onChanged(@Nullable E e)
			{
				LiveDataMapValue<E> liveDataMapValue = liveDataMap.get(item);

				for(Observer<E> observer: liveDataMapValue.itemObservers)
					observer.onChanged(e);

				for(ItemObserver<E> itemObserver: collectionObserverSet)
					itemObserver.onItemChanged(e, liveDataMapValue.index);
			}
		});

		list.add(item);
	}

	int size()
	{
		return list.size();
	}

	@NonNull
	@Override
	public Iterator<E> iterator()
	{
		return new LiveDataListIterator<>(list.iterator());
	}

	//adds an observer to an item, which this class manages as a itemObserver
	void observeItem(int index, LifecycleOwner owner, Observer<E> observer)
	{
		if(index >= list.size())
			return;

		LiveData<E> item = list.get(index);

		liveDataMap.get(item).itemObservers.add(observer);
		item.observe(owner, observer);
	}

	//remove the itemObserver and the observer associated with it
	void removeItemObserver(int index, Observer<E> observer)
	{
		if(index >= list.size())
			return;

		LiveData<E> item = list.get(index);

		liveDataMap.get(item).itemObservers.remove(observer);
		item.removeObserver(observer);

	}

	//adds an observer to all items in the collection, which this class manages as a collectionObserver
	void observeCollection(LifecycleOwner owner, final ItemObserver<E> itemObserver)
	{
		collectionObserverSet.add(itemObserver);
	}

	//remove the collectionObserver and the observer associated with it
	void removeCollectionObserver(ItemObserver<E> itemObserver)
	{
		collectionObserverSet.remove(itemObserver);
	}

	//on garbage collection, we guarantee that we will remove all of the observers attached through our LiveDataList methods
	//that way if this object dies and the underlying LiveData objects live on
	//...then we won't leak observers
	@Override
	protected void finalize() throws Throwable
	{
		//cleanup all of the itemObservers for each item
		for(LiveData<E> item: list)
			for(Observer<E> observer: liveDataMap.get(item).itemObservers)
				item.removeObserver(observer);

		super.finalize();
	}
}
