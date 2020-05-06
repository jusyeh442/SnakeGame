Spampede README.txt

Description:

   ** 
      To access the game file, click on src --> com/gradescope/spampede --> SpampedeBrain.java and run SpampedeBrain.java
      
      Click "New Game" to start a new game. Use the keys "i" to move up, "k" to move down, 
      "l" to move right, and "j" to move left. Use the key "a" to turn the snake into AI mode,
      and it will play the game on its own. The objective is to eat as much spam (yellow squares) 
      and grow as long as possible without hitting a wall or eating yourself.
   **

Known bugs:  

   ** 
      There are no known bugs that I have discovered in my programs.
   **

Extra features that were added:

   ** 
      I added a magenta "hyperspace" square that "sends" cells to another spot on the board.
      Hyperspace squares come in pairs; the snake enters through one and exits through another.
      The snake can also enter hyperspace squares while in AI_MODE, as I changed the code in
      getNextCellFromBFS() to account for the value of entering a hyperspace square (if going
      through hyperspace brings you closer to a spam, then do so).
   ** 
