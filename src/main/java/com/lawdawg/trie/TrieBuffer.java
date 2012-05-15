package com.lawdawg.trie;

import java.nio.ByteBuffer;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieBuffer {

	// an uncompressed node stores the following information
	// FIELD	OFFSET	BYTES
	// keyLength	 0		1
	// flags		 1		1
	// value		 2		4
	// left			 6		4
	// right		10		4
	// child		14		4
	// key			18		n, where n < Byte.MAX_VALUE
	
	private int keyLengthOffset(final int node) {
		return node;
	}
	
	private int flagsOffset(final int node) {
		return node + 1;
	}
	
	private int valueOffset(final int node) {
		return node + 2;
	}
	
	private int leftOffset(final int node) {
		if (this.isCompressed(node)) {
			return valueOffset(node) + (this.hasValue(node) ? 4 : 0);
		} else {
			return node + 6;
		}
	}
	
	private int rightOffset(final int node) {
		if (this.isCompressed(node)) {
			return this.leftOffset(node) + (this.hasLeft(node) ? 4 : 0);
		} else {
			return node + 10;
		}
	}

	private int childOffset(final int node) {
		if (this.isCompressed(node)) {
			return this.rightOffset(node) + (this.hasRight(node) ? 4 : 0);
		} else {
			return node + 14;
		}
	}
	
	private int keyOffset(final int node) {
		if (this.isCompressed(node)) {
			return this.childOffset(node) + (this.hasChild(node) ? 4 : 0);
		} else {
			return node  + 18;
		}		
	}
	
	public int nodeEnd(final int node) {
		return keyOffset(node) + getKeyLength(node);
	}

	private static final Logger logger = LoggerFactory.getLogger(TrieBuffer.class);
	private ByteBuffer buffer;
	
	public TrieBuffer(final int capacity) {
		this.buffer = ByteBuffer.allocate(capacity);
	}
	
	public void appendUncompressedNode(int node) {
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

	public void appendKey(final int node, final ByteBuffer key) {
		buffer.position(this.nodeEnd(node));
		this.setKeyLength(node, this.getKeyLength(node) + key.remaining());
		ensure(nodeEnd(node));
		buffer.put(key);
	}
	
	public int getKeyLength(int node) {
		return this.buffer.get(keyLengthOffset(node));
	}
	
	public void setKeyLength(final int node, final int keyLength) {
		if (keyLength > Byte.MAX_VALUE) {
			throw new IllegalArgumentException();
		}
		this.buffer.put(this.keyLengthOffset(node), (byte) keyLength);	
	}

	public byte getKeyCharAt(int node, int index) {
		return buffer.get(keyOffset(node) + index);
	}

	private static final byte VALUE_FLAG = 1;
	private static final byte LEFT_FLAG = 2;
	private static final byte RIGHT_FLAG = 4;
	private static final byte CHILD_FLAG = 8;
	private static final byte COMPRESSED_FLAG = 16;
	

	public boolean isCompressed(final int node) {
		return (this.getFlags(node) & COMPRESSED_FLAG) != 0;		
	}
	
	public void setIsCompressed(final int node) {
		setFlags(node, (byte)(getFlags(node) | COMPRESSED_FLAG));
	}
	
	public boolean hasLeft(final int node) {
		return (this.getFlags(node) & LEFT_FLAG) != 0;
	}
	
	public Integer getLeft(final int node) {
		if (hasLeft(node)) {
			return buffer.getInt(leftOffset(node));
		} else {
			return null;
		}
	}

	public boolean hasRight(final int node) {
		return (this.getFlags(node) & RIGHT_FLAG) != 0;
	}
	
	public Integer getRight(final int node) {
		if (this.hasRight(node)) {
			return buffer.getInt(rightOffset(node));
		} else {
			return null;
		}
	}
	
	public boolean hasChild(int node) {
		return (this.getFlags(node) & CHILD_FLAG) != 0;
	}
	
	public Integer getChild(final int node) {
		if (hasChild(node)) {
			return buffer.getInt(childOffset(node));
		} else {
			return null;
		}
	}

	public void setValue(final int node, final Integer value) {
		if (value == null) {
			buffer.put(flagsOffset(node), (byte) (getFlags(node) & ~VALUE_FLAG));
		} else {
			buffer.putInt(valueOffset(node), value);
			setFlags(node, (byte)(getFlags(node) | VALUE_FLAG));
		}
	}
	
	public void setLeft(final int node, final Integer left) {
		checkOrder(left, getChild(node), getRight(node));
		if (left == null) {
			setFlags(node, (byte)(getFlags(node) & ~LEFT_FLAG));			
		} else {
			setFlags(node, (byte)(getFlags(node) | LEFT_FLAG));
			buffer.putInt(leftOffset(node), left);
		}
	}
	
	public void setRight(final int node, final Integer right) {
		checkOrder(getLeft(node), getChild(node), right);
		if (right == null) {
			setFlags(node, (byte)(getFlags(node) & ~RIGHT_FLAG));			
		} else {
			setFlags(node, (byte)(getFlags(node) | RIGHT_FLAG));
			buffer.putInt(rightOffset(node), right);
		}
	}
	
	public void setChild(final int node, final Integer child) {
		checkOrder(getLeft(node), child, getRight(node));
		if (child == null) {
			setFlags(node, (byte)(getFlags(node) & ~CHILD_FLAG));
		} else {
			setFlags(node, (byte)(getFlags(node) | CHILD_FLAG));
			buffer.putInt(childOffset(node), child);
		}
	}
	
	public byte getFlags(final int node) {
		return buffer.get(flagsOffset(node));
	}
	
	public void setFlags(final int node, final byte flags) {
		buffer.put(flagsOffset(node), flags);
	}
	
	private static void checkOrder(final Integer left, final Integer child, final Integer right) {
		/*
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
		*/
	}

	public boolean hasValue(Integer node) {
		return (getFlags(node) & VALUE_FLAG) != 0;
	}

	public Integer getValue(Integer node) {
		return hasValue(node) ? buffer.getInt(valueOffset(node)) : null;
	}
	
	/**
	 * TODO: could this be faster if we used put(ByteBuffer)
	 * @param source
	 * @param destination
	 */
	public void moveNode(final int source, final int destination) {
		if (destination > source) {
			logger.error("cannot move from {} to {}", source, destination);
			throw new IllegalArgumentException();
		} else if (destination < source) {
			this.buffer.clear();
			final ByteBuffer sourceBuffer = this.buffer.slice();
			sourceBuffer.position(source);
			sourceBuffer.limit(nodeEnd(source));
			this.buffer.position(destination);
			this.buffer.put(sourceBuffer);
		} else {
			// noop
		}
	}

	private boolean hasOnlyOneChild(final int node) {
		if (hasChild(node)) {
			final int child = getChild(node);
			return !(hasRight(child) || hasLeft(child));
		} else {
			return false;
		}
	}

	public void compressInPlace(final int node) {
		this.buffer.clear();
		final ByteBuffer sourceKey = this.buffer.slice();

		// first we compress the path
		while (!hasValue(node) && hasOnlyOneChild(node)) {
			int child = getChild(node);
			
			this.setValue(node, this.getValue(child));
//			this.setLeft(node, this.getLeft(child)); DON'T DO THIS! THE LEFT AND RIGHT OF THE CHILD ARE NULL
//			this.setRight(node, this.getRight(child));
			this.setChild(node, this.getChild(child));

			sourceKey.limit(this.nodeEnd(child));
			sourceKey.position(this.keyOffset(child));
			
			this.appendKey(node, sourceKey);
		}		
		
		//  now we compress the node data itself
		// the candidates for compressionare:
		//    - value
		//    - left
		//    - right
		//    - child
		final Integer value = this.getValue(node);
		final Integer left = this.getLeft(node);
		final Integer right = this.getRight(node);
		final Integer child = this.getChild(node);
		
		// the key must be moved over as well
		this.buffer.clear();
		final ByteBuffer key = this.buffer.slice();
		key.position(this.keyOffset(node));
		key.limit(this.nodeEnd(node));
		
		// from now on any reads on this node are INVALID
		this.setIsCompressed(node);
		this.buffer.position(this.valueOffset(node));
		this.setValue(node, value);
//		this.setLeft(node, left);
//		this.setRight(node, right);
		this.setChild(node, child);

		this.setKeyLength(node, 0);
		this.appendKey(node, key);
	}

	public void limit(final int limit) {
		this.buffer.limit(limit);
	}
}
