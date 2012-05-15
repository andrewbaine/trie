package com.lawdawg.trie;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieTest extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(TrieTest.class);

	private static final Character computeValue(final String key) {
		return Character.toUpperCase(key.charAt(key.length() - 1));
	}

	private Map<String, Character> map(final String filename) {
		final Map<String, Character> map = new TreeMap<String, Character>();
		
		final InputStream in = this.getClass().getResourceAsStream(filename);
		final Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			final String key = scanner.nextLine().toLowerCase();
			final Character value = computeValue(key);
			map.put(key, value);
		}
		scanner.close();
		return map;
	}

	private RawTrieReader rawTrie(final String filename) {
		final TrieBuilder tb = new TrieBuilder(1024 * 1024, 1024 * 1024);
			
		final InputStream in = this.getClass().getResourceAsStream(filename);
		final Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			final String key = scanner.nextLine().toLowerCase();
			final Character value = computeValue(key);
			//logger.info("putting {} -> {}", key, value);
			tb.put(ByteBuffer.wrap(key.getBytes()), value);
		}
		tb.cleanup();
		scanner.close();
		return tb.getReader();
	}

	@Test
	public void testTrie() {
		for (int i = 0; i < 9; i++) {
			test("words." + i + ".txt");
		}
	}
	
	@Test
	public void testBig() {
		test("words.full");
	}
	
	@Test
	public void testRandom() {
		for (int i = 0; i < 100; i++) {
			test("random/words." + i + ".random");
		}
	}

	public void test(final String filename) {
		logger.info("begin testing {}", filename);
		final RawTrieReader reader = rawTrie(filename);
		final Map<String, Character> map =  map(filename);
		boolean pass = true;
		for (Map.Entry<String, Character> e : map.entrySet()) {
			final String key = e.getKey();
			final Character expectedValue = e.getValue();
//			logger.info("testing that {} -> {}", key, expectedValue);

			final Integer value = reader.get(ByteBuffer.wrap(key.getBytes()));
			final Character c = value == null ? null : (char)(int)value;
			assertEquals(expectedValue, c);
		}
		assertTrue(pass);
		logger.info("finished testing {}", filename);
	}
}
