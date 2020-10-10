# What It Does #
This is a Java implementation of the card game [Cheat](https://en.wikipedia.org/wiki/Cheat_(game)). It includes several different computer strategies (difficulties) as well as the ability for humans to play. At startup you will be given the option to select how many players to include and if they're human or a computer strategy, then the game starts with a random player. The game then plays out according to the rules of Cheat and the last player remaining is declared the winner.

# What I Learned #
In order to build this project, I had to draw upon my previous knowledge of **Object Oriented Programming (OOP)** as well as build upon it with an understanding of **serialization**, **comparators**, **iterators** and **Lambdas** (which was used to select a comparator for use).

To build the different strategies, I was given a basic description of what each one should do (Besides MyStrategy) and turning that into code that is executed in the game. For MyStrategy, I was given free-reign as to what actions it performs as long as it was different from the other strategies and non-trivial. I decided to make my one keep track of as much information it could, as well as having a chance to cheat by looking at the discard pile. This strategy proved effective as when testing, two opponents using MyStrategy ("Master AI") would often play a game of over fifteen thousand rounds against each other until one got unlucky.

# Usage Notes #
Compile the src\com\question2 folder as a project in your chosen way. The question2 folder is all that is needed for the game to run correctly; the question1 folder just includes basic classes for forming a card, hand or deck (which are the same as those classes in the question2 folder). There are no command line arguments for this program.