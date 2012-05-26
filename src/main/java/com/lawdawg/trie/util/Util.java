package com.lawdawg.trie.util;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Util {

	private static final Logger logger = LoggerFactory.getLogger(Util.class);
	
	public static void barf(final ByteBuffer bb, final OutputStream out) throws IOException {
		final DataOutputStream dout = new DataOutputStream(out);
		final int remaining = bb.remaining();
		dout.writeInt(remaining);
		dout.write(bb.array(), bb.position(), bb.limit());
	}
	
	public static byte[] slurp(final InputStream in) throws IOException {
		final DataInputStream din = new DataInputStream(new BufferedInputStream(in));
		final int n = din.readInt();
		logger.info("reading {} bytes", n);
		final byte[] data = new byte[n];
		for (int i = 0; i < n; i++) {
			data[i] = din.readByte();
		}
		return data;
	}
}
