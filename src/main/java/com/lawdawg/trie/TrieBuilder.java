
package com.lawdawg.trie;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieBuilder {
	
	private ByteBuffer data = ByteBuffer.allocate(4096);

	private void ensureCapacity(final int capacity) {
		final int limit = data.limit();
		if (capacity >= limit) {
			final int newLength = 2 * limit;
			logger.info("begin increasing capacity to {}", newLength);
			data.position(limit);
			data.flip();
			ByteBuffer newData = ByteBuffer.allocate(newLength);
			newData.put(data);
			data = newData;
			logger.info("finished increasing capacity to {}", newLength);
		}
	}

	private static final char A = 'A';
	private static final char Z = 'Z';
	private static final char a = 'a';
	private static final char z = 'z';
	private static final char ZERO = '0';
	private static final char NINE = '9';
	
	private static boolean isValidKeyCharacter(final char ch) {
		if (A <= ch && ch <= Z) {
			return true;
		} else if (a <= ch && ch <= z) {
			return true;
		} else if (ZERO <= ch && ch <= NINE) {
			return true;
		} else {
			return ch == (byte)'-';
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TrieBuilder.class);

	public TrieBuilder() {
		pushFreshListOntoChildrenStack();
		pushNode(null, null);
	}

	private int node = 0;
	// move <node> forward one
	private void pushNode(final Byte key, final Byte value) {

		final int length = RawTrieNode.getNodeLength(data, node);
		this.ensureCapacity(node + length);

		this.path.push(node);
		this.prefix.push(key == null ? null : (char)(byte)key);
		this.childrenStack.peek().add(node); // this list includes all of the siblings of this node

		RawTrieNode.setKey(data, node, key);
		RawTrieNode.setValue(data, node, value);
		
		pushFreshListOntoChildrenStack();
		this.node += length;
	}
	
	private final Stack<Character> prefix = new Stack<Character>();
	private final Stack<Integer> path = new Stack<Integer>();
	
	private final List<List<Integer>> lists = new ArrayList<List<Integer>>();
	private final Stack<List<Integer>> childrenStack = new Stack<List<Integer>>();
	
	private void pushFreshListOntoChildrenStack() {
		int x = childrenStack.size();
		while (x >= lists.size()) {
			lists.add(new ArrayList<Integer>());
		}
		final List<Integer> list = lists.get(x);
		list.clear();
		this.childrenStack.push(list); // add a new empty list
	}
	
	private void popNode() {
		this.prefix.pop();
		final int parent = this.path.pop();
		final List<Integer> children = this.childrenStack.pop();
		this.linkChildren(parent, children);
	}
	
	public void put(final CharSequence key, final byte value) {
		if (data.get(16) == 0) {
			System.out.println("still 0");
		}
		if ("aba".equals(key)) {
			System.out.println("here we are");
		}
		final int keyLength = key.length();
		int shared = 0;
		for (Character c : this.prefix) {
			if (c != null) {
				if (shared < keyLength && key.charAt(shared) == (char)c) {
					++shared;
				} else {
					break;
				}				
			}
		}

		// this key shares <s> characters with our current path
		// rewind to the node in the 
		while (shared + 1 < prefix.size()) {
			popNode();
		}

		for (int i = shared; i < keyLength; i++) {
			final char c = Character.toLowerCase(key.charAt(i));
			if (isValidKeyCharacter(c)) {	
				pushNode((byte) c, value);
			} else {
				throw new IllegalArgumentException(key.toString());
			}
		}
		if (data.get(16) == 0) {
			System.out.println("the shit has hit the fan");
		}
	}

	private void linkChildren(final int parent, final List<Integer> children) {
		if (children.size() > 0) {
			final int length = children.size();
			final int middle = length / 2;
			RawTrieNode.setChild(this.data, parent, children.get(middle));
			// link siblings to each other
			linkSiblings(children);
		}
	}

	
	private final Queue<Integer> linkerStack = new LinkedList<Integer>();
	private void enqueue(final int start, final int end) {
		linkerStack.add(start);
		linkerStack.add(end);
	}

	private void linkSiblings(final List<Integer> children) {
		linkerStack.clear();
		enqueue(0, children.size());
		while (!linkerStack.isEmpty()) {
			final int start = linkerStack.remove();
			final int end = linkerStack.remove();

			if (start < end) {
				final int middle = (start + end) / 2;
				if (start < middle) {
					final int nextStart = start;
					final int nextEnd = middle;
					final int nextMiddle = (nextStart + nextEnd) / 2;

					// middle.left = nextMiddle
					if (children.get(middle) == children.get(nextMiddle)) {
						System.out.println("wtf");
					}
					RawTrieNode.setLeft(this.data, children.get(middle), children.get(nextMiddle));
					enqueue(nextStart, nextEnd);
				}
				if (middle < end) {
					final int nextStart = middle + 1;
					final int nextEnd = end;
					final int nextMiddle = (nextStart + nextEnd) / 2;
					if (nextMiddle < end) {
						// middle.right = nextMiddle
						RawTrieNode.setRight(this.data, children.get(middle), children.get(nextMiddle));
						enqueue(nextStart, nextEnd);
					}
				}
			}
		}
	}

	public void cleanup() {
		logger.info("begin cleanup");
		while (!path.empty()) {
			popNode();
		}
		logger.info("finished cleanup");
	}

	public ByteBuffer getData() {
		return this.data;
	}
}
