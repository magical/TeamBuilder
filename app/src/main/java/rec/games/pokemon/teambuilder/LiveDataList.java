package rec.games.pokemon.teambuilder;

import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;

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

class LiveDataList<E> implements Iterable<E>
{
	private List<LiveData<E>> list;
	private HashSet<Pair<LiveData<E>, Observer<E>>> itemObserverMap = new HashSet<>();
	private HashMap<ItemObserver<E>, ArrayList<Observer<E>>> collectionObserverMap = new HashMap<>();

	LiveDataList()
	{
		list = new ArrayList<>();
	}

	LiveDataList(Collection<LiveData<E>> collection)
	{
		list = new ArrayList<>(collection);
	}

	E getValue(int index)
	{
		return list.get(index).getValue();
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
		itemObserverMap.add(Pair.create(item, observer));
		item.observe(owner, observer);
	}

	//remove the itemObserver and the observer associated with it
	void removeItemObserver(int index, Observer<E> observer)
	{
		if(index >= list.size())
			return;

		LiveData<E> item = list.get(index);
		Pair<LiveData<E>, Observer<E>> key = Pair.create(item, observer);
		if(!itemObserverMap.contains(key))
			return;

		itemObserverMap.remove(key);
		item.removeObserver(observer);

	}

	//adds an observer to all items in the collection, which this class manages as a collectionObserver
	void observeCollection(LifecycleOwner owner, final ItemObserver<E> itemObserver)
	{
		ArrayList<Observer<E>> observers = new ArrayList<>(list.size());
		for(int i = 0; i < list.size(); i++)
		{
			LiveData<E> item = list.get(i);
			final int readOnlyI = i;

			Observer<E> observer = new Observer<E>()
			{
				@Override
				public void onChanged(@Nullable E e)
				{
					itemObserver.onItemChanged(e, readOnlyI);
				}
			};
			item.observe(owner, observer);
			observers.add(observer);
		}

		collectionObserverMap.put(itemObserver, observers);
	}

	//remove the collectionObserver and the observer associated with it
	void removeCollectionObserver(ItemObserver<E> itemObserver)
	{
		if(!collectionObserverMap.containsKey(itemObserver))
			return;

		ArrayList<Observer<E>> observers = collectionObserverMap.remove(itemObserver);

		Iterator<LiveData<E>> listIterator = list.iterator();
		Iterator<Observer<E>> observerIterator = observers.iterator();
		while(listIterator.hasNext() && observerIterator.hasNext())
		{
			LiveData<E> item = listIterator.next();
			Observer<E> observer = observerIterator.next();

			item.removeObserver(observer);
		}
	}

	//on garbage collection, we guarantee that we will remove all of the observers attached through our LiveDataList methods
	//that way if this object dies and the underlying LiveData objects live on
	//...then we won't leak observers
	@Override
	protected void finalize() throws Throwable
	{
		//cleanup all of the itemObservers
		for(Pair<LiveData<E>, Observer<E>> key: itemObserverMap)
			key.first.removeObserver(key.second);

		//cleanup all of the collectionObservers
		for(ItemObserver<E> itemObserver: collectionObserverMap.keySet())
			removeCollectionObserver(itemObserver);

		super.finalize();
	}
}
