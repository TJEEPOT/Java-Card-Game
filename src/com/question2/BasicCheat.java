package com.question2;

import java.util.*;

public class BasicCheat implements CardGame {
    private Player[] players;
    private int nosPlayers;
    private static final int MINPLAYERS = 5;
    private int currentPlayer;
    private Hand discards;
    private Bid currentBid;
    private int cheatCounter;
    private int cheatCorrect;

    public BasicCheat() {
        this(MINPLAYERS);
    }

    public BasicCheat(int n) {
        nosPlayers = n;
        players = new Player[nosPlayers];
        for (int i = 0; i < nosPlayers; i++) {
            players[i] = (new BasicPlayer(new BasicStrategy(), this, i));
        }
        currentBid = new Bid();
        currentBid.setRank(Card.Rank.TWO);
        currentPlayer = 0;
        cheatCounter = 0;
        cheatCorrect = 0;
    }

    @Override
    public boolean playTurn() {
        //Ask player for a play
        currentBid = players[currentPlayer].playHand(currentBid);
        //Add hand played to discard pile
        discards.addHand(currentBid.getHand());

        //Offer all other players the chance to call cheat. Code added to
        // randomise the order of players each time.
        boolean cheat = false;
        Random rand = new Random();
        int[] randomPlayer = new int[nosPlayers];
        for (int i = 0; i < nosPlayers; i++) {
            randomPlayer[i] = i;
        }

        // Randomise the new array.
        for (int i = 0; i < nosPlayers; i++) {
            int swapIndex = rand.nextInt(nosPlayers);
            int temp = randomPlayer[i];
            randomPlayer[i] = randomPlayer[swapIndex];
            randomPlayer[swapIndex] = temp;
        }

        for (int i = 0; i < nosPlayers && ! cheat; i++) {
            if (randomPlayer[i] != currentPlayer) {
                cheat = players[randomPlayer[i]].callCheat(currentBid);
                if (cheat) {
                    this.cheatCounter++;
                    System.out.println("Player called cheat by Player " +
                            (randomPlayer[i] + 1) + ". Cheat has been called " +
                            this.cheatCounter + " times this game.");

                    if (isCheat(currentBid)) {
                        //CHEAT CALLED CORRECTLY
                        //Give the discard pile of cards to currentPlayer who
                        // then has to play again
                        this.cheatCorrect++;
                        players[currentPlayer].addHand(discards);
                        System.out.println("Player cheats! Cheat has been " +
                                "called correctly " + this.cheatCorrect +
                                " times this game.");
                        System.out.println("Adding cards to player " +
                                (currentPlayer + 1) + "'s hand.");
                    }
                    else {
                        //CHEAT CALLED INCORRECTLY
                        //Give cards to caller i who is new currentPlayer
                        System.out.println("Player Honest. Cheat has been " +
                                "called incorrectly " + (this.cheatCounter -
                                this.cheatCorrect) + " times this game.");
                        currentPlayer = randomPlayer[i];
                        players[currentPlayer].addHand(discards);
                        System.out.println("Adding cards to player " +
                                (currentPlayer + 1) + "'s hand.");
                    }
                    // If cheat is called, current bid reset to an empty bid
                    // with a random rank to prevent infinite loops in 2-player.
                    currentBid = new Bid();
                    currentBid.setRank(Card.Rank.getRandom());
                    //Discards now reset to empty
                    discards = new Hand();
                }
            }
        }
        if (! cheat) {
            //Go to the next player
            System.out.println("\nNo Cheat Called");
            currentPlayer = (currentPlayer + 1) % nosPlayers;
        }
        return true;
    }

    public int winner() {
        for (int i = 0; i < nosPlayers; i++) {
            if (players[i].cardsLeft() < 3) {
                System.out.println("Player " + (i + 1) + " has " +
                        players[i].cardsLeft() + " cards remaining.");
            }
            if (players[i].cardsLeft() == 0) {return i;}
        }
        return - 1;

    }

    public void initialise() {
        Random rand = new Random();
        //Create Deck of cards
        Deck d = new Deck();
        d.shuffle();
        //Deal cards to players
        Iterator<Card> it = d.iterator();
        int count = 0;
        while (it.hasNext()) {
            players[count % nosPlayers].addCard(it.next());
            it.remove();
            count++;
        }
        //Initialise Discards
        discards = new Hand();
        //Choose first player
        currentPlayer = rand.nextInt(nosPlayers); // Random player starts.
        currentBid = new Bid();
        currentBid.setRank(Card.Rank.getRandom()); // Random starting card.
    }

    private void playGame() {
        initialise();
        int turnNum = 0;
        String input;
        Scanner in = new Scanner(System.in);

        // Retrieve and set the strategies for each player using factory.
        int i = 0;
        System.out.println("[Basic] AI\n[Thinker] AI\n[Master] AI\n[Human] " +
                "Player\n[Random] Non-Human Opponent\n");
        while (i < nosPlayers) {
            System.out.println("What type of strategy is player " + (i + 1) +
                    " of " + (nosPlayers) + " going to use?");
            input = in.nextLine();
            boolean cont = StrategyFactory.set(players[i], input);
            if (cont) {i++;}
        }
        System.out.println("Players set, shuffling positions...\n");

        // Main game loop.
        boolean finished = false;
        while (! finished) {
            //Play a hand
            System.out.println("\nCheat turn for player " +
                    (currentPlayer + 1));
            playTurn();
            turnNum++;
            System.out.println("Turn " + turnNum + " Complete.\n" +
                    "<Press 'Enter' to continue or 'Q' to quit>");
            String str = in.nextLine();
            if (str.equals("Q") || str.equals("q") || str.equals("quit")) {
                finished = true;
            }
            int w = winner();
            if (w >= 0) { // (changed this from w > 0)
                System.out.println("The Winner is Player " + (w + 1));
                finished = true;
            }
        }
    }

    private static boolean isCheat(Bid b) {
        for (Card c : b.getHand()) {
            if (c.getRank() != b.r) { return true; }
        }
        return false;
    }

    /* Extra class to ensure inputs entered by the user are actually numbers.
     * Also checks input for lower and upper bounds to save repeating code.
     */
    private static int getInt(Scanner in, int lowerBound, int upperBound) {
        boolean valid;
        int temp;
        do {
            valid = true;
            // Ensure the number is an integer first.
            while (! in.hasNextInt()) {
                System.out.println("Please enter a number.");
                in.next();
            }
            temp = in.nextInt();

            // Ensure taken number is within bounds.
            if (temp < lowerBound) {
                System.out.println("Number too small, please try again.");
                valid = false;
            }
            else if (temp > upperBound) {
                System.out.println("Number too large, please try again.");
                valid = false;
            }
        }while (! valid);
        return temp;
    }

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int players; // Stores the current integer input.
        boolean repeat;
        System.out.println("Welcome to Cheat. How many total players will " +
                "this game contain (2-8, 5 recommended)?");
        players = getInt(in, 2, 8);

        BasicCheat cheat = new BasicCheat(players);
        cheat.playGame();
    }
}
