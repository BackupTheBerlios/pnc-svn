package com.mathias.pokerodds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mathias.pokerodds.Card.Rank;
import com.mathias.pokerodds.Card.Suit;

public class PokerHand {
	
	public enum PokerHands {
		StraightFlush(16),
		FourOfAKind(8),
		FullHouse(6),
		Flush(5),
		Straight(4),
		ThreeOfAKind(3),
		TwoPair(2),
		Pair(1),
		HighCard(0);
		
		public int value;
		
		private PokerHands(int value){
			this.value = value;
		}
	}

	private List<Card> hand;

	public PokerHand(Card ... hand){		
		this.hand = Arrays.asList(hand);
	}

	public PokerHand(List<Card> hand){
		this.hand = hand;
	}

	public List<Card> getHand() {
		return Collections.unmodifiableList(hand);
	}

	public void add(List<Card> cards){
		hand.addAll(cards);
	}

	public void add(Card card){
		hand.add(card);
	}

	public PokerHands getValue(){
		if(isStraightFlush()){
			return PokerHands.StraightFlush;
		}else if(isFourOfAKind()){
			return PokerHands.FourOfAKind;
		}else if(isFullHouse()){
			return PokerHands.FullHouse;
		}else if(isFlush()){
			return PokerHands.Flush;
		}else if(isStraight()){
			return PokerHands.Straight;
		}else if(isThreeOfAKind()){
			return PokerHands.ThreeOfAKind;
		}else if(isTwoPair()){
			return PokerHands.TwoPair;
		}else if(isPair()){
			return PokerHands.Pair;
		}
		return PokerHands.HighCard;
	}

	/**
	 * Is hand straight flush
	 * @return
	 */
	private boolean isStraightFlush(){
		// check straight
		if(!isStraight()){
			return false;
		}
		// check flush
		return isFlush();
	}

	/**
	 * Is hand four of a kind
	 * @return
	 */
	private boolean isFourOfAKind(){
		return getMaxRank().size() == 4;
	}
	
	/**
	 * Is hand full house
	 * @return
	 */
	private boolean isFullHouse(){
		List<Card> maxRank = getMaxRank();
		if(maxRank.size() != 3){
			return false;
		}
		List<Card> copy = new ArrayList<Card>(hand);
		copy.removeAll(maxRank);
		return new PokerHand(copy).isPair();
	}

	/**
	 * Is hand flush
	 * @return
	 */
	private boolean isFlush(){
		return getMaxSuit().size() >= 5;
	}

	/**
	 * Is hand straight
	 * @return
	 */
	private boolean isStraight(){
		Collections.sort(hand, Card.byRank);
		int count = 0;
		Rank last = null;
		for (Card card : hand) {
			if(last != null){
				if(last.ordinal()+1 == card.rank().ordinal()){
					count++;
				}else{
					count = 0;
				}
			}
			last = card.rank();
		}
		return count >= 4;
	}

	/**
	 * Is hand three of a kind
	 * @return
	 */
	private boolean isThreeOfAKind(){
		return getMaxRank().size() == 3;
	}

	/**
	 * Is hand two pair
	 * @return
	 */
	private boolean isTwoPair(){
		List<Card> maxRank = getMaxRank();
		if(maxRank.size() != 2){
			return false;
		}
		List<Card> copy = new ArrayList<Card>(hand);
		copy.removeAll(maxRank);
		return new PokerHand(copy).isPair();
	}

	/**
	 * Is hand pair
	 * @return
	 */
	private boolean isPair(){
		return getMaxRank().size() == 2;
	}

	/**
	 * Get number of cards of most common suit
	 * @return
	 */
	private List<Card> getMaxSuit(){
		List<Card> ret = new ArrayList<Card>();
		for(Suit suit : Suit.values()){
			List<Card> count = getMax(suit);
			if(count.size() > ret.size()){
				ret = count;
			}
		}
		return ret;
	}

	/**
	 * Get number of cards of most common rank
	 * @return
	 */
	private List<Card> getMaxRank(){
		List<Card> ret = new ArrayList<Card>();
		for(Rank rank : Rank.values()){
			List<Card> count = getMax(rank);
			if(count.size() > ret.size()){
				ret = count;
			}
		}
		return ret;
	}

	/**
	 * Get number of cards of rank
	 * @return
	 */
	private List<Card> getMax(Rank rank){
		List<Card> cards = new ArrayList<Card>();
		for (Card card : hand) {
			if(card.rank() == rank){
				cards.add(card);
			}
		}
		return new ArrayList<Card>(cards);
	}

	/**
	 * Get number of cards of suit
	 * @return
	 */
	private List<Card> getMax(Suit suit){
		List<Card> cards = new ArrayList<Card>();
		for (Card card : hand) {
			if(card.suit() == suit){
				cards.add(card);
			}
		}
		return new ArrayList<Card>(cards);
	}

	/**
	 * Get highest rank
	 * @return
	 */
	private Rank getHighRank(){
		Rank ret = null;
		for (Card card : hand) {
			if(ret == null || card.rank().ordinal() > ret.ordinal()){
				ret = card.rank();
			}
		}
		return ret;
	}

	/**
	 * Get lowest rank
	 * @return
	 */
	private Rank getLowRank(){
		Rank ret = null;
		for (Card card : hand) {
			if(ret == null || card.rank().ordinal() < ret.ordinal()){
				ret = card.rank();
			}
		}
		return ret;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getSimpleName()+"\n");
		for (Card card : hand) {
			sb.append(card+"\n");
		}
		return sb.toString();
	}

}
