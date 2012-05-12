package com.lawdawg.trie;

import java.nio.ByteBuffer;

public class RawTrieReader {
	
	private final ByteBuffer data;

	public RawTrieReader(final ByteBuffer raw) {
		this.data = raw;
	}

	private Integer matchChild(Integer node, final CharSequence chars, final int start) {
		Integer child = RawTrieNode.getChild(data, node);
		while (child != null) {
			final int comp = RawTrieNode.compareThisNodeToPrefix(data, child, chars, start);
			if (comp > 0) {
				child = RawTrieNode.getLeft(data, child);
			} else if (comp < 0) {
				child = RawTrieNode.getRight(data, child);
			} else {
				return child;
			}
		}
		return null;
	}
	
	public final Character get(final CharSequence chars) {
		Integer node = 0;
		int i = 0;
		final int length = chars.length();
		while (true) {
			Integer child = matchChild(node, chars, i);
			if (child == null) {
				return null;
			} else {
				final int increment = RawTrieNode.getN(data, child);
				i += increment;
				node = child;
				if (i == length) {
					final Byte value = RawTrieNode.getValue(data, node);
					return value == null ? null : (char)(byte)value;
				}
			}
		}
		
	}
}
