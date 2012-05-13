package com.lawdawg.trie;

import java.nio.ByteBuffer;

public class RawTrieNode {

	private RawTrieNode() {
	}
	
	private static byte getKeyLength(final ByteBuffer data, final int node) {
		return data.get(node);
	}
	private static byte getFlags(final ByteBuffer data, final int node) {
		return data.get(node + 1);
	}

	private static Integer getLeft(final ByteBuffer data, final int node) {
		return hasLeft(data, node) ? data.getInt(node + 2) : null;
	}
	
	private static Integer getRight(final ByteBuffer data, final int node) {
		return hasRight(data, node) ? data.getInt(node + 6) : null;
	}

	private static Integer getChild(final ByteBuffer data, final int node) {
		return hasChild(data, node) ? data.getInt(node + 10) : null;
	}
	
	private static Integer getValuePosition(final ByteBuffer data, final int node) {
		return hasValue(data, node) ? data.getInt(node + 14) : null;
	}

	private static Integer getValueLimit(final ByteBuffer data, final int node) {
		return hasValue(data, node) ? data.getInt(node + 18) : null;
	}
	
	private static Byte getKey(final ByteBuffer data, final int node, final int i) {
		if (i < getKeyLength(data, node)) {
			return data.get(node + 22 + i);
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	
	private static void setKeyLength(final ByteBuffer data, final int node, final byte n) {
		if (n == 0 || n == 1) {
			data.put(node, n);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private static void setFlags(final ByteBuffer data, final int node, final byte flags) {
		data.put(node + 1, flags);
	}

	// Interpretation of the flags
	private static boolean hasValue(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 1) != 0;
	}

	private static boolean hasLeft(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 2) != 0;
	}

	private static boolean hasRight(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 4) != 0;
	}

	private static boolean hasChild(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 8) != 0;
	}
	
	private static void setFlag(final ByteBuffer data, final int node, final boolean value, final int position) {
		byte flags = getFlags(data, node);
		if (value) {
			flags |= position;
		} else {
			flags &= position;
		}
		setFlags(data, node, flags);
	}
	
	private static void setHasValue(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 1);
	}

	private static void setHasLeft(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 2);
	}

	private static void setHasRight(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 4);
	}

	private static void setHasChild(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 8);
	}

	private static int getNodeLength(final ByteBuffer data, final int node) {
		return 1    // keylength
				+ 1 // flags 
				+ 4 // left
				+ 4 // right
				+ 4 // child
				+ 4 // value-position
				+ 4 // value-limit
				+ getKeyLength(data, node);
	}
}
