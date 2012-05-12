package com.lawdawg.trie;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import junit.framework.TestCase;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieTest extends TestCase {
	
	private static final String WORDS = "words";
	private static final Logger logger = LoggerFactory.getLogger(TrieTest.class);

	private final Map<String, Character> map = Collections.unmodifiableMap(map());
	private final ByteBuffer trie = rawTrie();

	private static final Character computeValue(final String key) {
		return Character.toUpperCase(key.charAt(0));
	}

	private Map<String, Character> map() {
		final Map<String, Character> map = new HashMap<String, Character>();
		
		final InputStream in = this.getClass().getResourceAsStream(WORDS);
		final Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			final String key = scanner.nextLine().toLowerCase();
			final Character value = computeValue(key);
			map.put(key, value);
		}
		scanner.close();
		return map;
	}

	private ByteBuffer rawTrie() {
		final TrieBuilder tb = new TrieBuilder();
		
		final InputStream in = this.getClass().getResourceAsStream(WORDS);
		final Scanner scanner = new Scanner(in);
		while (scanner.hasNextLine()) {
			final String key = scanner.nextLine().toLowerCase();
			final Character value = computeValue(key);
//			logger.info("putting {} -> {}", key, value);
			tb.put(key, (byte)(char)value);
		}
		tb.cleanup();
		return tb.getData();
	}
	
	@Test
	public void testTrie() {
		logger.info("begin: {}", System.currentTimeMillis());
		final RawTrieReader reader = new RawTrieReader(trie);
		for (Map.Entry<String, Character> e : map.entrySet()) {
			final String key = e.getKey();
			final Character expectedValue = e.getValue();
			final Character actualValue = reader.get(key);
//			logger.info("testing that {} -> {}", key, expectedValue);
			assertEquals(expectedValue, actualValue);
		}
		logger.info("end: {}", System.currentTimeMillis());
	}
}
