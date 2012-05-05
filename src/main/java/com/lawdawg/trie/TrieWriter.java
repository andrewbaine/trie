package com.lawdawg.trie;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class TrieWriter {

	public static void writeTrie(final Trie t, final OutputStream out) throws IOException {
		final int length = t.dataLength();
		out.write(length);
		for (int i = 0; i < length; i++) {
			out.write(t.dataAt(i));
		}
	}
	
	public static Trie readTrie(final Trie t, final InputStream in) throws IOException {
		final Scanner scanner = new Scanner(in);
		final int length = scanner.nextInt();
		final int[] data = new int[length];
		for (int i = 0; i < length; i++) {
			data[i] = scanner.nextInt();
		}
		if (scanner.hasNext()) {
			throw new IOException();
		}
		return new Trie(data);
	}
}
