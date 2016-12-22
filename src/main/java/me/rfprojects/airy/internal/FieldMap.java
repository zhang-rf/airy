package me.rfprojects.airy.internal;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class FieldMap {

    private Map<Short, Field> hashFieldMap = new HashMap<>();
    private String suffix;

    FieldMap() {
        suffix = "";
    }

    FieldMap(int conflicts) {
        char[] chars = new char[conflicts];
        Arrays.fill(chars, ' ');
        suffix = new String(chars);
    }

    boolean put(Field field) {
        short hashcode = (short) (field.getName() + suffix).hashCode();
        Field conflictField = hashFieldMap.get(hashcode);
        if (conflictField != null)
            return conflictField.getName().equals(field.getName());
        hashFieldMap.put(hashcode, field);
        return true;
    }

    Field pop(short hash) {
        return hashFieldMap.remove(hash);
    }

    boolean isEmpty() {
        return hashFieldMap.isEmpty();
    }

    byte getConflicts() {
        return (byte) suffix.length();
    }

    void reset() {
        suffix += ' ';
        hashFieldMap.clear();
    }

    Collection<Field> fields() {
        return hashFieldMap.values();
    }
}
