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



	private static final Logger logger = LoggerFactory.getLogger(TrieBuffer.class);
	private ByteBuffer buffer;
	
	public TrieBuffer(final int capacity) {
		this.buffer = ByteBuffer.allocate(capacity);
	}
	
	public void appendNode(int node) {
		ensure(node + 18);
		buffer.position(node);
		buffer.put((byte) 0); // key_length
		buffer.put((byte) 0); // flags
		buffer.putInt(0);     // value_position
		buffer.putInt(0);     // left
		buffer.putInt(0);     // right
		buffer.putInt(0);     // child
		final int position = buffer.position();
		if (position - node != 18) {
			throw new RuntimeException("unexpected node length");
		}
	}
	
	/**
	 * make sure we can add length bytes to data
	 * @param length
	 */
	private void ensure(final int requiredCapacity) {
		final int capacity = buffer.capacity();
		if (capacity < requiredCapacity) {
			logger.info("begin increasing capacity from {}", capacity);
			final int newCapacity = (requiredCapacity > (2 * capacity)) ? 
					requiredCapacity : (2 * capacity);
			final ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);

			this.buffer.flip();
			newBuffer.put(this.buffer);
			this.buffer = newBuffer;

			logger.info("finished increasing capacity to {}", buffer.capacity());
		}
	}
	
	public void position(final int position) {
		this.buffer.position(position);
	}

	public void setValuePosition(final int node, final int vp) {
		buffer.putInt(node + VALUE_POSITION_OFFSET, vp);
		buffer.put(node + FLAGS_OFFSET, (byte)1);
	}

	public void appendKey(final int node, final ByteBuffer key) {
		final int keyLength = key.remaining();
		if (keyLength > Byte.MAX_VALUE) {
			throw new IllegalArgumentException("key is too long");
		}
		
		ensure(node + KEY_OFFSET + keyLength);
		buffer.position(node + KEY_OFFSET);
		buffer.put(key);
		
		buffer.put(node + KEY_LENGTH_OFFSET, (byte)keyLength);
	}

	public int nodeLength(int node) {
		return 18 + getKeyLength(node);
	}

	public int getKeyLength(int node) {
		return this.buffer.get(node + KEY_LENGTH_OFFSET);
	}

	public int getKeyCharAt(int node, int index) {
		return buffer.get(node + KEY_OFFSET + index);
	}

	private static final byte VALUE_FLAG = 1;
	private static final byte LEFT_FLAG = 2;
	private static final byte RIGHT_FLAG = 4;
	private static final byte CHILD_FLAG = 8;
	
	public Integer getLeft(final int node) {
		if ((this.getFlags(node) & LEFT_FLAG) != 0) {
			return buffer.getInt(node + LEFT_OFFSET);
		} else {
			return null;
		}
	}

	public Integer getRight(final int node) {
		if ((this.getFlags(node) & RIGHT_FLAG) != 0) {
			return buffer.getInt(node + RIGHT_OFFSET);
		} else {
			return null;
		}
	}
	
	public Integer getChild(final int node) {
		if ((this.getFlags(node) & CHILD_FLAG) != 0) {
			return buffer.getInt(node + CHILD_OFFSET);
		} else {
			return null;
		}
	}
	
	public void setLeft(final int node, final int left) {
		checkOrder(left, getChild(node), getRight(node));
		setFlags(node, (byte)(getFlags(node) | LEFT_FLAG));
		buffer.putInt(node + LEFT_OFFSET, left);
	}
	
	public void setRight(final int node, final int right) {
		checkOrder(getLeft(node), getChild(node), right);
		setFlags(node, (byte)(getFlags(node) | RIGHT_FLAG));
		buffer.putInt(node + RIGHT_OFFSET, right);
	}
	
	public void setChild(final int node, final int child) {
		checkOrder(getLeft(node), child, getRight(node));
		setFlags(node, (byte)(getFlags(node) | CHILD_FLAG));
		buffer.putInt(node + CHILD_OFFSET, child);
	}
	
	public byte getFlags(final int node) {
		return buffer.get(node + FLAGS_OFFSET);
	}
	
	public void setFlags(final int node, final byte flags) {
		buffer.put(node + FLAGS_OFFSET, flags);
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
		return hasValue(node) ? buffer.getInt(node + VALUE_POSITION_OFFSET) : null;
	}
}
