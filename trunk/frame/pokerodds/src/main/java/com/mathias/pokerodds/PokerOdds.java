package com.mathias.pokerodds;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JPanel;

import com.mathias.pokerodds.PokerHand.PokerHands;

public class PokerOdds extends JApplet {

	private Map<Integer, Image> imgs;

	private List<Card> deck = null;
	private List<Card> hand1 = null;
	private List<Card> hand2 = null;
	private List<Card> pub = null;
	
	private int btncounter = 0;

	public void init(){

		imgs = new HashMap<Integer, Image>();
		for (Card card : Card.newDeck()) {
			String id = card.getIdAsString();
			imgs.put(Integer.parseInt(id), getToolkit().getImage(
					getClass().getResource("images/" + id + ".png")));
		}

		MediaTracker mt = new MediaTracker(this);
		for (Entry<Integer, Image> e : imgs.entrySet()) {
			mt.addImage(e.getValue(), e.getKey());
		}
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		JButton button = new JButton(new AbstractAction("Click me"){
			@Override
			public void actionPerformed(ActionEvent e) {
				switch(btncounter){
				case 0:
					deck = Card.newDeck();
					Collections.shuffle(deck);
					hand1 = Card.deal(deck, 2);
					hand2 = Card.deal(deck, 2);
					pub = Card.deal(deck, 3);
					btncounter++;
					break;
				case 1:
					pub.addAll(Card.deal(deck, 1));
					btncounter++;
					break;
				case 2:
					pub.addAll(Card.deal(deck, 1));
					btncounter = 0;
					break;
				default:
					System.out.println("ERROR");
					btncounter = 0;
				}

				repaint();
			}
		});
		getContentPane().add(button);
		
		JPanel panel = new JPanel(){
//			@Override
//			public void paint(Graphics g) {
//				System.out.println("paint panel");
//				super.paint(g);
//			}
//			@Override
//			protected void paintComponent(Graphics g) {
//				System.out.println("paintComponent panel");
//				super.paintComponent(g);
//			}
		};
		panel.setLayout(null);
		panel.setSize(900, 400);
		getContentPane().add(panel);

		setLayout(new FlowLayout());
		setLocation(100, 100);
		setSize(900, 600);
		setVisible(true);
	}

	private void paintCards(Graphics g) {
		int wcount = 0;
		int cwidth = 80;
		int top = 50;
		int top2 = 70;

		g.clearRect(0, top, getWidth(), getHeight());

		if(hand1 != null && hand1.size() == 2){
			g.drawImage(imgs.get(hand1.get(0).getId()), wcount, top, null);
			g.drawImage(imgs.get(hand1.get(1).getId()), wcount+=cwidth, top, null);
		}

		if(pub != null){
			for (Card card : pub) {
				g.drawImage(imgs.get(card.getId()), wcount+=cwidth+20, top2, null);
			}
		}

		if(hand2 != null && hand2.size() == 2){
			g.drawImage(imgs.get(hand2.get(0).getId()), wcount+=cwidth+20, top, null);
			g.drawImage(imgs.get(hand2.get(1).getId()), wcount+=cwidth, top, null);
		}

		if(hand1 != null && hand2 != null && pub != null){
			PokerHand ph1 = new PokerHand(hand1, pub);
			PokerHands val1 = ph1.getValue();
			g.drawString(val1.name(), 0, 200);

			PokerHand ph2 = new PokerHand(hand2, pub);
			PokerHands val2 = ph2.getValue();
			g.drawString(val2.name(), 640, 200);

			//winner
			String winner;
			int cmp = ph1.compareTo(ph2);
			if(cmp > 0){
				winner = "<<==";
			}else if(cmp < 0){
				winner = "==>>";
			}else{
				winner = "split pot";
			}
			g.drawString(winner, 350, 200);
		}
	}

	@Override
	public void paint(Graphics g) {
		System.out.println("paint");
		super.paint(g);
		paintCards(g);
	}
	
	@Override
	public void update(Graphics g) {
		System.out.println("update");
		super.update(g);
		paintCards(g);
	}
	
	@Override
	public void repaint() {
		System.out.println("repaint");
		super.repaint();
	}

	public static void main(String[] args) {
		new PokerOdds();
	}

}
