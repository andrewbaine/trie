package com.lawdawg.trie;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieBuffer {

	private static final int KEY_LENGTH_OFFSET = 0;
	private static final int FLAGS_OFFSET = 1;
	private static final int VALUE_POSITION_OFFSET = 2;
	private static final int LEFT_OFFSET = 6;
	private static final int RIGHT_OFFSET = 10;
	private static final int CHILD_OFFSET = 14;
	private static final int KEY_OFFSET = 18;


	public void appendNode(int node) {
		ensure(node + 18);
		data.position(node);
		data.put((byte) 0); // key_length
		data.put((byte) 0); // flags
		data.putInt(0);     // value_position
		data.putInt(0);     // left
		data.putInt(0);     // right
		data.putInt(0);     // child
		final int position = data.position();
		if (position - node != 18) {
			throw new RuntimeException("unexpected node length");
		}
	}

	private static final Logger logger = LoggerFactory.getLogger(TrieBuffer.class);
	
	private ByteBuffer data = ByteBuffer.allocate(32);
	
	/**
	 * make sure we can add length bytes to data
	 * @param length
	 */
	private void ensure(final int requiredCapacity) {
		final int capacity = data.capacity();
		if (capacity < requiredCapacity) {
			logger.info("begin increasing capacity from {}", capacity);
			final int newCapacity = (requiredCapacity > (2 * capacity)) ? 
					requiredCapacity : (2 * capacity);
			final ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);

			this.data.flip();
			newBuffer.put(this.data);
			this.data = newBuffer;

			logger.info("finished increasing capacity to {}", data.capacity());
		}
	}
	
	public void position(final int position) {
		this.data.position(position);
	}

	public void setValuePosition(final int node, final int vp) {
		data.putInt(node + VALUE_POSITION_OFFSET, vp);
		data.put(node + FLAGS_OFFSET, (byte)1);
	}

	public void appendKey(final int node, final ByteBuffer key) {
		final int keyLength = key.remaining();
		if (keyLength > Byte.MAX_VALUE) {
			throw new IllegalArgumentException("key is too long");
		}
		
		ensure(node + KEY_OFFSET + keyLength);
		data.position(node + KEY_OFFSET);
		data.put(key);
		
		data.put(node + KEY_LENGTH_OFFSET, (byte)keyLength);
	}

	public int nodeLength(int node) {
		return 18 + getKeyLength(node);
	}

	public int getKeyLength(int node) {
		return this.data.get(node + KEY_LENGTH_OFFSET);
	}

	public int getKeyCharAt(int node, int index) {
		return data.get(node + KEY_OFFSET + index);
	}

	private static final byte VALUE_FLAG = 1;
	private static final byte LEFT_FLAG = 2;
	private static final byte RIGHT_FLAG = 4;
	private static final byte CHILD_FLAG = 8;
	
	public Integer getLeft(final int node) {
		if ((this.getFlags(node) & LEFT_FLAG) != 0) {
			return data.getInt(node + LEFT_OFFSET);
		} else {
			return null;
		}
	}

	public Integer getRight(final int node) {
		if ((this.getFlags(node) & RIGHT_FLAG) != 0) {
			return data.getInt(node + RIGHT_OFFSET);
		} else {
			return null;
		}
	}
	
	public Integer getChild(final int node) {
		if ((this.getFlags(node) & CHILD_FLAG) != 0) {
			return data.getInt(node + CHILD_OFFSET);
		} else {
			return null;
		}
	}
	
	public void setLeft(final int node, final int left) {
		checkOrder(left, getChild(node), getRight(node));
		setFlags(node, (byte)(getFlags(node) | LEFT_FLAG));
		data.putInt(node + LEFT_OFFSET, left);
	}
	
	public void setRight(final int node, final int right) {
		checkOrder(getLeft(node), getChild(node), right);
		setFlags(node, (byte)(getFlags(node) | RIGHT_FLAG));
		data.putInt(node + RIGHT_OFFSET, right);
	}
	
	public void setChild(final int node, final int child) {
		checkOrder(getLeft(node), child, getRight(node));
		setFlags(node, (byte)(getFlags(node) | CHILD_FLAG));
		data.putInt(node + CHILD_OFFSET, child);
	}
	
	public byte getFlags(final int node) {
		return data.get(node + FLAGS_OFFSET);
	}
	
	public void setFlags(final int node, final byte flags) {
		data.put(node + FLAGS_OFFSET, flags);
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

	public boolean hasValue(Integer node) {
		return (getFlags(node) & VALUE_FLAG) != 0;
	}

	public Integer getValue(Integer node) {
		return hasValue(node) ? data.getInt(node + VALUE_POSITION_OFFSET) : null;
	}
}
