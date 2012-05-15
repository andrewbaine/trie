
package com.lawdawg.trie;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(TrieBuilder.class);

	private final TrieBuffer trieBuffer;
	private final ByteBuffer bb = ByteBuffer.wrap(new byte[1]);

	private int node = 0;

	public TrieBuilder(final int trieCapacity, final int valueCapacity) {
		trieBuffer = new TrieBuffer(trieCapacity);
		pushFreshListOntoChildrenStack();
		pushNode(null, null);
	}
	
	public RawTrieReader getReader() {
		return new RawTrieReader(this.trieBuffer);
	}

	// move <node> forward one
	private void pushNode(final Byte key, final Integer value) {

		this.path.push(node);
		this.childrenStack.peek().add(node); // this list includes all of the siblings of this node

		this.trieBuffer.appendUncompressedNode(node);
		
		if (key != null) {
			bb.clear();
			bb.put(0, key);
			this.trieBuffer.appendKey(node, bb);			
		}

		this.trieBuffer.setValue(node, value);
		pushFreshListOntoChildrenStack();
		final int expected = node + 18 + ((key == null) ? 0 : 1);
		node = this.trieBuffer.nodeEnd(node);
		if (expected != node) {
			logger.info("expected {} but got {} actual", expected, node);
			throw new RuntimeException();
		}
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
	
	public void put(final ByteBuffer key, final int value) {
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
		logger.info("uncompressed trie size: {}", node);
		compress();
	}

	private static class N {
		public N(final int node, final int depth) {
			this.node = node;
			this.depth = depth;
		}
		public final int node;
		public final int depth;
	}
	
	private void compress() {
		final Stack<Integer> s = new Stack<Integer>();
		final PriorityQueue<Integer> pq = new PriorityQueue<Integer>();
		
		final Stack<List<Integer>> childrenStack = new Stack<List<Integer>>();
		
		final Stack<N> uncompressed = new Stack<N>();
		uncompressed.push(new N(0, 0));

		int previousDepth = -1;
		int compressedNode = 0;
		while (!uncompressed.isEmpty()) {

			final N n = uncompressed.pop();
			final int node = n.node;
			final int depth = n.depth;
			
			if (depth < previousDepth) {
				if (depth != previousDepth - 1) {
					logger.error("we should not ascend more than 1 link at a time");
					throw new RuntimeException();
				}
				final List<Integer> children = childrenStack.pop();
				linkChildren(node, children);

				// we're moving back up the tree; no compression is necessary
				// but its now safe to link this node's children
			} else {
				if (depth > previousDepth) { // we just moved down
					if (depth != previousDepth + 1) {
						logger.error("we should not descend more than 1 link at a time");
						throw new RuntimeException();						
					}
					final ArrayList<Integer> siblings = new ArrayList<Integer>();
					childrenStack.push(siblings);
				}

				// we moved down or across
				childrenStack.peek().add(compressedNode);
				trieBuffer.moveNode(node, compressedNode);
				trieBuffer.compressInPlace(compressedNode);
				//
				if (trieBuffer.hasChild(compressedNode)) {
					// put it back on the stack and add its children at a greater depth
					uncompressed.push(new N(compressedNode, depth));
					
					pq.clear();
					s.clear();
					s.push(trieBuffer.getChild(compressedNode));
					while (!s.isEmpty()) {
						int x = s.pop();
						pq.add(-1 * x);
						if (trieBuffer.hasLeft(x)) {
							s.push(trieBuffer.getLeft(x));
						}
						if (trieBuffer.hasRight(x)) {
							s.push(trieBuffer.getRight(x));
						}
					}
					while (!pq.isEmpty()) {
						final int x = pq.remove();
						uncompressed.push(new N(-1 * x, depth + 1));
					}
				}
				compressedNode = trieBuffer.nodeEnd(compressedNode);
			}
			previousDepth = depth;
		}
		this.trieBuffer.limit(compressedNode);
	}

}
