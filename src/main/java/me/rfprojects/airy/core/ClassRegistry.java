package me.rfprojects.airy.core;

import me.rfprojects.airy.util.HashList;

import java.util.*;

public class ClassRegistry {

    private List<Class<?>> classList = new HashList<>();
    private int primitives;

    public ClassRegistry() {
        classList.add(boolean.class);
        classList.add(char.class);
        classList.add(byte.class);
        classList.add(short.class);
        classList.add(int.class);
        classList.add(long.class);
        classList.add(float.class);
        classList.add(double.class);
        classList.add(Boolean.class);
        classList.add(Character.class);
        classList.add(Byte.class);
        classList.add(Short.class);
        classList.add(Integer.class);
        classList.add(Long.class);
        classList.add(Float.class);
        classList.add(Double.class);
        primitives = classList.size();
        classList.add(String.class);
        classList.add(Enum.class);

        classList.add(ArrayList.class);
        classList.add(LinkedList.class);
        classList.add(PriorityQueue.class);
        classList.add(HashMap.class);
        classList.add(EnumMap.class);
        classList.add(IdentityHashMap.class);
        classList.add(LinkedHashMap.class);
        classList.add(TreeMap.class);
        classList.add(WeakHashMap.class);
        classList.add(HashSet.class);
        classList.add(BitSet.class);
        classList.add(LinkedHashSet.class);
        classList.add(TreeSet.class);
    }

    public int register(Class<?> clazz) {
        return classList.add(clazz) ? classList.size() : idOf(clazz);
    }

    public Class<?> getClass(int id) {
        return id <= classList.size() ? classList.get(id - 1) : null;
    }

    public int idOf(Class<?> clazz) {
        return classList.indexOf(clazz) + 1;
    }

    public boolean isPrimitive(Class<?> clazz) {
        int id = idOf(clazz);
        return id > 0 && id <= primitives;
    }
}
