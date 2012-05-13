
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
	
	private static final Logger logger = LoggerFactory.getLogger(TrieBuilder.class);
	
	private final ValueBuffer valueBuffer = new ValueBuffer();
	private final TrieBuffer trieBuffer = new TrieBuffer();

	public TrieBuilder() {
		pushFreshListOntoChildrenStack();
		pushNode(null, null);
	}

	private int node = 0;
	private final ByteBuffer bb = ByteBuffer.wrap(new byte[1]);
	
	// move <node> forward one
	private void pushNode(final Byte key, final ByteBuffer value) {

		this.path.push(node);
		this.childrenStack.peek().add(node); // this list includes all of the siblings of this node

		this.trieBuffer.appendNode(node);
		
		if (key != null) {
			bb.clear();
			bb.put(0, key);
			this.trieBuffer.appendKey(node, bb);			
		}


		if (value != null) {
			final int valuePosition = this.valueBuffer.position();
			this.valueBuffer.put(value);
			this.trieBuffer.setValuePosition(node, valuePosition);
		}
		
		pushFreshListOntoChildrenStack();
		final int expected = 18 + ((key == null) ? 0 : 1);
		final int actual = this.trieBuffer.nodeLength(node);
		if (expected != actual) {
			logger.info("expected {} but got {} actual", expected, actual);
			throw new RuntimeException();
		}
		node += actual;
	}

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
		final int parent = this.path.pop();
		final List<Integer> children = this.childrenStack.pop();
		this.linkChildren(parent, children);
	}
	
	public void put(final ByteBuffer key, final ByteBuffer value) {
		final int keyLength = key.limit();
		
		int sharedNode = 0;
		int sharedPrefix = 0;
		for (Integer node : this.path) {
			final int length = this.trieBuffer.getKeyLength(node);
			int shared = 0;
			boolean match = true;
			for (int i = 0; i < length; i++) {
				if (sharedPrefix + shared == keyLength ||
						trieBuffer.getKeyCharAt(node, i) != key.get(sharedPrefix + shared)) {
					match = false;
					break;
				} else {
					++shared;
				}
			}
			if (!match) {
				key.position(0);
				break;
			} else {
				sharedNode++;
				sharedPrefix += shared;
			}
		}

		// this key shares <s> characters with our current path
		// rewind to the node in the 
		while (sharedNode < path.size()) {
			popNode();
		}

		for (int i = sharedPrefix; i < keyLength - 1; i++) {
			final byte b = key.get(i);
			pushNode(b, null);
		}
		// note, this will choke on 0-length keys, oh well
		pushNode(key.get(keyLength - 1), value);
	}

	private void linkChildren(final int parent, final List<Integer> children) {
		if (children.size() > 0) {
			final int length = children.size();
			final int middle = length / 2;
			this.trieBuffer.setChild(parent, children.get(middle));
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
					this.trieBuffer.setLeft(children.get(middle), children.get(nextMiddle));
					enqueue(nextStart, nextEnd);
				}
				if (middle < end) {
					final int nextStart = middle + 1;
					final int nextEnd = end;
					final int nextMiddle = (nextStart + nextEnd) / 2;
					if (nextMiddle < end) {
						// middle.right = nextMiddle
						this.trieBuffer.setRight(children.get(middle), children.get(nextMiddle));
						enqueue(nextStart, nextEnd);
					}
				}
			}
		}
	}

	public void cleanup() {
		while (!path.empty()) {
			popNode();
		}
	}

	public RawTrieReader getReader() {
		return new RawTrieReader(this.trieBuffer, this.valueBuffer);
	}
}
