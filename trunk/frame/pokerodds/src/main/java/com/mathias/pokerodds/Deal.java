package com.mathias.pokerodds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deal {

	public static List<Card> deal(List<Card> deck, int n) {
		int deckSize = deck.size();
		List<Card> handView = deck.subList(deckSize - n, deckSize);
		ArrayList<Card> hand = new ArrayList<Card>(handView);
		handView.clear();
		return hand;
	}
	
	public static int outs(){
		return 0;
	}
	
	public static boolean winner(){
		return false;
	}
	
	public static List<Card> best(List<Card> hand){
		return null;
	}

	public static int best(List<PokerHand> hand){
		return 0;
	}

	public static void main(String args[]) {
		List<Card> deck = Card.newDeck();
		Collections.shuffle(deck);
		List<Card> hand1 = deal(deck, 2);
//		List<Card> hand2 = deal(deck, 2);
		List<Card> pub = deal(deck, 3);
		pub.addAll(deal(deck, 1));
		pub.addAll(deal(deck, 1));

		PokerHand ph = new PokerHand(hand1);
		ph.add(pub);
		
//		System.out.println(hand1);
//		System.out.println(hand2);
//		System.out.println(pub);
		System.out.println(ph);
		
		System.out.println(ph.getValue().name());
	}

}
