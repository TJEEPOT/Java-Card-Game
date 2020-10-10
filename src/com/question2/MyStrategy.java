/******************************************************************************

 Project     : CMP-5015Y - Java Programming:
                Java_Card_Game.

 File        : MyStrategy.java

 Date        : Sunday 02 February 2020

 Author      : Martin Siddons

 Description : A custom strategy implementation created to test what can be
 done within the bounds of the game. This strategy is referred to as Master AI
 in BasicCheat and StrategyFactory.

 History     : 02/02/2020 - v1.0 - Initial setup, 
 ******************************************************************************/
package com.question2;

import java.util.ArrayList;
import java.util.Random;

public class MyStrategy implements Strategy {
    private Hand oldBids; // All cards discarded by this player.
    private Hand othersOldBids; // Cards discarded by all other players.
    private int players;

    public MyStrategy() {
        this.oldBids = new Hand();
        this.othersOldBids = new Hand();
    }

    public void setPlayers(int players) {
        this.players = players;
    }

    /*  MyStrategy uses a record of the amount of discards made so far to decide
     *  whether to cheat or not. It knows that essentially if there's a lot of
     *  discards, there are fewer cards held in each hand for people to know and
     *  they will be less likely to call cheat. Having a variable number of
     *  players makes this difficult to be definitive, however, so we use it
     *  in conjunction with a simple probability simulation.
     */
    @Override
    public boolean cheat(Bid previousBid, Hand playerHand) {
        Random rand = new Random();
        Card.Rank nextRank = previousBid.getRank().getNext();
        boolean cheat = true;

        // Like ThinkerStrategy, check the hand for a card of the given rank or
        // the next rank up and decide not to cheat if there's a match.
        for (Card c : playerHand) {
            if (c.getRank() == previousBid.getRank()) {cheat = false;}
            else if (c.getRank() == nextRank) {cheat = false;}
        }
        if (cheat) {return true;} // No match means we must cheat.

        // Otherwise use the number of total discards to find whether to cheat.
        double discards = this.othersOldBids.size() + this.oldBids.size();
        double p = discards / 65;
        // 65 seems like a good number as it gives p=0.4 at 26 cards.
        double random = rand.nextDouble();
        return p > random;
    }

    /*  If MyStrategy doesn't have to cheat, it will play all the cards it can
     *  in order to get down it's card count as quick as possible. When it can
     *  only cheat, it plays cautiously and only plays one card, preferring to
     *  choose from single ranks in hand, matching cards it previously also
     *  bid. It will call rank one above the last in the hopes of outsmarting
     *  ThinkerStrategy.
     */
    @Override
    public Bid chooseBid(Bid previousBid, Hand playerHand, boolean cheat) {
        Hand bidHand = new Hand(); // stores the final hand to pass
        Card.Rank bidRank = previousBid.getRank();
        Card.Rank nextBidRank = bidRank.getNext();
        Random rand = new Random();

        // Select cards where we're not cheating. This part is the same as
        // what is implemented in ThinkerStrategy.
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
            if (bidRankHand.size() >= nextBidRankHand.size()) {
                bidHand.addHand(bidRankHand);
            }
            else {
                bidHand.addHand(nextBidRankHand);
                bidRank = nextBidRank; // set rank for returning bid.
            }
        }

        // Strategy for when we are forced to cheat.
        else {
            // Sort the cards to ensure the higher-rank cards are towards the
            // end of the hand.
            playerHand.sortAscending();

            // If there are no discards, play one of the higher cards in the
            // hand, similar to Thinker. Weight the later cards more by using
            // an estimation of cumulative probability.
            if (this.oldBids.size() == 0) {
                int die1 = rand.nextInt(playerHand.size());
                int die2 = rand.nextInt(playerHand.size());
                int pickedCard = (die1 + die2) / 2; // Int ensures rounding up.
                bidHand.addCard(playerHand.getPos(pickedCard));
            }

            else {
                // Find the ranks of the cards we've discarded the most so far,
                // as these don't show up on ThinkerStrategy's lists. This code
                // is bad but I can't think of a better way right now.
                Hand sortedDiscards = new Hand(this.oldBids);
                Card.Rank[] vals = Card.Rank.values();
                sortedDiscards.sortAscending();

                int[] numDiscarded = new int[13];
                for (int i = 0; i < sortedDiscards.size(); i++) {
                    numDiscarded
                            [sortedDiscards.getPos(i).getRank().ordinal()]++;
                }

                // Find the rank that has the most discards.
                int max = 0;
                for (int i : numDiscarded) {if (i > max) {max = i;}}

                // Then retrieve all cards discarded that many times. Loop until
                // we have cards we can use.
                while (bidHand.size() == 0) {
                    ArrayList<Card.Rank> maxRanks = new ArrayList<>();
                    for (int i = 0; i < numDiscarded.length; i++) {
                        if (numDiscarded[i] == max) {
                            maxRanks.add(vals[i]);
                        }
                    }

                    // Iterate through the hand to find all the cards matching
                    // the most discarded ranks. If they can't be found, loop
                    // back and find the ranks of the cards discarded one fewer
                    // times. Pick the first card that matches.
                    boolean found = false;
                    for (Card c : playerHand) {
                        for (int i = 0; i < maxRanks.size() && ! found; i++) {
                            if (maxRanks.get(i) == c.getRank()) {
                                bidHand.addCard(c);
                                found = true;
                            }
                        }
                    }
                    max--;
                }
            }

            // If the last player played 4 of the bidded card, it would be
            // stupid to also play a card of that rank, so select the next rank
            if (previousBid.getHand().size() == 4) {
                bidRank = nextBidRank;
            }
            // Since Thinker will know how many of the current card rank have
            // been played if it was the last player, we should call the rank
            // one up from this to gain a slight advantage.
            bidRank = nextBidRank;
        }
        // Add all the chosen cards to the internal discard piles for later
        // reference and return the found values as a bid.
        this.oldBids.addHand(bidHand);
        return new Bid(bidHand, bidRank);
    }

    /*  MyStrategy is a dirty little cheater and will happily look at the last
     *  discarded card every one in twenty calls and use that to decide what to
     *  call. At any other time, it takes in as much information as it can and
     *  tries to spit out a reasonable likelihood the card is a cheat, which
     *  should be more accurate than the ThinkerStrategy that it is modelled
     *  after.
     */
    @Override
    public boolean callCheat(Hand playerHand, Bid currentBid) {
        Random rand = new Random();

        // First, perform the same operation that Thinker does, by iterating
        // through the cards in the discard memories and check them against the
        // cards in currentBid and playerHand to see if there's any matches.
        // If there is, disregard our memory as this means cheat has been
        // called and those cards are either back in our hand or now in another
        // player's hand.
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
        // Same for othersOldBids.
        for (int i = 0; i < this.othersOldBids.size(); i++) {
            for (int j = 0; j < playerHand.size(); j++) {
                if (this.othersOldBids.getPos(i).getRank() ==
                        playerHand.getPos(j).getRank()) {
                    if (this.othersOldBids.getPos(i).getSuit() ==
                            playerHand.getPos(j).getSuit()) {
                        this.othersOldBids = new Hand();
                        break;
                    }
                }
            }
            if (this.othersOldBids.size() != 0) {
                for (int j = 0; j < currentBid.getCount(); j++) {
                    if (this.othersOldBids.getPos(i).getRank() ==
                            currentBid.getHand().getPos(j).getRank()) {
                        if (this.othersOldBids.getPos(i).getSuit() ==
                                currentBid.getHand().getPos(j).getSuit()) {
                            this.othersOldBids = new Hand();
                            break;
                        }
                    }
                }
            }
        }

        // Now see if we're sneaking a look at the last bid card and if so,
        // look at it.
        int cheating = rand.nextInt(20);
        if (cheating == 1) {
            // retrieve the last card in currentBid
            Card cardBid = currentBid.getHand().getPos
                    (currentBid.getHand().size() - 1);
            if (cardBid.getRank() != currentBid.getRank()) {
                System.out.println("Events have transpired to lead me to know" +
                        " for certain that your play was that of trickery.");
                return true;
            }
            else {
                return false;
            }
        }

        // Iterate through discarded cards from our previous bids and the cards
        // in our hand to see how likely it is the bidder is cheating.
        int counter = 0;
        for (
                Card c : playerHand) {
            if (c.getRank() == currentBid.getRank()) {
                counter++;
            }
        }
        for (
                Card c : this.oldBids) {
            if (c.getRank() == currentBid.getRank()) {
                counter++;
            }
        }

        // Make a decision based off of the cards seen.
        if (counter >= 4) {
            System.out.println("The odds that you have played the card you " +
                    "say you have is exactly zero, as I have already played " +
                    "them all. Cheater.");
            return true;
        }

        // If we don't know for certain, make an educated guess if the player is
        // cheating or not. This code works amazing when there's only two
        // players but does almost nothing beyond that. If I could pass through
        // how many players the game has, this would be 100x more effective.
        double allBidsSize = this.oldBids.size() + this.othersOldBids.size(),
                playerHandSize = playerHand.size(),
                bidHandSize = currentBid.getHand().size();

        double cardProbability = ((this.oldBids.size() +
                playerHandSize) / 52) * ((double) counter / 6);
        // The more cards we know of, the more accurate our prediction is and so
        // the more faith we can put into being correct.

        double bidProbability = (1 - (bidHandSize / 4)) / 1.2;
        // The fewer cards that are being discarded this turn, the more likely
        // it is the current player is not cheating as most strategies revolve
        // around discarding as few cheat cards as possible. Dividing this by
        // 1.2 to weight it a little lower than other probabilities.

        double gameEndProbability = allBidsSize / 52;
        // The more cards are currently discarded, the more likely it is that
        // the game is coming to an end, so more cheating is expected.

        // Find the average of the above probabilities.
        double finalProbability = (cardProbability +
                bidProbability + gameEndProbability) / 3;

        //        System.out.printf("MasterAI: [Pcard: %.3f][Pbid: %.3f]" +
        //                        "[Pendgame: %.3f][Pcheat: %.3f]\n",
        //                cardProbability, bidProbability, gameEndProbability,
        //                finalProbability);
        this.othersOldBids.addHand(currentBid.getHand()); // memorise this bid.
        return finalProbability > 0.5; // Change this for more than 2 players.
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
