package me.rfprojects.airy.core;

import me.rfprojects.airy.util.HashList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ClassRegistry {

    private static final ConcurrentMap<String, Class<?>> classNameMap = new ConcurrentHashMap<>();
    private List<Class<?>> classList = new HashList<>();

    public ClassRegistry() {
        classList.add(Object.class);

        classList.add(Boolean.class);
        classList.add(Character.class);
        classList.add(Byte.class);
        classList.add(Short.class);
        classList.add(Integer.class);
        classList.add(Long.class);
        classList.add(Float.class);
        classList.add(Double.class);
        classList.add(String.class);
        classList.add(Enum.class);

        classList.add(boolean[].class);
        classList.add(char[].class);
        classList.add(byte[].class);
        classList.add(short[].class);
        classList.add(int[].class);
        classList.add(long[].class);
        classList.add(float[].class);
        classList.add(double[].class);
        classList.add(String[].class);
        classList.add(Enum[].class);

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

    public int register(Class<?> type) {
        return classList.add(Objects.requireNonNull(type)) ? classList.size() : idOf(type);
    }

    public int idOf(Class<?> type) {
        return classList.indexOf(type) + 1;
    }

    public Class<?> findClass(int id) {
        return id > 0 && id <= classList.size() ? classList.get(id - 1) : null;
    }

    public void writeClass(NioBuffer buffer, Class<?> type) {
        if (type != null) {
            int classId = idOf(type);
            buffer.putUnsignedVarint(classId);
            if (classId == 0)
                buffer.putString(type.getName().replaceAll("^java\\.lang\\.", "#"));
        } else {
            buffer.asByteBuffer().put((byte) 0);
            buffer.putString("");
        }
    }

    public Class<?> readClass(NioBuffer buffer, Class<?> defaultClass) {
        try {
            Class<?> type = findClass((int) buffer.getUnsignedVarint());
            if (type == null) {
                type = defaultClass;
                String className = buffer.getString().replaceAll("^#", "java.lang.");
                if (!"".equals(className)) {
                    type = classNameMap.get(className);
                    if (type == null) {
                        type = Class.forName(className);
                        classNameMap.put(className, type);
                    }
                }
            }
            return type;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
