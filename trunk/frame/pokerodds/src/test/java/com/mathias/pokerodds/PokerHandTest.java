package com.mathias.pokerodds;

import java.lang.reflect.Constructor;

import junit.framework.TestCase;

import com.mathias.pokerodds.Card.Rank;
import com.mathias.pokerodds.Card.Suit;

public class PokerHandTest extends TestCase {
	
	private Constructor<Card> cardCons = null;

	private Card getCard(Rank rank, Suit suit) throws Exception {
		if(cardCons == null){
			cardCons = Card.class.getDeclaredConstructor(Rank.class,
					Suit.class);
			cardCons.setAccessible(true);
		}
		return cardCons.newInstance(rank, suit);
	}

	/**
	 * Test straight flush
	 * @throws Exception
	 */
	public void testStraightFlush() throws Exception {
		Card c1 = getCard(Rank.ACE, Suit.HEARTS);
		Card c2 = getCard(Rank.TEN, Suit.HEARTS);
		Card c3 = getCard(Rank.QUEEN, Suit.HEARTS);
		Card c4 = getCard(Rank.JACK, Suit.HEARTS);
		Card c5 = getCard(Rank.KING, Suit.HEARTS);
		PokerHand ph = new PokerHand(c1, c2, c3, c4, c5);
		assertEquals(PokerHand.PokerHands.StraightFlush, ph.getValue());
	}

	/**
	 * Test four of a kind
	 * @throws Exception
	 */
	public void testFourOfAKind() throws Exception {
		Card c1 = getCard(Rank.SEVEN, Suit.CLUBS);
		Card c2 = getCard(Rank.SIX, Suit.HEARTS);
		Card c3 = getCard(Rank.SEVEN, Suit.DIAMONDS);
		Card c4 = getCard(Rank.SEVEN, Suit.CLUBS);
		Card c5 = getCard(Rank.SEVEN, Suit.SPADES);
		PokerHand ph = new PokerHand(c1, c2, c3, c4, c5);
		assertEquals(PokerHand.PokerHands.FourOfAKind, ph.getValue());
	}

	/**
	 * Test flush
	 * @throws Exception
	 */
	public void testFlush() throws Exception {
		Card c1 = getCard(Rank.ACE, Suit.CLUBS);
		Card c2 = getCard(Rank.NINE, Suit.HEARTS);
		Card c3 = getCard(Rank.SEVEN, Suit.CLUBS);
		Card c4 = getCard(Rank.JACK, Suit.CLUBS);
		Card c5 = getCard(Rank.TEN, Suit.DIAMONDS);
		Card c6 = getCard(Rank.JACK, Suit.CLUBS);
		Card c7 = getCard(Rank.TEN, Suit.CLUBS);
		PokerHand ph = new PokerHand(c1, c2, c3, c4, c5, c6, c7);
		assertEquals(PokerHand.PokerHands.Flush, ph.getValue());

		c1 = getCard(Rank.ACE, Suit.CLUBS);
		c2 = getCard(Rank.NINE, Suit.CLUBS);
		c3 = getCard(Rank.SEVEN, Suit.CLUBS);
		c4 = getCard(Rank.JACK, Suit.CLUBS);
		c5 = getCard(Rank.TEN, Suit.CLUBS);
		ph = new PokerHand(c1, c2, c3, c4, c5);
		assertEquals(PokerHand.PokerHands.Flush, ph.getValue());

		c1 = getCard(Rank.ACE, Suit.CLUBS);
		c2 = getCard(Rank.NINE, Suit.HEARTS);
		c3 = getCard(Rank.SEVEN, Suit.CLUBS);
		c4 = getCard(Rank.JACK, Suit.CLUBS);
		c5 = getCard(Rank.TEN, Suit.DIAMONDS);
		c6 = getCard(Rank.JACK, Suit.CLUBS);
		c7 = getCard(Rank.TEN, Suit.SPADES);
		ph = new PokerHand(c1, c2, c3, c4, c5, c6, c7);
		assertNotSame(PokerHand.PokerHands.Flush, ph.getValue());
	}

	/**
	 * Test straight
	 * @throws Exception
	 */
	public void testStraight() throws Exception {
		Card c1 = getCard(Rank.NINE, Suit.CLUBS);
		Card c2 = getCard(Rank.TEN, Suit.HEARTS);
		Card c3 = getCard(Rank.QUEEN, Suit.SPADES);
		Card c4 = getCard(Rank.JACK, Suit.DIAMONDS);
		Card c5 = getCard(Rank.KING, Suit.CLUBS);
		Card c6 = getCard(Rank.SEVEN, Suit.DIAMONDS);
		Card c7 = getCard(Rank.DEUCE, Suit.CLUBS);
		PokerHand ph = new PokerHand(c1, c2, c3, c4, c5, c6, c7);
		assertEquals(PokerHand.PokerHands.Straight, ph.getValue());

		c1 = getCard(Rank.NINE, Suit.CLUBS);
		c2 = getCard(Rank.TEN, Suit.HEARTS);
		c3 = getCard(Rank.QUEEN, Suit.SPADES);
		c4 = getCard(Rank.JACK, Suit.DIAMONDS);
		c5 = getCard(Rank.KING, Suit.CLUBS);
		ph = new PokerHand(c1, c2, c3, c4, c5);
		assertEquals(PokerHand.PokerHands.Straight, ph.getValue());

		c1 = getCard(Rank.NINE, Suit.CLUBS);
		c2 = getCard(Rank.TEN, Suit.HEARTS);
		c3 = getCard(Rank.QUEEN, Suit.SPADES);
		c4 = getCard(Rank.JACK, Suit.DIAMONDS);
		c5 = getCard(Rank.FIVE, Suit.CLUBS);
		ph = new PokerHand(c1, c2, c3, c4, c5);
		assertNotSame(PokerHand.PokerHands.Straight, ph.getValue());
	}

	/**
	 * Test three of a kind
	 * @throws Exception
	 */
	public void testThreeOfAKind() throws Exception {
		Card c1 = getCard(Rank.ACE, Suit.CLUBS);
		Card c2 = getCard(Rank.ACE, Suit.HEARTS);
		Card c3 = getCard(Rank.ACE, Suit.SPADES);
		Card c4 = getCard(Rank.JACK, Suit.CLUBS);
		Card c5 = getCard(Rank.TEN, Suit.CLUBS);
		PokerHand ph = new PokerHand(c1, c2, c3, c4, c5);
		assertEquals(PokerHand.PokerHands.ThreeOfAKind, ph.getValue());
	}

	/**
	 * Test two pair
	 * @throws Exception
	 */
	public void testTwoPair() throws Exception {
		Card c1 = getCard(Rank.ACE, Suit.CLUBS);
		Card c2 = getCard(Rank.ACE, Suit.HEARTS);
		Card c3 = getCard(Rank.QUEEN, Suit.CLUBS);
		Card c4 = getCard(Rank.QUEEN, Suit.DIAMONDS);
		Card c5 = getCard(Rank.TEN, Suit.SPADES);
		PokerHand ph = new PokerHand(c1, c2, c3, c4, c5);
		assertEquals(PokerHand.PokerHands.TwoPair, ph.getValue());
	}

	/**
	 * Test pair
	 * @throws Exception
	 */
	public void testPair() throws Exception {
		Card c1 = getCard(Rank.ACE, Suit.CLUBS);
		Card c2 = getCard(Rank.ACE, Suit.HEARTS);
		Card c3 = getCard(Rank.QUEEN, Suit.CLUBS);
		Card c4 = getCard(Rank.JACK, Suit.CLUBS);
		Card c5 = getCard(Rank.TEN, Suit.CLUBS);
		PokerHand ph = new PokerHand(c1, c2, c3, c4, c5);
		assertEquals(PokerHand.PokerHands.Pair, ph.getValue());
	}

}
