package org.dominoserver;

public class MyPair<K, V> {
	
	private K mKey = null;
	private V mValue = null;
	
	public MyPair(K key, V value) {
		
		mKey = key;
		mValue = value;
	}
	
	public K getKey() {
		
		return mKey;
	}
	
	public V getValue() {
		
		return mValue;
	}

}
