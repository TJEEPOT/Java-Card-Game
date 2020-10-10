/******************************************************************************

 Project     : CMP-5015Y - Java Programming:
                Java_Card_Game.

 File        : ThinkerStrategy.java

 Date        : Saturday 01 February 2020

 Author      : Martin Siddons

 Description : The ThinkerStrategy is an evolution of BasicStrategy whereby it
 makes more informed decisions on what choices to make. More
 information is available in the block comment above each method.

 History     : 01/02/2020 - v1.0 - Initial setup, 
 ******************************************************************************/
package com.question2;

import java.util.Random;

public class ThinkerStrategy implements Strategy {
    private Hand oldBids; // hand of all cards discarded by this player.

    public ThinkerStrategy() {this.oldBids = new Hand();}

    /*  Similar to the cheat() implementation in BasicStrategy, this will
     *  cheat when it has to, but every 1 in 4 plays it will decide to just
     *  cheat when it doesn't need to.
     */
    @Override
    public boolean cheat(Bid previousBid, Hand playerHand) {
        Card.Rank nextRank = previousBid.getRank().getNext();
        Random rand = new Random();
        boolean cheat = true;

        // Check for a card of the given rank or the next rank up and decide
        // not to cheat if there's a match.
        for (Card c : playerHand) {
            if (c.getRank() == previousBid.getRank()) {cheat = false;}
            else if (c.getRank() == nextRank) {cheat = false;}
        }
        if (cheat) {return true;} // No match means we must cheat.

        // Roll to decide whether to just go ahead and cheat regardless of hand.
        // Otherwise, don't cheat.
        return rand.nextInt(4) == 0;
    }

    /*  When not cheating, this strategy will play all of the cards it can
     *  75% of the time, but will otherwise play a random number of cards.
     *  When cheating, this strategy will throw a random number of cards each
     *  time, and the likelihood the card will be of a high rank is higher
     *  than for the low rank cards.
     */
    @Override
    public Bid chooseBid(Bid previousBid, Hand playerHand, boolean cheat) {
        Hand bidHand = new Hand(); // stores the final hand to pass
        Card.Rank bidRank = previousBid.getRank();
        Card.Rank nextBidRank = bidRank.getNext();
        Random rand = new Random();

        // Select cards where we're not cheating.
        if (! cheat) {
            Hand bidRankHand = new Hand(); // Hand of cards matching bidRank.
            Hand nextBidRankHand = new Hand(); // Cards matching nextBidRank.

            // Find all the bidRank cards.
            for (Card c : playerHand) {
                if (c.getRank() == bidRank) {
                    bidRankHand.addCard(c);
                } // and nextBidRank cards.
                else if (c.getRank() == nextBidRank) {
                    nextBidRankHand.addCard(c);
                }
            }

            // Select the larger of the two hands.
            if (bidRankHand.size() > nextBidRankHand.size()) {
                bidHand.addHand(bidRankHand);
            }
            else {
                bidHand.addHand(nextBidRankHand);
                bidRank = nextBidRank; // set rank for returning bid.
            }

            // For one in every 4 plays, remove a random number of cards from
            // the hand to be submitted, always leaving at least one card.
            if (rand.nextInt(4) == 0) {
                int removeCards = rand.nextInt((bidHand.size() - 1) + 1);
                while (removeCards > 0) {
                    bidHand.removePos(removeCards);
                    removeCards--;
                }
            }
        }

        // Card selection for when we've decided to cheat.
        else {
            // Sort the cards to ensure the higher-rank cards are towards the
            // end of the hand.
            playerHand.sortAscending();

            // Decide to play a number of cards between 1 and the lesser of 4
            // or the number of cards remaining in the player's hand.
            int playCards = rand.nextInt(Math.min(4, playerHand.size())) + 1;

            // Work out which cards to pick from the hand, weighting the later
            // cards more, by using an estimation of cumulative probability.
            while (playCards > 0) {
                int die1 = rand.nextInt(playerHand.size());
                int die2 = rand.nextInt(playerHand.size());
                int pickedCard = (die1 + die2) / 2; // Int ensures rounding up.
                bidHand.addCard(playerHand.getPos(pickedCard));
                playerHand.removePos(pickedCard); // Don't pick this card twice.
                playCards--;
            }

            // Since there's no real intelligent way of guessing what rank to
            // call (outside of actually cheating), I will randomise the choice.
            if (rand.nextInt(2) == 1) {
                bidRank = nextBidRank;
            }
        }

        // Add all the chosen cards to the internal discard pile for later
        // reference and return the found values as a bid.
        this.oldBids.addHand(bidHand);
        return new Bid(bidHand, bidRank);
    }

    /*  This strategy holds a record of all of the cards it has assigned to the
     *  discard pile so far and using that plus the known cards in hand it can
     *  work out how likely the other players are cheating.
     */
    @Override
    public boolean callCheat(Hand playerHand, Bid currentBid) {
        Random rand = new Random();
        // First iterate through the cards in the discard memory and check them
        // against the cards in currentBid and playerHand to see if there's any
        // matches. If there is, disregard our memory as this means cheat has
        // been called and those cards are either back in our hand or now in
        // another player's hand.
        // This might seem like cheating at the game at first, but in reality
        // someone would know cheat was called and be able to forget their bid
        // cards that way. As we can't receive that information, this is the
        // next best thing we can do. We won't be storing anything else about
        // the actual bid cards here.
        if (this.oldBids.size() != 0) {
            for (int i = 0; i < this.oldBids.size(); i++) {
                for (int j = 0; j < playerHand.size(); j++) {
                    if (this.oldBids.getPos(i).getRank() ==
                            playerHand.getPos(j).getRank()) {
                        if (this.oldBids.getPos(i).getSuit() ==
                                playerHand.getPos(j).getSuit()) {
                            this.oldBids = new Hand();
                            break;
                        }
                    }
                }
                if (this.oldBids.size() != 0) {
                    for (int j = 0; j < currentBid.getCount(); j++) {
                        if (this.oldBids.getPos(i).getRank() ==
                                currentBid.getHand().getPos(j).getRank()) {
                            if (this.oldBids.getPos(i).getSuit() ==
                                    currentBid.getHand().getPos(j).getSuit()) {
                                this.oldBids = new Hand();
                                break;
                            }
                        }
                    }
                }
            }
        }

        // Iterate through discarded cards from our previous bids and the cards
        // in our hand to see how likely it is the bidder is cheating.
        int counter = 0;
        for (Card c : playerHand) {
            if (c.getRank() == currentBid.getRank()) {
                counter++;
            }
        }
        for (Card c : this.oldBids) {
            if (c.getRank() == currentBid.getRank()) {
                counter++;
            }
        }

        // Make a decision based off of the cards seen.
        if (counter >= 4) {
            System.out.println("Based on my calculations, I'd say this fellow" +
                    " is playing a trick! You are a fraud and a charlatan!");
            return true;
        }
        // I have assumed the result should be random but weighted towards p.
        // Therefore I have implemented another simple probability simulation
        // based off of this assumption.
        double p = ((double) counter + 1) / 10; // Base odds between 0.1 and 0.4
        double random = rand.nextDouble(); // Generate number between 0 and 1.
        if (p > random) {
            System.out.println("I'm not certain, but it's possible there was " +
                    "some amount of untruth in this play.");
            return true;
        }

        return false; // If p > random, return true else false.
    }

    public static void main(String[] args) {
        // Setup
        Strategy strat1 = new ThinkerStrategy();
        // fill cardList with random cards
        int handSize = 5;
        Card[] cardList = new Card[handSize];
        for (int i = 0; i < handSize; i++) {
            Card.Rank randRank = Card.Rank.getRandom();
            Card.Suit randSuit = Card.Suit.getRandom();
            cardList[i] = new Card(randRank, randSuit);
        }

        // cheat()
        Hand hand1 = new Hand(cardList);
        Bid lastBid = new Bid(hand1, Card.Rank.JACK);
        System.out.println("bid: JACK.\nhand1: " + hand1);
        boolean cheat = strat1.cheat(lastBid, hand1);
        System.out.println("Should the player cheat?: " + cheat);

        // chooseBid()
        Bid nextBid = strat1.chooseBid(lastBid, hand1, cheat);
        System.out.println("\nBidding cards: " + nextBid.getHand());
        System.out.println("Therefore the bid for this hand is: " +
                nextBid);

        // callCheat()
        cheat = strat1.callCheat(hand1, lastBid);
        System.out.println("\nIf this player was given that bid, " +
                "they would call: " + cheat);

    }
}
