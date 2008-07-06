package com.mathias.pokerodds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mathias.pokerodds.Card.Rank;
import com.mathias.pokerodds.Card.Suit;

public class PokerHand implements Comparable<PokerHand> {

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

	public PokerHand(List<Card> ... hand){
		this.hand = new ArrayList<Card>();
		for (List<Card> h : hand) {
			this.hand.addAll(h);
		}
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
		return getMaxRank(hand).size() == 4;
	}
	
	/**
	 * Is hand full house
	 * @return
	 */
	private boolean isFullHouse(){
		List<Card> maxRank = getMaxRank(hand);
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
		return getMaxSuit(hand).size() >= 5;
	}

	/**
	 * Is hand straight
	 * @return
	 */
	private boolean isStraight(){
		return getMaxStraight(hand).size() >= 5;
	}

	/**
	 * Is hand three of a kind
	 * @return
	 */
	private boolean isThreeOfAKind(){
		return getMaxRank(hand).size() == 3;
	}

	/**
	 * Is hand two pair
	 * @return
	 */
	private boolean isTwoPair(){
		List<Card> maxRank = getMaxRank(hand);
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
		return getMaxRank(hand).size() == 2;
	}

	/**
	 * Get number of cards of most common suit
	 * @return
	 */
	private static List<Card> getMaxSuit(List<Card> hand){
		List<Card> ret = new ArrayList<Card>();
		for(Suit suit : Suit.values()){
			List<Card> count = getMax(suit, hand);
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
	private static List<Card> getMaxRank(List<Card> hand){
		Collections.sort(hand, Card.byRank);
		List<Card> ret = new ArrayList<Card>();
		for(Rank rank : Rank.values()){
			List<Card> count = getMax(rank, hand);
			if(count.size() >= ret.size()){
				ret = count;
			}
		}
		return ret;
	}

	/**
	 * Get most straigt cards
	 * @return
	 */
	private static List<Card> getMaxStraight(List<Card> hand){
		Collections.sort(hand, Card.byRank);
		List<Card> ret = new ArrayList<Card>();
		List<Card> cur = new ArrayList<Card>();
		Rank last = null;
		for (Card card : hand) {
			if(last == null){
				cur.add(card);
			}else{
				if(last.ordinal()+1 == card.rank().ordinal()){
					cur.add(card);
				}else{
					if(cur.size() >= ret.size()){
						ret = cur;
					}
					cur = new ArrayList<Card>();
					cur.add(card);
				}
			}
			last = card.rank();
		}
		if(cur.size() >= ret.size()){
			ret = cur;
		}
		return new ArrayList<Card>(ret);
	}

	/**
	 * Get number of cards of rank
	 * @return
	 */
	private static List<Card> getMax(Rank rank, List<Card> hand){
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
	private static List<Card> getMax(Suit suit, List<Card> hand){
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
	private static Rank getHighRank(List<Card> hand){
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
	private static Rank getLowRank(List<Card> hand){
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

	@Override
	public int compareTo(PokerHand other) {
		PokerHands v1 = getValue();
		PokerHands v2 = other.getValue();
		if(v1.value == v2.value){
			if(v1 == PokerHands.StraightFlush || v1  == PokerHands.StraightFlush){
				return getHighRank(getMaxStraight(getMaxSuit(hand))).compareTo(
								getHighRank(getMaxStraight(getMaxSuit(other.hand))));
			}else if(v1 == PokerHands.FourOfAKind){
				return getHighRank(getMaxRank(hand)).compareTo(
						getHighRank(getMaxRank(other.hand)));
			}else if(v1 == PokerHands.FullHouse){
				List<Card> maxRank = getMaxRank(hand);
				List<Card> otherMaxRank = getMaxRank(other.hand);
				int cmp = getHighRank(maxRank).compareTo(
						getHighRank(otherMaxRank));
				if(cmp != 0){
					return cmp;
				}
				List<Card> copy = new ArrayList<Card>(hand);
				copy.removeAll(maxRank);
				List<Card> otherCopy = new ArrayList<Card>(other.hand);
				otherCopy.removeAll(otherMaxRank);
				return getHighRank(getMaxRank(copy)).compareTo(
						getHighRank(getMaxRank(otherCopy)));
			}else if(v1 == PokerHands.Flush){
				return getHighRank(getMaxSuit(hand)).compareTo(
						getHighRank(getMaxSuit(other.hand)));
			}else if(v1 == PokerHands.Straight){
				return getHighRank(getMaxStraight(hand)).compareTo(
						getHighRank(getMaxStraight(other.hand)));
			}else{
				List<Card> copy = new ArrayList<Card>(hand);
				List<Card> otherCopy = new ArrayList<Card>(other.hand);
				do{
					List<Card> maxRank = getMaxRank(copy);
					List<Card> otherMaxRank = getMaxRank(otherCopy);
					int cmp = getHighRank(maxRank).compareTo(
							getHighRank(otherMaxRank));
					if(cmp != 0){
						return cmp;
					}
					copy = new ArrayList<Card>(copy);
					copy.removeAll(maxRank);
					otherCopy = new ArrayList<Card>(otherCopy);
					otherCopy.removeAll(otherMaxRank);
				}while(copy.size() > 0);
				return 0;
			}
		}else{
			return new Integer(getValue().value).compareTo(other.getValue().value);
		}
	}

}
