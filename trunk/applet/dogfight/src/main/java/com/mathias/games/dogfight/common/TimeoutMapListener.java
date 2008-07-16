package com.mathias.games.dogfight.common;

public interface TimeoutMapListener<K, V> {

	void handleTimeout(K key, V value);

}
