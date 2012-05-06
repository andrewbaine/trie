package com.lawdawg.trie;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

public class Trie implements SortedMap<CharSequence, Integer> {

	private final int root;
	private final int min;
	private final int max;
	
	private final boolean inRange(final int n) {
		return (min <= n && n < max);
	}
	
	private final int find(final CharSequence chars) {
		final int length = chars.length();
		int root = 0;
		for (int i = 0; i < length && inRange(root); i++) {
			final int middleChild = this.middleChild(root);
			final char c = chars.charAt(i);
			root = findLetterAmongSiblings(middleChild, c);
		}
		return root;
	}
	
	private final int findLetterAmongSiblings(int index, final char c) {
		while (true) {
			if (index == -1) {
				return -1;
			}
			final char x = charAt(index);
			if (x == c) {
				return index;
			}
			index = (x < c) ? leftSibling(index) : rightSibling(index);
		}
	}
	
	// a node in the trie takes up 6 spaces
	//    (0) a character and a boolean indicating containsKey
	//    (1) value - the integer payload stored at this node (ignored)
	//    (2) size - the size of the sub-trie rooted here // do we need this?
	//    (3) middleChild
	//    (4) leftSibling
	//    (5) rightSibling
	
	private char charAt(final int node) {
		return  (char) (data[node] & 0xffff);	
	}
	
	private boolean nodeContainsKey(final int node) {
		return (data[node] & (~(0xffff))) != 0;
	}
	
	private int nodeValue(final int node) {
		return data[node + 1];
	}

	private int nodeSize(final int node) {
		return data[node + 2];
	}
	
	private final int middleChild(final int node) {
		return data[node + 3];		
	}
	
	private final int leftSibling(final int node) {
		return data[node + 4];
	}
	
	private final int rightSibling(final int node) {
		return data[node + 5];
	}
	
	private final int[] data;
	
	public Trie(final int[] data) {
		this(data, 0, 0, data.length);
	}
	
	private Trie(final int[] data, final int root, final int min, final int max) {
		this.data = data;
		this.root = root;
		this.min = min;
		this.max = max;
	}
	
	@Override
	public boolean isEmpty() {
		return this.size() == 0;
	}

	@Override
	public Set<CharSequence> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int size() {
		return this.nodeSize(this.root);
	}

	@Override
	public Collection<Integer> values() {
		throw new UnsupportedOperationException();
	}

	public Comparator<? super CharSequence> comparator() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private final int youngestChild(final int index) {
		int x = middleChild(index);
		while (leftSibling(x) != -1) {
			x = leftSibling(x);
		}
		return x;
	}
	
	public CharSequence firstKey() {
		throw new UnsupportedOperationException();
	}

	public SortedMap<CharSequence, Integer> headMap(CharSequence toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public CharSequence lastKey() {
		// TODO Auto-generated method stub
		return null;
	}

	public SortedMap<CharSequence, Integer> subMap(CharSequence fromKey,
			CharSequence toKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public SortedMap<CharSequence, Integer> tailMap(CharSequence fromKey) {
		// TODO Auto-generated method stub
		return null;
	}
	

	public void putAll(Map<? extends CharSequence, ? extends Integer> m) {
		throw new UnsupportedOperationException();
	}

	public Integer remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	public boolean containsKey(Object key) {
		if (key instanceof CharSequence) {
			int index = this.find((CharSequence) key);
			return this.nodeContainsKey(index);
		}
		return false;
	}

	public boolean containsValue(Object value) {
		int count = 0;
		final int size = this.size();
		for (int i = root; count < size; i++) {
			
		}
		int stop = this.root + this.size();
		for (int i = 0; i < this.size(); i++) {
			
		}
		// TODO Auto-generated method stub
		return false;
	}

	public Integer get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	public Integer put(CharSequence key, Integer value) {
		// TODO Auto-generated method stub
		return null;
	}

	public Set<java.util.Map.Entry<CharSequence, Integer>> entrySet() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int dataLength() {
		return this.data.length;
	}
	
	public int dataAt(final int i) {
		return this.data[i];
	}
}
