package com.question2;

import java.io.*;
import java.util.*;

public class Hand implements Iterable<Card>, Serializable {
    // Part 1. Constructors.
    // Create two lists, one that stores a list of cards which can be sorted,
    // and the other holding the cards in the position that they were inserted
    // into the hand. sortableCards is the hand presented to the user.
    private ArrayList<Card> sortableCards;
    private ArrayList<Card> insertedOrderCards;

    public Hand() {
        this.sortableCards = new ArrayList<>();
        this.insertedOrderCards = new ArrayList<>();
        this.rankCount = rankCount = new int[13]; // elements default to 0
    }

    public Hand(Card[] cards) {
        this();
        // use part 3b function to add these cards to hand
        for (Card c : cards) {this.addCard(c);}
    }

    // Add all the cards from the passed hand to this hand. We could use
    // serialisation for this but there's a perfectly good method already made
    // further below. KISS.
    public Hand(Hand hand) {
        this();
        this.addHand(hand);
    }

    // Part 2. Store the number of each rank in the hand.
    private int[] rankCount; // (also added to constructor)

    // Part 3a. 'Add' methods - Add Card to hand.
    public void addCard(Card card) {
        this.sortableCards.add(card);
        this.insertedOrderCards.add(card);

        this.rankCount[card.getRank().ordinal()] =
                this.rankCount[card.getRank().ordinal()] + 1;
    }

    // Part 3b. Add Collection of cards to hand.
    public void addCollection(Collection<Card> cardCollection) {
        for (Card c : cardCollection) {this.addCard(c);}
    }

    // Part 3c. Add a hand to the current hand.
    public void addHand(Hand hand) {
        // add each card in passed hand to this hand (and increment rankCount)
        for (int i = 0; i < hand.sortableCards.size(); i++) {
            this.addCard(hand.sortableCards.get(i));
        }
    }

    // Part 4a. 'Remove' methods - Remove a card from the Hand.
    public boolean removeCard(Card card) {
        // This function will remove only the first matching card passed to it
        // in the case of multiple decks being used. Iterator used to prevent
        // ConcurrentModificationException.
        for (int i = 0; i < this.sortableCards.size(); i++) {
            Card cur = this.sortableCards.get(i);

            if (cur.getRank() == card.getRank() &&
                    cur.getSuit() == card.getSuit()) {
                this.sortableCards.remove(cur);

                // Remove the oldest card in the inserted order list (as
                // it's impossible to tell two duplicate cards apart).
                for (int j = 0; j < this.insertedOrderCards.size(); j++) {
                    cur = this.insertedOrderCards.get(j);

                    if (cur.getRank() == card.getRank() &&
                            cur.getSuit() == card.getSuit()) {
                        this.insertedOrderCards.remove(cur);
                        this.rankCount[cur.getRank().ordinal()] =
                                this.rankCount[cur.getRank().ordinal()] - 1;
                        return true;
                    }
                }
            }
        }
        return false; // this is only hit if the card wasn't found
    }

    // Part 4b. Remove all cards from hand listed in passed-in hand. This
    // method will attempt to remove one copy from the current hand of each of
    // the cards passed by argument but will return false if it removes less
    // than the total number of cards in the argument hand.
    public boolean removeHand(Hand hand) {
        int removalCounter = 0;

        // Iterator used to prevent ConcurrentModificationException
        Iterator<Card> iter = hand.sortableCards.iterator();
        while (iter.hasNext()) {
            Card c = iter.next();
            if (this.removeCard(c)) {removalCounter++;}
            // If the current card has been removed, increment counter.
        }

        // check to see if we've removed the same amount of cards passed in,
        // if so return true else return false
        return removalCounter == hand.sortableCards.size();
    }

    // Part 4c. Remove a card at a specific position in the hand
    public Card removePos(int pos) {
        Card found = null;
        // This might not be the most efficient implementation but I chose it
        // to keep the code clean and reuse previously-written code.
        try {
            found = this.sortableCards.get(pos);
            this.removeCard(found);
        }
        catch (IndexOutOfBoundsException e) {
            System.out.println("---Error: Card number " + pos +
                    " does not exist in this hand.---");
        }
        return found;
    }

    // Part 5. Iterator for the hand to iterate through cards in added order.
    // This would possibly be easier if each card in the hand had an
    // 'int originalPosition' constant assigned to it. That doesn't seem to be
    // the intended goal here though, so the choices are add/remove each card
    // to/from two separate lists each time, or change the implementation of
    // the current Collection to allow for storing a counter value with each
    // card object - I have chosen the former and added a second ArrayList
    // while adjusting the above code to work with both collections.

    // Warning to developers: Since this is the default implementation of
    // iterator, any foreach call will iterate in the inserted order, therefore
    // it is NOT safe to return insertedOrderCards.get and use that index on
    // the hand (such as in removePos). In that case you would need to use
    // removeCard instead.
    @Override
    public Iterator<Card> iterator() { return new InsertedOrderIterator();}

    private class InsertedOrderIterator implements Iterator<Card> {
        private int pos;

        public InsertedOrderIterator() {pos = 0;}

        @Override
        public boolean hasNext() {
            // Return true if position is less than the cards in the array.
            return pos < insertedOrderCards.size();
        }

        @Override
        public Card next() {return insertedOrderCards.get(pos++);}
    }

    // Part 6. Hand should be Serializable.
    private static final long serialVersionUID = 100225786L;

    public static void serializeHand(String filename, Hand hand) {
        Hand orderedHand = new Hand();
        for (Card c : hand) { // Iterate through the deck by order added.
            orderedHand.addCard(c);
        }
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(orderedHand);
            System.out.println("\n--Written hand to output. Closing stream.--");
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object loadHand(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);
            Object o = in.readObject();
            System.out.println("--Object read from file. Closing Stream.--");
            in.close();
            return o;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Extra Requested methods:
    public void sortDescending() {Collections.sort(this.sortableCards);}

    public void sortAscending() {
        this.sortableCards.sort(new Card.CompareAscending());
    }

    public int countRank(Card.Rank rank) {return rankCount[rank.ordinal()];}

    public int handValue() {
        int totalValue = 0;

        for (Card c : this.sortableCards) {
            totalValue = totalValue + c.getRank().getValue();
        }
        return totalValue;
    }

    public String toString() {
        String handString = "";

        for (Card c : this.sortableCards) {
            if (! Objects.equals(handString, "")) {
                handString = handString + ", " + c;
            }
            else {handString = c.toString();}
        }
        return handString;
    }

    public boolean isFlush() {
        if (this.sortableCards.size() < 2) {return false;}
        // you can't have a flush with less than 2 cards so end here.

        Card.Suit firstSuit = this.sortableCards.get(0).getSuit();
        for (Card c : this.sortableCards) {
            if (c.getSuit() != firstSuit) return false;
        }
        return true;
    }

    public boolean isStraight() {
        if (this.sortableCards.size() < 2) return false;
        // you can't have a straight with one or zero cards.
        boolean inStraight = false;
        boolean foundStraight = false;

        for (int i = 0; i < this.rankCount.length; i++) {
            // If you find duplicates of a rank, you can't have a pure straight.
            if (this.rankCount[i] > 1) return false;

            // If you find the first 1, you are now in your first straight.
            if (rankCount[i] == 1 && ! foundStraight) {
                inStraight = true;
                foundStraight = true;
            }

            // If you find 0 after finding a 1, you are no longer in a straight.
            if (rankCount[i] == 0 && inStraight) {inStraight = false;}

            // If you find another 1 after previously being in a straight, then
            // this deck must have two straights, so return false.
            if (rankCount[i] == 1 && ! inStraight && foundStraight) {
                return false;
            }
        }
        // After checking through the entire array, if you found just
        // one straight at some point, you have a straight. No need for any
        // additional checks.
        return true;
    }

    // Additional methods to make Cheat easier to program:
    // Return the number of cards in the hand.
    public int size() {return this.sortableCards.size();}

    // Return the card at the given position.
    public Card getPos(int pos) {return this.sortableCards.get(pos);}

    // Part 7. Test the above methods.
    public static void main(String[] args) {
        // Test Part 1.
        Hand emptyHand = new Hand();
        System.out.println("emptyHand contains:" + emptyHand.sortableCards);

        int handSize = 5;
        Card[] cardList = new Card[handSize];
        for (int i = 0; i < handSize; i++) { // fill cardList with random cards
            Card.Rank randRank = Card.Rank.getRandom();
            Card.Suit randSuit = Card.Suit.getRandom();
            cardList[i] = new Card(randRank, randSuit);
        }

        Hand hand1 = new Hand(cardList); // construct a hand from above array
        System.out.println("Hand 1 contains:\n" + hand1.sortableCards);

        Hand hand2 = new Hand(hand1); // construct a hand from cards in hand1
        System.out.println("Hand 2 contains:\n" + hand2.sortableCards);

        // Test Part 2.
        System.out.print("\nCard ranks in hand1 = ");
        for (int i = 0; i < hand1.rankCount.length; i++) {
            System.out.print(hand1.rankCount[i] + ", ");
        }

        // Test Part 3.
        Card c1 = cardList[0]; // take the first card from cardList
        hand1.addCard(c1); // and add it to hand1
        System.out.println("\n\nWith c1 added to hand1, hand1 contains:\n" +
                hand1.sortableCards);

        Collection<Card> handCollection = new ArrayList<>(); // build Collection
        handCollection.add(new Card(Card.Rank.ACE, Card.Suit.SPADES));
        handCollection.add(new Card(Card.Rank.KING, Card.Suit.SPADES));
        hand1.addCollection(handCollection); // add from Collection to hand1
        System.out.println("With the collection hand3 added to hand1, hand1 " +
                "now contains:\n" + hand1.sortableCards);
        Hand hand3 = new Hand(); // create a hand from these cards for later
        for (Card c : handCollection) {hand3.addCard(c);}

        // Add a random new card in here.
        Card c2 = new Card(Card.Rank.getRandom(), Card.Suit.getRandom());
        hand1.addCard(c2);
        System.out.println("Added " + c2 + " to hand1.");

        hand1.addHand(hand2); // Add hand2 to hand1. This is essentially the
        // same as above but is called on a Hand rather than a Collection.
        System.out.println("With hand2 added to hand1, hand1 now contains:\n" +
                hand1.sortableCards);
        System.out.print("\nCard ranks in hand1 now = ");
        for (int i = 0; i < hand1.rankCount.length; i++) {
            System.out.print(hand1.rankCount[i] + ", ");
        }

        // Test Part 4.
        // remove previously assigned card (and duplicates) from hand1.
        System.out.println("\n\nDid we remove c1 from hand2?: " +
                hand2.removeCard(c1));
        System.out.println("After trying to remove c1, hand2 now contains: " +
                hand2.sortableCards);

        // remove remaining cards from hand2 from hand1.
        System.out.println("Attempting to remove all cards in hand2 from " +
                "hand1...");
        System.out.println("Did we remove all of hand2 from hand1?: " +
                hand1.removeHand(hand2));
        System.out.println("After trying to remove hand2, hand1 now contains:" +
                hand1.sortableCards);

        // remove card from certain position from hand1.
        System.out.println("Attempting to remove card 50 from hand1:");
        hand1.removePos(50);
        System.out.println("Removing card 1 from hand1, which is " +
                hand1.removePos(1) + ".");
        System.out.println("Hand1 now contains: " + hand1.sortableCards);

        System.out.print("Card ranks in hand1 now = ");
        for (int i = 0; i < hand1.rankCount.length; i++) {
            System.out.print(hand1.rankCount[i] + ", ");
        }

        // Test Part 5.
        System.out.println("\n\nThe remaining cards in the order they were " +
                "added to the hand are:");
        for (Card c : hand1) {System.out.println(c);}

        // Test Part 6.
        serializeHand("hand1.ser", hand1);
        Object o = loadHand("hand1.ser");
        System.out.println("Object loaded is: " + o);

        // Test Extra Methods.
        hand1.sortDescending();
        System.out.println("\nAfter using descending sort, hand1 is now:\n" +
                hand1.sortableCards);

        hand1.sortAscending();
        System.out.println("After using ascending sort, hand1 is now:\n" +
                hand1.sortableCards);

        System.out.println("\nThere are " + hand1.countRank(Card.Rank.JACK) +
                " cards of rank Jack in hand1.");

        System.out.println("The total value of hand1 is " + hand1.handValue());
        System.out.println("The total value of emptyHand is " +
                emptyHand.handValue());

        System.out.println("\nTesting toString on hand1:\n" + hand1);

        System.out.println("\nAre all the cards in hand1 of the same suit? " +
                hand1.isFlush());
        System.out.println("Are all the cards in emptyHand of the same suit? " +
                emptyHand.isFlush());
        System.out.println("Are all the cards in hand3 of the same suit? " +
                hand3.isFlush()); // should be, as both are spades.

        System.out.println("\nAre all the cards in hand1 in one straight? " +
                hand1.isStraight());
        System.out.println("Are all the cards in emptyHand in one straight? " +
                emptyHand.isStraight());
        System.out.println("Are all the cards in hand3 in one straight? " +
                hand3.isStraight()); // should be, as it goes king-ace.
    }
}