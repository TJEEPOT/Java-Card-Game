/******************************************************************************

 Project     : CMP-5015Y - Summative Assessment 1: Java Programming:
                Java_Card_Game.

 File        : HumanStrategy.java

 Date        : Friday 31 January 2020

 Author      : Martin Siddons

 Description : Class which implements the Strategy interface and interacts with
                the user via keyboard commands.

 History     : 31/01/2020 - v1.0 - Initial setup, 
 ******************************************************************************/
package com.question2;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class HumanStrategy implements Strategy {
    public HumanStrategy() {}

    /*  Static method to ensure the character taken is an integer. Prevents
     *  InputMismatchException errors.
     */
    private static int getInt(Scanner in) {
        while (! in.hasNextInt()) {
            in.next();
            System.out.println("Incorrect input, please try again.");
        }
        return in.nextInt();
    }

    /*  Present the user with their cards and the value of the previous bid and
     *  allow them to select whether they want to cheat or not.
     */
    @Override
    public boolean cheat(Bid previousBid, Hand playerHand) {
        Strategy basicStrat = new BasicStrategy();
        Scanner in = new Scanner(System.in);

        // Before starting the turn, ensure the cards passed in are sorted.
        playerHand.sortAscending();

        System.out.println("\nThe previous player bid rank " +
                previousBid.getRank());
        System.out.println("Your cards are:");
        for (int i = 0; i < playerHand.size(); i++) {
            System.out.println(playerHand.getPos(i));
        }

        // Before asking the user, ensure they can actually choose to cheat or
        // if they're forced to by passing the arguments to BasicStrategy.
        if (basicStrat.cheat(previousBid, playerHand)) { // True = must cheat.
            System.out.println("Oof, bad luck. With cards like that it " +
                    "looks like you're going to have to cheat.\n");
        }
        else {
            Card.Rank nextRank = previousBid.getRank().getNext();
            int choice;
            do {
                System.out.println("\nWhat would you like to do?\n" +
                        "Enter '1' to play your rank " + previousBid.getRank() +
                        " or your rank " + nextRank + " card(s), or Enter " +
                        "'2' to attempt to cheat by playing something else.");
                choice = getInt(in);
            }
            while (choice != 1 && choice != 2);
            return choice != 1; // if not 1, return false.
        }
        return true;
    }

    @Override
    public Bid chooseBid(Bid previousBid, Hand playerHand, boolean cheat) {
        Scanner in = new Scanner(System.in);
        int cur; // Current input taken from Scanner
        Hand bidHand = new Hand();
        Card.Rank bidRank = previousBid.getRank();
        Card.Rank nextBidRank = bidRank.getNext();
        ArrayList<Card> tempCards = new ArrayList<>();

        // Sort the playerHand to make reading easier.
        playerHand.sortAscending();

        // Strategy for if we're not cheating.
        // First form a list of all usable cards.
        if (! cheat) {
            for (Card c : playerHand) {
                if (c.getRank() == bidRank || c.getRank() == nextBidRank) {
                    tempCards.add(c);
                }
            }

            // Automatically play a single card if that's all there is to play.
            if (tempCards.size() == 1) {
                System.out.println("Looks like you only have one card to " +
                        "play. I'll go ahead and play that for you.");
                bidHand.addCard(tempCards.get(0));
            }

            // Otherwise set out the available non-cheat cards for the player
            // and let them choose, then package those cards into a bid.
            else {
                System.out.println("Please Enter the number for the card " +
                        "you would like to play, one by one, from the " +
                        "following list. Then enter 0 continue the game:");
                for (int i = 0; i < tempCards.size(); i++) {
                    System.out.println((i + 1) + ". " + tempCards.get(i));
                }

                // Take input from the user, resolve each number to a card from
                // tempCards and add it to bidHand.
                int counter = 0;
                do {
                    boolean repeat = false;
                    Card temp = null;
                    cur = getInt(in);

                    // Ignore any number out of bounds.
                    if (cur > tempCards.size() || cur < 0) {
                        System.out.println("Ignoring invalid card number: " +
                                cur);
                        repeat = true;
                    }
                    // If the user does not select a card, warn them against it
                    // and select a card for them if they still don't.
                    if (cur == 0 && counter == 0) {
                        System.out.println("You have not chosen a card. " +
                                "Please choose one now or enter 0 again to " +
                                "have me to choose for you.");
                        cur = getInt(in);
                        if (cur == 0) {
                            Random rnd = new Random();
                            cur = rnd.nextInt(tempCards.size()) + 1;
                            System.out.println("Random card selected. Please" +
                                    "be more decisive next time, user.");
                        }
                        temp = tempCards.get(cur - 1);
                    }

                    // Run through the current choices and check that the new
                    // card is of the same rank as the previous ones, and
                    // ensure there are no duplicates being selected.
                    else if (cur > 0) {
                        temp = tempCards.get(cur - 1);
                        if (bidHand.size() > 0) {
                            if (temp.getRank() != bidHand.getPos(0).getRank()) {
                                System.out.println("Selecting cards from two " +
                                        "different ranks is cheating! Please " +
                                        "select again or enter 0 to finish.");
                                repeat = true;
                            }
                            else { // ranks are checked, just check suits.
                                for (Card c : bidHand) {
                                    if (temp.getSuit() == c.getSuit()) {
                                        System.out.println("You have already " +
                                                "selected that card, please " +
                                                "select again or enter zero " +
                                                "to finish.");
                                        repeat = true;
                                    }
                                }
                            }
                        }
                    }
                    // Assign card and increase counter only if there's no flag.
                    if (! repeat && cur != 0) {
                        bidHand.addCard(temp);
                        System.out.println(temp + " was added to your bid.");
                        counter++;
                    }
                }
                while (cur != 0 && counter < 4 && counter < tempCards.size());
            }
            // Set bidRank to be equal to the rank of the chosen card(s).
            bidRank = bidHand.getPos(0).getRank();
        }

        // Strategy for if we are cheating.
        // Start by forming a list of cards to choose from.
        else {
            for (Card c : playerHand) {tempCards.add(c);}

            // Before starting the turn, ensure the cards passed in are sorted.
            tempCards.sort(new Card.CompareAscending());

            // Automatically play a single card if that's all there is to play.
            if (tempCards.size() == 1) {
                System.out.println("Looks like you only have one card to " +
                        "play. I'll go ahead and add that to the bid for you.");
                bidHand.addCard(tempCards.get(0));
            }

            // Otherwise set out the available non-cheat cards for the player
            // and let them choose, then package those cards into a bid.
            else {
                ;
                System.out.println("You have decided to cheat. Please Enter " +
                        "the number for the card(s) you would like to bid, " +
                        "one by one, from the following list. Then Enter 0 " +
                        "to continue the game:");
                for (int i = 0; i < tempCards.size(); i++) {
                    System.out.println((i + 1) + ". " + tempCards.get(i));
                }

                // Take input from the user, add that number to an array.
                int counter = 0;
                do {
                    boolean repeat = false;
                    Card temp = null;
                    cur = getInt(in);

                    // Ignore any number out of bounds.
                    if (cur > tempCards.size() || cur < 0) {
                        System.out.println("Ignoring invalid card number: " +
                                cur);
                        repeat = true;
                    }
                    // If the user does not select a card, warn them against it
                    // and get them to enter again.
                    else if (cur == 0 && counter == 0) {
                        System.out.println("You have not chosen a card. " +
                                "Please select again");
                        cur = - 1; // ensures do..while loop doesn't end.
                        repeat = true;
                    }

                    // Run through the current choices and check that the new
                    // card is of the same rank as the previous ones, and
                    // ensure there are no duplicates being selected.
                    else if (cur > 0) {
                        temp = tempCards.get(cur - 1);
                        if (bidHand.size() > 0) {
                            for (Card c : bidHand) {
                                if (temp.getRank() == c.getRank() &&
                                        temp.getSuit() == c.getSuit()) {
                                    System.out.println("You have already " +
                                            "selected that card, please " +
                                            "select again or enter 0 to " +
                                            "finish.");
                                    repeat = true;
                                }
                            }
                        }
                    }
                    // Assign card and increase counter only if there's no flag.
                    if (! repeat && cur != 0) {
                        bidHand.addCard(temp);
                        System.out.println(temp + " was added to your bid.");
                        counter++;
                    }
                }
                while (cur != 0 && counter < 4 && counter < tempCards.size());
            }

            // Retrieve the bidRank from the player and set it.
            do {
                System.out.println("Which bid would you like to call?\n" +
                        "1. " + bidRank + " or 2. " + nextBidRank + "?");
                cur = getInt(in);
            }while (cur != 1 && cur != 2);
            if (cur == 2) {bidRank = nextBidRank;} // no need to set for 1.
        }
        return new Bid(bidHand, bidRank);
    }

    /*  Allows the human player to call cheat on another player, based off of
     *  what they've seen in the game, no extra help will be given.
     */
    @Override
    public boolean callCheat(Hand playerHand, Bid currentBid) {
        Scanner in = new Scanner(System.in);
        String cur; // Current input taken from Scanner
        int counter = 0;

        System.out.print("Your hand includes: ");
        for (Card c : playerHand){
            if (c.getRank() == currentBid.getRank()) {
                System.out.print(c + " ");
                counter++;
            }
        }
        if (counter == 0){
            System.out.println("no cards of that rank.");
        }

        do {
            System.out.println("\nWill you call cheat (Y/N)?");
            cur = in.nextLine();
            if (cur.equals("Y") || cur.equals("y")) {return true;}
            if (cur.equals("N") || cur.equals("n")) {return false;}
        }while(true);
    }

    public static void main(String[] args) {
        // cheat() Testing
        Strategy strat1 = new HumanStrategy();
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
        boolean cheat = strat1.cheat(bid1, hand1);
        System.out.println("The user is going to cheat: " + cheat);

        // chooseBid() Testing
        System.out.println("Testing placing a bid on the last set of cards:\n");
        Bid bid2 = strat1.chooseBid(bid1, hand1, cheat);
        System.out.println("The user's bid is: " + bid2);

        System.out.println("\nTesting where the user is not going to cheat:");
        // ensure a JACK is in the hand at least.
        Hand hand2 = new Hand(hand1);
        hand2.addCard(new Card(Card.Rank.JACK, Card.Suit.HEARTS));
        Bid bid3 = strat1.chooseBid(bid1, hand2, false);
        System.out.println("The user's bid is: " + bid3);

        // callCheat() Testing
        cheat = strat1.callCheat(hand1, bid1);
        System.out.println("User calls cheat on hand1, bid1: " + cheat);
    }

}