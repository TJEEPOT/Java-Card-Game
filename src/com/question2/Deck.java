package com.question2;

import java.io.*;
import java.util.Iterator;
import java.util.Random;

public class Deck<T extends Comparable<? super T>>
        implements Iterable<Card>, Serializable {
    // Part 1. Initialise array to hold all 52 cards in a deck.
    private Card[] deck = new Card[52];

    // Part 2. Constructor for full deck (now moved to newDeck).
    public Deck() {newDeck();}

    // Part 3a. Method to return the size of the current deck.
    public int size() {
        int numCards = 0;

        for (Card card : this.deck) {
            if (card != null) {
                numCards++;
            }
        }
        return numCards;
    }

    // Part 3b. Method to reinitialise the current deck. Constructor also
    // points here.
    public final void newDeck() {
        int cardCount = 0;
        for (int suitCount = 0; suitCount < 4; suitCount++) {
            Card.Suit[] suitVals = Card.Suit.values();
            Card.Suit curSuit = suitVals[suitCount];

            for (int rankCount = 0; rankCount < 13; rankCount++) {
                Card.Rank[] rankVals = Card.Rank.values();
                Card.Rank curRank = rankVals[rankCount];

                this.deck[cardCount] = new Card(curRank, curSuit);
                cardCount++;
            }
        }
    }

    // Part 4. Iterator to traverse deck in order to deal cards.
    // (Part 8 - Make Part 4 the default iterator.)
    @Override
    public Iterator<Card> iterator() { return new DeckIterator();}

    private class DeckIterator implements Iterator<Card> {
        private int pos;

        public DeckIterator() {pos = 0;}

        @Override
        public boolean hasNext() {
            return pos < deck.length;
        }

        @Override
        public Card next() {return deck[pos++];}

        @Override
        public void remove() {deck[pos - 1] = null;}
    }

    // Part 5. Method to randomise the card order.
    public void shuffle() {
        Random rand = new Random();

        // For each card in the deck, choose a random card in the deck and
        // swap it with that card.
        for (int i = 0; i < this.deck.length; i++) {
            int swapIndex = rand.nextInt(this.deck.length);
            Card temp = this.deck[swapIndex];
            this.deck[swapIndex] = this.deck[i];
            this.deck[i] = temp;
        }
    }

    // Part 6. Method removes top card from deck and returns it.
    public Card deal() {
        Iterator<Card> it = this.iterator();

        while (it.hasNext()) {
            Card next = it.next();
            if (next != null) {
                it.remove();
                return next;
            }
        }
        return null;
    }

    // Part 7. Iterate through all cards in Odd positions, then through all
    // cards in Even positions.
    public Iterator<Card> iteratorOddEven() {
        return new OddEvenIterator();
    }

    private class OddEvenIterator implements Iterator<Card> {
        private int pos;
        private boolean odd; // track if we're iterating on odds or evens

        public OddEvenIterator() {
            pos = 0;
            odd = true;
        }

        @Override
        public boolean hasNext() {
            // If we're not at the end, return true.
            if (pos < deck.length) return true;
            if (odd) { // if we reach the end and it's odd...
                odd = false; // switch to the even-numbered cards...
                pos = 1; // and reset counter to first even card
                return true;
            }
            return false;
        }

        @Override
        public Card next() {
            Card nextCard = deck[pos];
            pos = pos + 2; // increment pointer to the next odd/even card
            return nextCard;
        }

        @Override
        public void remove() {deck[pos - 2] = null;}
    }

    // Part 9. Set the Serialisation ID and allow the deck to be serialised.
    private static final long serialVersionUID = 100225766L;

    private static void serializeDeck(String filename, Deck deck) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(deck);
            System.out.println("\n--Written deck to output. Closing stream.--");
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Object loadDeck(String filename) {
        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);
            Object o = in.readObject();
            System.out.println("Object read from file. Closing Stream.");
            in.close();
            return o;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //Part 10. Call all methods.
    public static void main(String[] args) {
        // Test Part 2.
        Deck deck1 = new Deck();

        // Test Part 3a.
        System.out.println("Total cards in deck1: " + deck1.size());
        // Test Part 3b.
        deck1.newDeck();

        // Test Part 4. Now iterates without defining an iterator!
        for (Object o : deck1) {System.out.println(o);}

        // Test Part 5.
        System.out.println("\nShuffling Deck:");
        deck1.shuffle();
        for (Object o : deck1) {System.out.println(o);}

        // Test Part 6.
        System.out.println("\nDealing card: " + deck1.deal());

        // Test Part 7.
        deck1.newDeck(); // reinitialise the deck
        System.out.println("\nReporting odd cards then evens:");
        Iterator oddEvenIter = deck1.iteratorOddEven();

        while (oddEvenIter.hasNext()) {
            System.out.println(oddEvenIter.next());
        }

        // Test Part 9.
        serializeDeck("deck1.ser", deck1);
        Object o = loadDeck("deck1.ser");
        System.out.println("Object loaded: " + o);
    }
}
