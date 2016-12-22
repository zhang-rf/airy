package me.rfprojects.airy.util;

import java.io.Serializable;
import java.util.*;

public class HashList<E> extends ArrayList<E>
        implements List<E>, RandomAccess, Cloneable, Serializable {

    private static final long serialVersionUID = 8683452581122892189L;
    private Map<E, Integer> indexMap;

    public HashList() {
        indexMap = new HashMap<>();
    }

    public HashList(int initialCapacity) {
        super(initialCapacity);
        indexMap = new HashMap<>(initialCapacity);
    }

    public HashList(int initialCapacity, float loadFactor) {
        super(initialCapacity);
        indexMap = new HashMap<>(initialCapacity, loadFactor);
    }

    public HashList(Collection<? extends E> c) {
        this();
        addAll(c);
    }

    @Override
    public boolean add(E e) {
        if (contains(e))
            return false;
        indexMap.put(e, this.size());
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        updateIndex(index);
    }

    private void updateIndex(int from) {
        for (int i = from, size = size(); i < size; i++)
            indexMap.put(get(i), i);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean addAll = true;
        for (E e : c)
            addAll = add(e) && addAll;
        return addAll;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        boolean addAll = true;
        for (E e : c) {
            if (addAll)
                addAll = !contains(e);
            add(index++, e);
        }
        return addAll;
    }

    @Override
    public void clear() {
        super.clear();
        indexMap.clear();
    }

    @Override
    public boolean contains(Object o) {
        return indexMap.containsKey(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c)
            if (!contains(o))
                return false;
        return true;
    }

    @Override
    public int indexOf(Object o) {
        Integer index = indexMap.get(o);
        return index != null ? index : -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public E remove(int index) {
        E oldValue = super.remove(index);
        indexMap.remove(oldValue);
        updateIndex(index);
        return oldValue;
    }

    @Override
    public boolean remove(Object o) {
        int index = indexOf(o);
        return index >= 0 && remove(index) != null;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean removeAll = super.removeAll(c);
        if (removeAll)
            reindex();
        return removeAll;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean retainAll = false;
        Iterator<E> iterator = iterator();
        while (iterator.hasNext()) {
            E e = iterator.next();
            if (!c.contains(e)) {
                iterator.remove();
                retainAll = true;
            }
        }
        if (retainAll)
            reindex();
        return retainAll;
    }

    private void reindex() {
        indexMap.clear();
        for (int i = 0, size = size(); i < size; i++)
            indexMap.put(get(i), i);
    }

    @Override
    public E set(int index, E element) {
        if (contains(element))
            return null;
        E oldValue = super.set(index, element);
        indexMap.remove(oldValue);
        indexMap.put(element, index);
        return oldValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashList<E> newList = (HashList<E>) super.clone();
        newList.indexMap = (Map<E, Integer>) ((HashMap<E, Integer>) indexMap).clone();
        return newList;
    }
}
