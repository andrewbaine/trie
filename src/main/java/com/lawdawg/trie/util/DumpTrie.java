package com.lawdawg.trie.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import com.lawdawg.trie.RawTrieReader;

public class DumpTrie {

	public static void main(final String[] args) throws FileNotFoundException, IOException {

		final byte[] bytes = Util.slurp(new FileInputStream(args[0]));

		final RawTrieReader reader = new RawTrieReader(ByteBuffer.wrap(bytes));

		final Scanner scanner = new Scanner(System.in);

		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			System.out.println();
			System.out.format("%s\t%d\n", line, reader.get(ByteBuffer.wrap(line.getBytes())));
		}
	}

}
