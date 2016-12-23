package me.rfprojects.airy.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by LocalUser on 2016/12/23.
 */
public class HashListTest {

    @Test
    public void add() throws Exception {
        List<String> list = new HashList<>();
        list.add("123");
        list.add("456");
        list.add("789");
        assertEquals("123", list.get(0));
        assertEquals("456", list.get(1));
        assertEquals("789", list.get(2));
    }

    @Test
    public void add0() throws Exception {
        List<String> list = new HashList<>();
        list.add(0, "123");
        list.add(0, "456");
        list.add(0, "789");
        assertEquals("123", list.get(2));
        assertEquals("456", list.get(1));
        assertEquals("789", list.get(0));
    }

    @Test
    public void addAll() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        assertEquals("123", list.get(0));
        assertEquals("456", list.get(1));
        assertEquals("789", list.get(2));
    }

    @Test
    public void addAll0() throws Exception {
        List<String> list = new HashList<>();
        list.add("123");
        list.addAll(0, Arrays.asList("456", "789"));
        assertEquals("123", list.get(2));
        assertEquals("456", list.get(0));
        assertEquals("789", list.get(1));
    }

    @Test
    public void clear() throws Exception {
        List<String> list = new HashList<>();
        list.add("123");
        list.add("456");
        list.add("789");
        list.clear();
        assertTrue(list.isEmpty());
    }

    @Test
    public void contains() throws Exception {
        List<String> list = new HashList<>();
        list.add("123");
        assertTrue(list.contains("123"));
    }

    @Test
    public void containsAll() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        assertTrue(list.containsAll(Arrays.asList("123", "456", "789")));
    }

    @Test
    public void indexOf() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        assertEquals(1, list.indexOf("456"));
    }

    @Test
    public void lastIndexOf() throws Exception {
        assertFalse(new HashList<>().addAll(Arrays.asList("123", "123")));
    }

    @Test
    public void remove() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        list.remove("123");
        assertEquals(0, list.indexOf("456"));
        assertEquals(1, list.indexOf("789"));
    }

    @Test
    public void remove0() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        list.remove(0);
        assertEquals(0, list.indexOf("456"));
        assertEquals(1, list.indexOf("789"));
    }

    @Test
    public void removeAll() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        list.removeAll(Arrays.asList("123", "456"));
        assertEquals(0, list.indexOf("789"));
    }

    @Test
    public void retainAll() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        list.retainAll(Arrays.asList("456", "789"));
        assertEquals(0, list.indexOf("456"));
    }

    @Test
    public void set() throws Exception {
        List<String> list = new HashList<>();
        list.addAll(Arrays.asList("123", "456", "789"));
        list.set(1, "654");
        assertEquals("654", list.get(1));
    }
}