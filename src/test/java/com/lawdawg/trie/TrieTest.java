package com.lawdawg.trie;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieTest extends TestCase {
	
	private static final Logger logger = LoggerFactory.getLogger(TrieTest.class);

	private final Map<String, Integer> map = Collections.unmodifiableMap(map());
	private final Trie trie = trie();

	@Test
	public void testSize() {
		logger.info("begin testing size");
		assertEquals(235886, map.size());
		assertEquals(235886, trie.size());
		logger.info("finished testing size");
	}
	
	@Test
	public void testSerialize() throws FileNotFoundException, IOException {
		final OutputStream out = new FileOutputStream("src/test/resources/trie.dat");
		TrieWriter.writeTrie(trie, out);
		out.close();
	}

	private Map<String, Integer> map() {
		final Map<String, Integer> map = new HashMap<String, Integer>();
		final InputStream in = this.getClass().getResourceAsStream("words.txt");
		final Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			final String line = scanner.nextLine();
			final int x = line.lastIndexOf("\t");
			final String key = line.substring(0, x);
			final Integer value = Integer.parseInt(line.substring(x + 1));
			map.put(key, value);
		}
		scanner.close();
		return map;
	}
	
	private Trie trie() {
		final InputStream in = this.getClass().getResourceAsStream("words.txt");
		final Trie trie = new TrieBuilder().buildTrie(in);
		try {
			in.close();
		} catch (IOException e) {
			fail("couldn't close words.txt");
		}
		return trie;
	}

}
