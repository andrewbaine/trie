package com.lawdawg.trie;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class CompressedTrie {
	private static class TrieCompressor {

		private final Stack<Integer> path = new Stack<Integer>();
		
		private final List<Integer> data;
		private final List<Integer> compressedData = new ArrayList<Integer>();
		private final List<List<Integer>> sibilings = new ArrayList<List<Integer>>();
		
		private int n = 0;
		
		public TrieCompressor(final ArrayList<Integer> data) {
			this.data = data;
		}
		
		public void compress() {
			for (int i = 0; i < data.size();) {
				
			}
		}
		
		private final char[] compressionPath = new char[64];
		private void pushCompressedNode(final int node) {
			
			final int compressedNode = compressedData.size();
			this.path.push(compressedNode);		
			final int encodedKey = data.get(node);
			final int value = data.get(node + 1);
			final int size = data.get(node + 2);
			final int middle = data.get(node + 3);
			final int left = data.get(node + 4);
			final int right = data.get(node + 5);
			
			
			// (0) put all the key data in
			compressedData.add(encodedKey);
			
			// figure out the compression path - this will b 0 or more nodes
			int compressionCount = 0;
			for (int n = node; !hasValue(n) && hasOneChild(n); n = middleChild(n)) {
				final int middleChild = middleChild(n);
				final char c = getValue(middleChild);
				compressionPath[compressionCount++] = c;
			}
						
			// add the compressed data
			int dataLength = (compressionCount + 1) / 2;
			for (int i = 0; i < dataLength; i++) {
				final int index = 2 * i;
				final char c = compressionPath[index];
				final char d = index + 1 < compressionPath.length ? compressionPath[index + 1] : 0;
				final int encoding = c << 16 + d;
				compressedData.add(encoding);
			}
			// update the encoded key
			encodedKey |= (compressionCount << COMPRESSION_COUNT_FLAG);

			// (1) value
			if (hasValue(encodedKey)) {
				compressedData.add(value);
			}
			
			// (2) size
			compressedData.add(size); // do we need this?
			
			// (3) middle
			if (middle == -1) {
				encodedKey &= ~(1 << HAS_MIDDLE_CHILD_FLAG);
			} else {
				encodedKey |= (1 << HAS_MIDDLE_CHILD_FLAG);
				compressedData.add(middle);
			}

			// (4) left
			if (left == -1) {
				encodedKey &= ~(1 << HAS_LEFT_FLAG);
			} else {
				encodedKey |= (1 << HAS_LEFT_FLAG);
				compressedData.add(left);
			}
			
			// (5) right
			if (right == -1) {
				encodedKey &= ~(1 << HAS_RIGHT_FLAG);
			} else {
				encodedKey |= (1 << HAS_RIGHT_FLAG);
				compressedData.add(right);
			}
						
			// This is important:
			compressedData.set(compressedNode, encodedKey);
		}
		
		private void popCompressedNode() {
			int node = path.pop();
			final List<Integer> children = childrenStack.pop();
			
		}
	}
}
