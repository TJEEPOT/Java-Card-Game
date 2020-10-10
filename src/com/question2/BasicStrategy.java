package com.question2;

import java.util.Random;

public class BasicStrategy implements Strategy {
    public BasicStrategy() {}

    /*  Check if the player's hand contains cards of either the current bid rank
     *  or the one above. If it does, return false and if not, return true. */
    @Override
    public boolean cheat(Bid previousBid, Hand playerHand) {
        Card.Rank nextRank = previousBid.getRank().getNext();

        // Check for a card of the given rank or the next rank up and decide
        // not to cheat if there's a match.
        for (Card c : playerHand) {
            if (c.getRank() == previousBid.getRank()) {return false;}
            else if (c.getRank() == nextRank) {return false;}
        }
        return true; // if there's no matching rank, the player must cheat
    }

    /* If we have decided not to cheat, check for any card matching the previous
     * bid in playerHand and if that exists, play it. If not, retrieve
     * every card from playerHand in the next bid rank up and play that.
     * If we are cheating, select a random card, choose the next rank up and
     * return that card and rank.
     */
    @Override
    public Bid chooseBid(Bid previousBid, Hand playerHand, boolean cheat) {
        Hand bidHand = new Hand();
        Card.Rank bidRank = previousBid.getRank();
        Card.Rank nextBidRank = bidRank.getNext();

        // Strategy for if we're not cheating.
        if (! cheat) {
            for (Card c : playerHand) {
                if (c.getRank() == bidRank) {
                    bidHand.addCard(c);
                }
            }
            // If there were no cards added to the bidHand in the above loop,
            // use nextBidRank and add cards of that rank to bidHand instead.
            if (bidHand.handValue() == 0) {
                for (Card c : playerHand) {
                    if (c.getRank() == nextBidRank) {
                        bidHand.addCard(c);
                    }
                }
                bidRank = nextBidRank;
            }
        }

        // If we have to cheat, select a random card in the hand and return it.
        else {
            Random rnd = new Random();
            int random = rnd.nextInt(playerHand.size());
            bidHand.addCard(playerHand.getPos(random));
            // In case the previous bid was four cards, it's safer to call the cheat
            // as being of the next rank up.
            bidRank = nextBidRank;
        }
        return new Bid(bidHand, bidRank);
    }

    /* Simply checks if the current bid is posting a card that we already hold
     * four of, and if so, calls cheat.
     */
    @Override
    public boolean callCheat(Hand playerHand, Bid currentBid) {
        Card.Rank bidRank = currentBid.getRank();
        int cardCounter = 0; // Counter to track how many cards of the above
        // rank are in the player's hand.

        for (Card c : playerHand) {
            if (c.getRank() == bidRank) {cardCounter++;}
        }
        if (cardCounter == 4){
            System.out.println("He's like, totally cheating cos I've got " +
                    "like, those cards and stuff." );
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        // cheat()
        Strategy strat1 = new BasicStrategy();
        // fill cardList with random cards
        int handSize = 5;
        Card[] cardList = new Card[handSize];
        for (int i = 0; i < handSize; i++) {
            Card.Rank randRank = Card.Rank.getRandom();
            Card.Suit randSuit = Card.Suit.getRandom();
            cardList[i] = new Card(randRank, randSuit);
        }

        Hand hand1 = new Hand(cardList);
        Bid bid1 = new Bid(hand1, Card.Rank.JACK);
        System.out.println("bid: JACK. hand1:\n" + hand1);
        boolean cheat = strat1.cheat(bid1, hand1);
        System.out.println("Should the player cheat?: " + cheat);

        // chooseBid()
        Bid bid2 = strat1.chooseBid(bid1, hand1, cheat);
        System.out.println("\nTherefore the bid for this hand is: " +
                bid2);

        // callCheat()
        cheat = strat1.callCheat(hand1, bid1);
        System.out.println("\nIf this player was given that bid, " +
                "they would call: " + cheat);
    }
}
