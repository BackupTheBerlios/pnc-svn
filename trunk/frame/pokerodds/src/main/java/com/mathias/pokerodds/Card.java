package com.mathias.pokerodds;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Card {
	public enum Rank {
		DEUCE, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE
	}

	public enum Suit {
		CLUBS, DIAMONDS, HEARTS, SPADES
	}

	private final Rank rank;
	private final Suit suit;

	private Card(Rank rank, Suit suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public Rank rank() {
		return rank;
	}

	public Suit suit() {
		return suit;
	}

	public String toString() {
		return rank + " of " + suit;
	}

	static Comparator<Card> byRank = new Comparator<Card>(){
		@Override
		public int compare(Card c1, Card c2) {
			return c1.rank.compareTo(c2.rank);
		}
	};

	static Comparator<Card> bySuit = new Comparator<Card>(){
		@Override
		public int compare(Card c1, Card c2) {
			return c1.suit.compareTo(c2.suit);
		}
	};

	private static final List<Card> protoDeck = new ArrayList<Card>();

	// Initialize prototype deck
	static {
		for (Suit suit : Suit.values()){
			for (Rank rank : Rank.values()){
				protoDeck.add(new Card(rank, suit));
			}
		}
	}

	public static List<Card> newDeck() {
		return new ArrayList<Card>(protoDeck); // Return copy of prototype deck
	}

	public static List<Card> deal(List<Card> deck, int n) {
		int deckSize = deck.size();
		List<Card> handView = deck.subList(deckSize - n, deckSize);
		ArrayList<Card> hand = new ArrayList<Card>(handView);
		handView.clear();
		return hand;
	}
	
	public int getId(){
		return Integer.parseInt(getIdAsString());
	}
	
	public String getIdAsString(){
		return String.format("%01d%02d", suit.ordinal(), rank.ordinal());
	}

}
