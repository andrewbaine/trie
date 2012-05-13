package com.lawdawg.trie;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueBuffer {

	private static final Logger logger = LoggerFactory.getLogger(ValueBuffer.class);
	private ByteBuffer buffer = ByteBuffer.allocate(1024);
	
	public int position() {
		return this.buffer.position();
	}
	
	public void put(final ByteBuffer value) {
		final int remaining = value.remaining();
		if (this.buffer.remaining() < remaining + 4) {
			logger.info("begin increasing capacity from {}", buffer.capacity());
			final int capacity = this.buffer.capacity();
			final int newCapacity = capacity + (remaining + 4 > capacity ? remaining + 4: capacity);
			final ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);

			this.buffer.flip();
			newBuffer.put(this.buffer);
			this.buffer = newBuffer;

			logger.info("finished increasing capacity to {}", buffer.capacity());
		}
		buffer.putInt(remaining);
		if (buffer.remaining() < value.remaining()) {
			System.out.println("wtf");
		}
		buffer.put(value);
	}

	public ByteBuffer get(final int index) {
		final int remaining = this.buffer.getInt(index);
		this.buffer.position(index + 4);
		this.buffer.limit(index + 4 + remaining);
		final ByteBuffer slice = this.buffer.slice();
		this.buffer.clear();
		return slice;
	}
}
