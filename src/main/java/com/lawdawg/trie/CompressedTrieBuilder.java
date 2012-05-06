package com.lawdawg.trie;

import java.util.ArrayList;
import java.util.List;

public class CompressedTrieBuilder {
	
	private int index;
	private final List<Integer> data;
	
	public void compressNode(final int node, final List<Integer> children) {

		final int compressionPathLength = compressionPathLength(node);
		final int numberOfChildren = children.size();
		
		final int compressionInfo = (compressionPathLength << 16) | numberOfChildren;

		
		data.set(index++, compressionInfo);
		data.set(index++, encodedKey);
		
		
		data.set(index++, child);
	}
	
	private int compressionPathLength(final int node) {
		int length = 0;
		for (int n = node; child(node) != -1 ||
				(left(child(node)) == -1 && right(child(node)) == -1); n = child(n)) {
			length++;
		}
		return length;
	}
	
	private int child(final int node) {
		return data.get(node + 3);
	}
	
	private int left(final int node) {
		return data.get(node + 4);
	}
	
	private int right(final int node) {
		return data.get(node + 5);
	}
}
