package com.question1;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class Card implements Serializable, Comparable<Card> {
    // Part 1. Set serialisation ID equal to registration number.
    private static final long serialVersionUID = 100225776L;

    // Part 2. Set up enums for Rank and Suit.
    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8),
        NINE(9), TEN(10), JACK(10), QUEEN(10), KING(10), ACE(11);

        // Initializer for Rank instances.
        private final int value;

        Rank(int val) {this.value = val;}

        // Method to return the associated value of the given Rank.
        public int getValue() {return this.value;}

        // Method to return the Value before the one passed-in.
        public Rank getPrevious() {
            Rank[] vals = Rank.values();
            // Ensure vals loops backwards to ACE if rank = TWO
            return vals[((this.ordinal() - 1) + vals.length) % vals.length];
        }

        // Method to generate and return a random rank.
        public static Rank getRandom() {
            Random rand = new Random();
            int rndInt = rand.nextInt(13);

            Rank[] vals = Rank.values();
            return vals[rndInt];
        }
    }

    enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES;

        // Method to generate and return a random Suit.
        public static Suit getRandom() {
            Random rand = new Random();
            int rndInt = rand.nextInt(4);

            Suit[] vals = Suit.values();
            return vals[rndInt];
        }
    }

    // Part 3. Attributes, Constructor, Accessor Methods and toString.
    private final Rank rank;
    private final Suit suit;

    public Card(Rank r, Suit s) {
        this.rank = r;
        this.suit = s;
    }

    public Rank getRank() {return rank;}

    public Suit getSuit() {return suit;}

    @Override
    public String toString() {return "The " + rank + " of " + suit;}

    // Part 4. Comparable interface to Sort cards in descending order by Rank,
    // then Suit.
    @Override
    public int compareTo(Card c) {
        if (this.rank.ordinal() < c.rank.ordinal()) {return 1; }
        if (this.rank.ordinal() > c.rank.ordinal()) {return - 1;}
        if (this.suit.ordinal() > c.suit.ordinal()) {return 1; }
        if (this.suit.ordinal() < c.suit.ordinal()) {return - 1;}
        return 0; // if both ranks and suits match, return 0
    }

    // Part 5. Static method to find the distance between two ranks.
    public static int difference(Card a, Card b) {
        int aRank = a.rank.ordinal();
        int bRank = b.rank.ordinal();
        return Math.abs(aRank - bRank); // absolute value between a and b.
    }

    // Part 6. Method to find the difference in Value attribute between cards.
    public static int differenceValue(Card a, Card b) {
        int aValue = a.rank.getValue();
        int bValue = b.rank.getValue();
        return Math.abs(aValue - bValue); // absolute value between the Values.
    }

    // Part 7a. Comparator class to sort cards ascending by Rank.
    public static class CompareAscending implements Comparator<Card> {
        // List should generally be sorted by Suit before calling this method.
        @Override
        public int compare(Card a, Card b) {
            return a.getRank().ordinal() - b.getRank().ordinal();
        }
    }

    // Part 7b. Comparator class to determine ascending order of suit.
    public static class CompareSuit implements Comparator<Card> {
        @Override
        public int compare(Card a, Card b) {
            int aSuit = a.getSuit().ordinal();
            int bSuit = b.getSuit().ordinal();
            return aSuit - bSuit;
        }
    }

    // Part 8. Selection of comparators including Lambda.
    public static void selectTest(Card cardIn) {
        ArrayList<Card> cardList = new ArrayList<>();
        Random rand = new Random();
        int randCount = rand.nextInt(7) + 3;
        // The number of cards in the array is between 3 and 10

        int i = 0;
        while (i < randCount) { // fill cardList with cards
            Rank randRank = Rank.getRandom();
            Suit randSuit = Suit.getRandom();
            cardList.add(new Card(randRank, randSuit));
            i++;
        }

        System.out.println("The comparison card is " + cardIn);
        for (i = 0; i < cardList.size(); i++) {
            System.out.print("\n");
            Card curCard = cardList.get(i);
            System.out.println("Comparing this with " + curCard + ":");

            int diff = new CompareAscending().compare(curCard, cardIn);
            System.out.println("CompareAscending difference is " + diff);

            diff = new CompareSuit().compare(curCard, cardIn);
            System.out.println("CompareSuit difference is " + diff);

            Comparator<Card> rankCard = (Card a, Card b) -> {
                int comp = a.getRank().compareTo(b.getRank());
                if (comp == 0) {comp = b.getSuit().compareTo(a.getSuit());}
                return comp;
            };
            int result = rankCard.compare(curCard, cardIn);
            System.out.println("Lambda comparator result is: " + result);
        }
    }

    // Part 9. Test implementation of above methods.
    public static void main(String[] args) {
        // Test part 2 + 3.
        Card.Rank rankJack = Card.Rank.JACK;
        System.out.println("Value of JACK is: " + rankJack.getValue());
        System.out.println("JACK previous rank is: " + rankJack.getPrevious());

        Card.Rank rankTwo = Card.Rank.TWO;
        System.out.println("Rank Previous to TWO is: " + rankTwo.getPrevious());

        Card.Suit suitHearts = Card.Suit.HEARTS;
        System.out.println("suitHearts is: " + suitHearts);

        Card.Suit suitRandom = Card.Suit.getRandom();
        System.out.println("suitRandom is: " + suitRandom + "\n");

        Card threeHearts = new Card(Card.Rank.THREE, Card.Suit.HEARTS);
        System.out.println("Card threeHearts is: " + threeHearts + "\n");

        // Test Part 4.
        Card sixHearts = new Card(Card.Rank.SIX, Card.Suit.HEARTS);
        Card threeSpades = new Card(Card.Rank.THREE, Card.Suit.SPADES);

        System.out.println("Is sixHearts to threeHearts descending?: " +
                sixHearts.compareTo(threeHearts)); // -1 (yes)
        System.out.println("is threeHearts to threeSpades descending?: " +
                threeHearts.compareTo(threeSpades)); // -1 (yes)
        System.out.println("is threeSpades to threeHearts descending?: " +
                threeSpades.compareTo(threeHearts) + "\n"); // 1 (no)

        // Test Part 5.
        System.out.println("The Rank difference between " + threeHearts +
                " and " + sixHearts + " is " +
                difference(threeHearts, sixHearts));

        // Test Part 6.
        System.out.println("The value difference between " + threeHearts +
                " and " + threeSpades + " is " +
                differenceValue(threeHearts, threeSpades) + "\n");

        // Test Part 7.
        ArrayList<Card> c1 = new ArrayList<>();
        c1.add(sixHearts);
        c1.add(threeHearts);
        c1.add(threeSpades);
        System.out.println("Card list c1:");
        for (Card c : c1) {System.out.println(c.toString());}

        System.out.println("\nSort list c1 in ascending order:");
        Collections.sort(c1, new CompareSuit()); // determine suit order
        Collections.sort(c1, new CompareAscending()); // sort by rank

        for (Card c : c1) {System.out.println(c.toString());}
        System.out.println("\n");

        // Test Part 8.
        Card.selectTest(threeHearts);

        // Test Part 1.
        String filename = "c1.ser";
        Object o = null;

        try {
            FileOutputStream fos = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(sixHearts);
            System.out.println("\nWritten card sixHearts to output. " +
                    "Closing stream.");
            out.close();
        }
        catch (Exception e) {e.printStackTrace();}

        try {
            FileInputStream fis = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fis);
            o = in.readObject();
            System.out.println("Object read from file. Closing stream.");
            in.close();
        }
        catch (Exception e) {e.printStackTrace();}

        System.out.println("Object read = " + o);
    }
}

