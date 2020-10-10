/******************************************************************************

 Project     : CMP-5015A - Summative Assessment 1: Java Programming:
 JavaCardGame

 File        : BasicPlayer.java

 Date        : Wednesday 29 January 2020

 Author      : Martin Siddons

 Description : Implements the Player Interface, containing a Hand, Strategy and
 CardGame reference. See Player.java for more information.

 History     : 29/01/2020 - v1.0 - Initial setup, 
 ******************************************************************************/
package com.question2;

public class BasicPlayer implements Player {
    private Hand playerHand;
    private Strategy playerStrat;
    private CardGame currentGame;
    public final int playerNumber; // Easily recall player number in game.

    public BasicPlayer(Strategy s, CardGame g, int n) {
        this.playerHand = new Hand();
        this.playerStrat = s;
        this.currentGame = g;
        this.playerNumber = n + 1;
    }

    @Override
    public void addCard(Card c) {this.playerHand.addCard(c);}

    @Override
    public void addHand(Hand h) {this.playerHand.addHand(h);}

    @Override
    public int cardsLeft() {return this.playerHand.size();}

    @Override
    public void setGame(CardGame g) {this.currentGame = g;}

    @Override
    public void setStrategy(Strategy s) {this.playerStrat = s;}

    @Override
    public Bid playHand(Bid b) {
        boolean cheating = this.playerStrat.cheat(b, this.playerHand);
        Bid newBid = this.playerStrat.chooseBid(b, this.playerHand, cheating);

        // Iterate through player's hand and remove the cards being bid from it.
        for (int i = 0; i < newBid.getHand().size(); i++) {
            for (int j = 0; j < this.playerHand.size(); j++) {
                if (this.playerHand.getPos(j).getRank() ==
                        newBid.getHand().getPos(i).getRank()) {
                    if (this.playerHand.getPos(j).getSuit() ==
                            newBid.getHand().getPos(i).getSuit()) {
                        this.playerHand.removeCard(this.playerHand.getPos(j));
                        break;
                    }
                }
            }
        }
        System.out.println("Player " + this.playerNumber + "'s bid is: " +
                newBid);
        return newBid;
    }

    @Override
    public boolean callCheat(Bid currentBid) {
        return this.playerStrat.callCheat(this.playerHand, currentBid);
    }

    public static void main(String[] args) {
        // BasicPlayer()
        Strategy strategy = new BasicStrategy();
        CardGame game = new BasicCheat();
        Player player = new BasicPlayer(strategy, game, 0);
        System.out.println("Created BasicPlayer \"player\"");

        // addCard()
        Card c1 = new Card(Card.Rank.KING, Card.Suit.SPADES);
        player.addCard(c1);
        System.out.println("Card c1 added to player's hand.");

        // addHand()
        Card c2 = new Card(Card.Rank.FOUR, Card.Suit.DIAMONDS);
        Hand h1 = new Hand();
        h1.addCard(c2);
        player.addHand(h1);
        System.out.println("Hand h1 added to player's hand.");

        // cardsLeft()
        System.out.println("Player has " + player.cardsLeft() + " cards left.");

        // setGame()
        CardGame game2 = new BasicCheat();
        player.setGame(game2);
        System.out.println("Player assigned to game2.");

        // setStrategy()
        Strategy strategy2 = new BasicStrategy();
        player.setStrategy(strategy2);
        System.out.println("Player assigned to strategy2.");

        // playHand()
        Bid bid = new Bid(h1, Card.Rank.QUEEN);
        bid = player.playHand(bid);
        System.out.println("When the last bid was a Queen, player's next " +
                "bid is: " + bid.getHand());

        player.addCard(c1);
        Bid bid2 = new Bid(h1, Card.Rank.TWO);
        bid2 = player.playHand(bid2); // can be random
        System.out.println("When the last bid was a Two, player's next " +
                "bid is: " + bid2.getHand());

        player.addHand(h1);
        Bid bid3 = new Bid(h1, Card.Rank.FOUR);
        bid3 = player.playHand(bid3);
        System.out.println("When the last bid was a Four, player's next " +
                "bid is: " + bid3.getHand());

        // callCheat()
        Card c3 = new Card(Card.Rank.TWO, Card.Suit.DIAMONDS);
        Card c4 = new Card(Card.Rank.TWO, Card.Suit.HEARTS);
        Card c5 = new Card(Card.Rank.TWO, Card.Suit.SPADES);
        Card c6 = new Card(Card.Rank.TWO, Card.Suit.CLUBS);
        player.addCard(c3);
        player.addCard(c4);
        player.addCard(c5);
        player.addCard(c6);
        Bid bid4 = new Bid(h1, Card.Rank.TWO);
        System.out.println("When player isn't certain the bid is a cheat, " +
                "it will call: " + player.callCheat(bid));
        System.out.println("When player is certain the bid is a cheat, " +
                "it will call: " + player.callCheat(bid4));
    }
}
