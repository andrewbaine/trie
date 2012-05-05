package com.lawdawg.trie;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Stack;

public class TrieBuilder {

	private final List<Integer> data = new ArrayList<Integer>();
	private final Stack<Integer> path = new Stack<Integer>();
	private final Stack<Character> prefix = new Stack<Character>();
	private final List<List<Integer>> lists = new ArrayList<List<Integer>>();
	
	private final Stack<List<Integer>> childrenStack = new Stack<List<Integer>>();
	
	public TrieBuilder() {
		for (int i = 0; i < 65; i++) {
			lists.add(new ArrayList<Integer>());
		}
		childrenStack.add(lists.get(0));
	}
	
	private class E implements Entry<CharSequence, Integer> {

		public CharSequence key;
		public Integer value;
		
		@Override
		public CharSequence getKey() {
			return key;
		}

		@Override
		public Integer getValue() {
			return value;
		}

		@Override
		public Integer setValue(Integer arg0) {
			throw new UnsupportedOperationException();
		}
	}
	
	public Trie buildTrie(final InputStream tsv) {
		return buildTrie(new Scanner(tsv));
	}
	
	public Trie buildTrie(final Scanner scanner) {
		return buildTrie(new Iterable<Entry<CharSequence, Integer>>() {
			
			private final E entry = new E();
			
			@Override
			public Iterator<Entry<CharSequence, Integer>> iterator() {
				
				return new Iterator<Entry<CharSequence,Integer>>() {

					@Override
					public boolean hasNext() {
						return scanner.hasNextLine();
					}

					@Override
					public Entry<CharSequence, Integer> next() {
						final String line = scanner.nextLine();
						int x = line.lastIndexOf("\t");
						entry.key = line.subSequence(0, x);
						entry.value = Integer.parseInt(line.substring(x + 1));
						return entry;
					}

					@Override
					public void remove() {
						throw new UnsupportedOperationException();
					}
				};
			}
			
		});
	}
	
	private void pushNode(final char c, final Integer value) {

		this.prefix.push(c);
		
		final int node = this.data.size();
		this.path.push(node);
		this.childrenStack.peek().add(node); // this list includes all of the siblings of this node
		
		int x = childrenStack.size();
		final List<Integer> list = lists.get(x);
		list.clear();
		this.childrenStack.push(list); // add a new empty list

		final int k = (value == null ? 0 : ~(0xffff)) | c;
		final int v = (value == null) ? -1 : value;
		final int size = (value == null) ? 0 : 1;
		final int middle = -1;
		final int left = -1;
		final int right = -1;
		
		// we add 6 items
		this.data.add(k);
		this.data.add(v);
		this.data.add(size); // unneccessary?
		this.data.add(middle);
		this.data.add(left);
		this.data.add(right);
	}
	
	private void popNode() {
		this.prefix.pop();
		int node = this.path.pop();
		final List<Integer> children = this.childrenStack.pop();
		this.addChildren(node, children);
	}
		
	public Trie buildTrie(final Iterable<Entry<CharSequence, Integer>> entries) {
		
		pushNode('\0', null);
		
		for (final Entry<CharSequence, Integer> e : entries) {
			final CharSequence key = e.getKey();
			final int keyLength = key.length();

			boolean firstPath = true;
			int shared = 0;
			for (Character c : this.prefix) {
				if (firstPath) {
					firstPath = false; // there's a null root node
				} else {
					if (shared < keyLength && key.charAt(shared) == c) {
						++shared;
					} else {
						break;
					}
				}
			}
			
			// this key shares <s> characters with our current path
			// rewind to the node in the 
			while (prefix.size() - 1 > shared) {
				popNode();
			}
			
			for (int i = shared; i < keyLength; i++) {
				final char c = key.charAt(i);
				final Integer v = (i == (keyLength - 1)) ? e.getValue() : null;
				pushNode(c, v);
			}
		}
		
		while (!path.empty()) {
			popNode();
		}

		// set the size and middle child of the root
		final int[] d = new int[data.size()];
		int i = 0;
		for (Integer x : data) {
			d[i++] = x;
		}
		final Trie trie = new Trie(d);
		return trie;
	}
		
	private void addChildren(final int root, final List<Integer> children) {
		int size = containsValue(root) ? 1 : 0;
		if (children != null && children.size() > 0) {
			final int middle = children.size() / 2;
			final int middleChild = children.get(middle);
			setMiddleChild(root, middleChild);			
			linkChildren(children, 0, middle, children.size());
			
			for (final Integer i : children) {
				size += getSize(i);
			}
		}
		setSize(root, size);
	}

	private void linkChildren(final List<Integer> children, final int start, final int middle, final int end) {
		if (start < end) {
			if (start < middle) {
				final int nextStart = start;
				final int nextEnd = middle - 1;
				final int nextMiddle = (nextStart + nextEnd) / 2;
				setLeft(children.get(middle), children.get(nextMiddle));
				linkChildren(children, nextStart, nextEnd, nextMiddle);
			}
			if (middle < end) {
				final int nextStart = middle + 1;
				final int nextEnd = end;
				final int nextMiddle = (nextStart + nextEnd / 2);
				if (nextMiddle < end) {
					setRight(children.get(middle), children.get(nextMiddle));
					linkChildren(children, nextStart, nextEnd, nextMiddle);
				}
			}
		}
	}

	private boolean containsValue(final int node) {
		int key = data.get(node);
		return (key & ~(0xffff)) != 0;
	}
	
	private int getSize(final int node) {
		return data.get(node + 2);
	}
	
	private void setSize(final int node, final int size) {
		data.set(node + 2, size);
	}
	
	private void setMiddleChild(final int node, final int middleChild) {
		data.set(node + 3, middleChild);					
	}
	
	private void setLeft(final int node, final int left) {
		data.set(node + 4, left);
	}

	private void setRight(final int node, final int right) {
		data.set(node + 5, right);
	}
}
