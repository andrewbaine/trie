package com.lawdawg.trie;

import java.nio.ByteBuffer;

public class RawTrieReader {

	private final TrieBuffer trie;

	public RawTrieReader(final ByteBuffer buffer) {
		this.trie = new TrieBuffer(buffer);
	}
	
	// returns -1 if we need to move right
	// returns 0 if this node is a prefix match;
	// returns 1 if if we need to move left
	public int compareThisNodeToPrefix(final int node, final ByteBuffer key, final int start) {
		final int nodeLength = trie.getKeyLength(node);
		for (int i = 0; i < nodeLength; i++) {
			if (i + start < key.limit()) {
				final int diff = trie.getKeyCharAt(node, i) - key.get(i + start);
				if (diff != 0) {
					return diff;
				}
			} else {
				return 1; // the key has more characters than this node, we need to move left
			}
			
		}
		return 0;
	}

	private Integer matchChild(Integer node, final ByteBuffer chars, final int start) {
		Integer child = trie.getChild(node);
		while (child != null) {
			final int comp = this.compareThisNodeToPrefix(child, chars, start);
			if (comp > 0) {
				child = trie.getLeft(child);
			} else if (comp < 0) {
				child = trie.getRight(child);
			} else {
				return child;
			}
		}
		return null;
	}
	
	public final Integer get(final ByteBuffer chars) {
		Integer node = 0;
		int start = 0;
		final int length = chars.remaining();
		final int comp = this.compareThisNodeToPrefix(node, chars, 0);
		if (comp != 0) { // root node is not a prefix of chars
			return null;
		}
		while (node != null) {
			start += this.trie.getKeyLength(node);
			if (start == length) {
				return trie.getValue(node);
			} else 
			node = matchChild(node, chars, start);
		}
		return null;
	}
}
