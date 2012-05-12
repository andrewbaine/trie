package com.lawdawg.trie;

import java.nio.ByteBuffer;

public class RawTrieNode {

	private RawTrieNode() {
	}

	// returns -1 if we need to move right
	// returns 0 if this node is a prefix match;
	// returns 1 if if we need to move left
	public static int compareThisNodeToPrefix(final ByteBuffer data, final int node, final CharSequence cs, final int start) {
		final int n = getN(data, node);
		final int length = cs.length();
		final int stop = n < length ? n : length;
		for (int i = 0; i < stop; i++) {
			final int diff = getKey(data, node, i) - cs.charAt(start + i);
			if (diff != 0) {
				return diff;
			}
		}
		return 0;
	}
	
	public static byte getN(final ByteBuffer data, final int node) {
		return data.get(node);
	}
	private static byte getFlags(final ByteBuffer data, final int node) {
		return data.get(node + 1);
	}
	public static Byte getKey(final ByteBuffer data, final int node, final int index) {
		if (index == 0 && getN(data, node) == 1) {
			return data.get(node + 2);
		} else {
			throw new IndexOutOfBoundsException();
		}
	}
	public static Byte getValue(final ByteBuffer data, final int node) {
		return hasValue(data, node) ? data.get(node + 3) : null;
	}
	public static Integer getLeft(final ByteBuffer data, final int node) {
		return hasLeft(data, node) ? data.getInt(node + 4) : null;
	}
	
	public static Integer getRight(final ByteBuffer data, final int node) {
		return hasRight(data, node) ? data.getInt(node + 8) : null;
	}

	public static Integer getChild(final ByteBuffer data, final int node) {
		return hasChild(data, node) ? data.getInt(node + 12) : null;
	}
	
	private static void setN(final ByteBuffer data, final int node, final byte n) {
		if (n == 0 || n == 1) {
			data.put(node, n);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	private static void setFlags(final ByteBuffer data, final int node, final byte flags) {
		data.put(node + 1, flags);
	}
	
	public static void setKey(final ByteBuffer data, final int node, final Byte key) {
		if (key == null) {
			setN(data, node, (byte) 0);
		} else {
			setN(data, node, (byte) 1);
			data.put(node + 2, key);
		}
	}
	
	public static void setValue(final ByteBuffer data, final int node, final Byte value) {
		setHasValue(data, node, value != null);
		if (value != null) {
			data.put(node + 3, value);
		}
	}

	// Interpretation of the flags
	public static boolean hasValue(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 1) != 0;
	}

	public static boolean hasLeft(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 2) != 0;
	}

	public static boolean hasRight(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 4) != 0;
	}

	public static boolean hasChild(final ByteBuffer data, final int node) {
		return (getFlags(data, node) & 8) != 0;
	}
	
	public static void setFlag(final ByteBuffer data, final int node, final boolean value, final int position) {
		byte flags = getFlags(data, node);
		if (value) {
			flags |= position;
		} else {
			flags &= position;
		}
		setFlags(data, node, flags);
	}
	
	public static void setHasValue(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 1);
	}

	public static void setHasLeft(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 2);
	}

	public static void setHasRight(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 4);
	}

	public static void setHasChild(final ByteBuffer data, final int node, final boolean flag) {
		setFlag(data, node, flag, 8);
	}
	
	private static void checkOrder(final Integer left, final Integer child, final Integer right) {
		if (left != null) {
			if (child != null && child <= left) {
				throw new IllegalArgumentException();
			} else if (right != null && right <= left) {
				throw new IllegalArgumentException();
			}
		}
		if (child != null) {
			if (right != null && right <= child) {
				throw new IllegalArgumentException();
			}
		}
	}
	
	static void setLeft(final ByteBuffer data, final int node, final Integer left) {
		checkOrder(left, getChild(data, node), getRight(data, node));
		setHasLeft(data, node, left != null);
		if (left != null) {
			data.putInt(node + 4, left);
		}
	}

	static void setRight(final ByteBuffer data, final int node, final Integer right) {
		checkOrder(getLeft(data, node), getChild(data,node), right);
		setHasRight(data, node, right != null);
		if (right != null) {
			data.putInt(node + 8, right);
		}
	}

	static void setChild(final ByteBuffer data, final int node, final Integer child) {
		checkOrder(getLeft(data, node), child, getRight(data, node));
		setHasChild(data, node, child != null);
		if (child != null) {
			data.putInt(node + 12, child);
		}
	}

	public static int getNodeLength(final ByteBuffer data, final int node) {
		return 16;
	}
}
