package com.gradescope.spampede;

import java.awt.Color;
import java.lang.Math;
import java.util.LinkedList;

/**
 * SpampedeData - Representation of the Board. Outside of the model, no one knows
 * how the Board is represented. They can only access the methods provided by the
 * class.
 * 
 * @author CS60 instructors
 */
class SpampedeData {
	/** 
	 * The collection of all the BoardCells in the program.
	 * <p>
	 * All BoardCells needed by the program are created by the
	 * SpampedeData constructor, so you don't need to create any
	 * new BoardCells in your code; you'll just pass around (references to)
	 * existing cells, and change the contents of some of these cells.
	 */
	private BoardCell[][] boardCells2D;

	
	/** 
	 * The number of non-wall cells in the initial Board.
	 */
	private int freeSpots = 0;

	/**
	 * The current movement "mode" of the snake, i.e., whether it's headed
	 * in a particular direction or in AI mode.
	 */
	private SnakeMode currentMode = SnakeMode.GOING_EAST;
	
	/** 
	 * A list of (references to) cells that currently contain spam, 
	 * ordered from oldest (first) to youngest (last).
	 * 
	 */
	private LinkedList<BoardCell> spamCells = new LinkedList<BoardCell>();
	
	/**
	 * A list of (references to) the cells that contain the snake. 
	 * The head is the last element of the list. 
	 */
	private LinkedList<BoardCell> snakeCells = new LinkedList<BoardCell>();
	
	/**
	 * A list of (references to) the cells that contain hyperspace. 
	 */
	private LinkedList<BoardCell> hyperspaceCells = new LinkedList<BoardCell>();
	
	/**
	 * Whether the game is done.
	 */
	private boolean gameOver = false;

	/* -------------------------------------- */
	/* Constructor and initialization methods */
	/* -------------------------------------- */
	
	/**
	 * Constructor; creates a "Board" with walls on the boundary
	 * and open in the interior.
	 */
	public SpampedeData() {
		int height = Preferences.NUM_CELLS_TALL;
		int width = Preferences.NUM_CELLS_WIDE;
		this.boardCells2D = new BoardCell[height][width];

		// Place walls around the outside
		this.addWalls();

		// Fill the remaining cells not already filled!
		this.fillRemainingCells();
	}

	/**
	 * Adds WALL Boardcells around the edges of this.boardCells2DD.
	 */
	private void addWalls() {
		int height = this.getNumRows();
		int width = this.getNumColumns();

		// Add Left and Right Walls
		for (int row = 0; row < height; row++) {
			this.boardCells2D[row][0] = new BoardCell(row, 0, CellType.WALL);
			this.boardCells2D[row][width - 1] = new BoardCell(row, width - 1,
					CellType.WALL);
		}
		// Add top and bottom walls
		for (int column = 0; column < width; column++) {
			this.boardCells2D[0][column] = new BoardCell(0, column, CellType.WALL);
			this.boardCells2D[height - 1][column] = new BoardCell(height - 1,
					column, CellType.WALL);
		}
	}

	/** 
	 * Finishes filling this.boardCells2D with OPEN BoardCells 
	 * (and set this.freeSpots to the number of OPEN cells).
	 */
	private void fillRemainingCells() {
		int height = this.getNumRows();
		int width = this.getNumColumns();

		this.freeSpots = 0;
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				if (this.boardCells2D[row][column] == null) {
					this.boardCells2D[row][column] = 
							new BoardCell(row, column, CellType.OPEN);
					this.freeSpots++;
				}
			}
		}
	}

	/**
	 * Puts the snake in the upper-left corner of the walls, facing east.
	 */
	public void placeSnakeAtStartLocation() {
		BoardCell body = this.getCell(1, 1);
		BoardCell head = this.getCell(1, 2);
		this.snakeCells.addLast(body);
		this.snakeCells.addLast(head);
		head.becomeHead();
		body.becomeBody();
	}

	/* -------------------------------------------- */
	/* Methods to access information about the Board */
	/* -------------------------------------------- */

	/**
	 * @return Are we in AI mode?
	 */
	public boolean inAImode() {
		return this.currentMode == SnakeMode.AI_MODE;
	}

	/**
	 * @return the height of the Board (including walls) in cells.
	 */
	public int getNumRows() {
		return this.boardCells2D.length;
	}

	/**
	 * @return The width of the Board (including walls) in cells.
	 */
	public int getNumColumns() {
		return this.boardCells2D[0].length;
	}

	/**
	/* Access a cell at a particular location.
	 * <p>
	 * This should really be private. We make it public to allow
	 * our unit tests to use it, but it shouldn't be called
	 * from the SpampedeBrain or the SpampedeDisplay.
	 * 
	 * @param r  between 0 and this.getNumRows()-1 inclusive
	 * @param c  between 0 and this.getNumColumns()-1 inclusive
	 * @return cell c in row r.
	 */
	public BoardCell getCell(int r, int c) {
		if (r >= this.getNumRows() || c >= this.getNumColumns() || r < 0
				|| c < 0) {
			System.err.println("Trying to access cell outside of the Board:");
			System.err.println("row: " + r + " col: " + c);
			System.exit(0);
		}
		return this.boardCells2D[r][c];
	}

	/* -------------------- */
	/* Spam-related Methods */
	/* -------------------- */
	
	/**
	 * @return Is there zero spam?
	 */
	public boolean noSpam() {
		return this.spamCells.isEmpty();
	}

	/**
	 * Adds spam to an open spot.
	 */
	public void addSpam() {
		// Pick a random cell.
		int row    = (int) (this.getNumRows()    * Math.random());
		int column = (int) (this.getNumColumns() * Math.random());
		BoardCell cell = this.getCell(row, column);
		
		if (cell.isOpen()) { 
			// If the random cell is open, put spam there.
			cell.becomeSpam();
			spamCells.addLast(cell);
		} else {
			// If the random cell is occupied and the Board isn't already
			// too full of spam, try again to place spam. 
			double totalSize = this.getNumColumns() * this.getNumRows();
			double currentFreeSpots = this.freeSpots - this.snakeCells.size()
					- this.spamCells.size();
			double ratioFree = currentFreeSpots / totalSize;
			if (ratioFree < 0.2) {
				System.err.println("Not adding more spam");
			} else {
				addSpam();
			}
		}
	}

	/** 
	 * Deletes the oldest piece of un-eaten spam.
	 * <p>
	 * The function is not used in the given code, but it might be
	 * useful if you want to extend the game.
	 */
	@SuppressWarnings("unused")
	private void removeSpam() {
		if (!spamCells.isEmpty()) {
			spamCells.peekFirst().becomeOpen();
			spamCells.removeFirst();
		}
	}
	
	/* -------------------- */
	/* Hyperspace-related Methods */
	/* -------------------- */
	
	/**
	 * @return Is there zero hyperspace cells?
	 */
	public boolean noHyperspace() {
		return this.hyperspaceCells.isEmpty();
	}

	/**
	 * Adds hyperspace to an open spot.
	 */
	public void addHyperspace() {
		// Pick a random cell.
		int row    = (int) (this.getNumRows()    * Math.random());
		int column = (int) (this.getNumColumns() * Math.random());
		BoardCell cell = this.getCell(row, column);
		
		if (cell.isOpen()) { 
			// If the random cell is open, put hyperspace there.
			cell.becomeHyperspace();
			hyperspaceCells.addLast(cell);
		} else {
			// If the random cell is occupied and the Board isn't already
			// too full of hyperspace, try again to place hyperspace. 
			double totalSize = this.getNumColumns() * this.getNumRows();
			double currentFreeSpots = this.freeSpots - this.snakeCells.size()
					- this.hyperspaceCells.size();
			double ratioFree = currentFreeSpots / totalSize;
			if (ratioFree < 0.2) {
				System.err.println("Not adding more hyperspace");
			} else {
				addHyperspace();
			}
		}
	}
	
	/**
	 * @input One of the hyperspace cells in the pair
	 * @return The other hyperspace cell in that pair
	 */
	public BoardCell otherHyperspace(BoardCell oneHyperspace) {
		if (this.hyperspaceCells.getFirst() == oneHyperspace) {
			return this.hyperspaceCells.getLast();
		} else {
			return this.hyperspaceCells.getFirst();
		}
	}

	/* --------------------- */
	/* Snake movement methods */
	/* --------------------- */

	// methods here support SpampedeBrain's advanceSnake method
	
	public void snakeEatsSpam(BoardCell nextCell) {
		this.getSnakeHead().becomeBody();
		this.snakeCells.addLast(nextCell);
		this.snakeCells.getLast().becomeHead();
		this.spamCells.remove(nextCell);
	}
	
	public void snakeEatsHyperspace(BoardCell nextCell) {
		this.getSnakeHead().becomeBody();
		this.snakeCells.addLast(nextCell);
		this.snakeCells.getLast().becomeBody();
		this.snakeCells.addLast(otherHyperspace(nextCell));
		this.snakeCells.getLast().becomeHead();	
		this.hyperspaceCells.remove(otherHyperspace(nextCell));
		this.hyperspaceCells.remove(nextCell);
		this.getSnakeTail().becomeOpen();
		this.snakeCells.removeFirst();
	}
	
	public void snakeEatsNoSpam(BoardCell nextCell) {
		this.getSnakeHead().becomeBody();
		this.snakeCells.addLast(nextCell);
		this.snakeCells.getLast().becomeHead();
		this.getSnakeTail().becomeOpen();
		this.snakeCells.removeFirst();
	}
	
	/* -------------------------------------- */
	/* Methods to support movement without AI */
	/* -------------------------------------- */

	/**
	 * @return the cell to the north of the given cell,
	 *         which must not be on the boundary.
	 */
	public BoardCell getNorthNeighbor(BoardCell cell) {
		return this.getCell(cell.getRow() - 1, cell.getColumn());
	}

	/**
	 * @return the cell to the south of the given cell,
	 *         which must not be on the boundary.
	 */	
	public BoardCell getSouthNeighbor(BoardCell cell) {
		return this.getCell(cell.getRow() + 1, cell.getColumn());
	}

	/**
	 * @return the cell to the east of the given cell,
	 *         which must not be on the boundary.
	 */	
	public BoardCell getEastNeighbor(BoardCell cell) {
		return this.getCell(cell.getRow(), cell.getColumn() + 1);
	}

	/** 
	 * @return the cell to the west of the given cell,
	 *         which must not be on the boundary.
	 */
	public BoardCell getWestNeighbor(BoardCell cell) {
		return this.getCell(cell.getRow(), cell.getColumn() - 1);
	}
	
	/** 
	 * @return the cell to the north of the snake's head
	 */	
	public BoardCell getNorthNeighbor() {
		return this.getNorthNeighbor(this.getSnakeHead());
	}

	/** 
	 * @return the cell to the south of the snake's head
	 */	
	public BoardCell getSouthNeighbor() {
		return this.getSouthNeighbor(this.getSnakeHead());
	}

	/** 
	 * @return the cell to the east of the snake's head
	 */	
	public BoardCell getEastNeighbor() {
		return this.getEastNeighbor(this.getSnakeHead());
	}

	/** 
	 * @return the cell to the west of the snake's head
	 */	
	public BoardCell getWestNeighbor() {
		return this.getWestNeighbor(this.getSnakeHead());
	}

	/**
	 * @return a cell North, South, East or West of the snake head 
	 *         based upon the current direction of travel (this.currentMode).
	 *         (We won't call this function if we're in AI_MODE, though 
	 *         Java wants this function to return a value even then.)
	 */ 
	public BoardCell getNextCellInDir() {
		if (this.currentMode == SnakeMode.GOING_NORTH) {
			return this.getNorthNeighbor();
		} else if (this.currentMode == SnakeMode.GOING_EAST) {
			return this.getEastNeighbor();
		} else if (this.currentMode == SnakeMode.GOING_SOUTH) {
			return this.getSouthNeighbor();
		} else {
			return this.getWestNeighbor();
		}
	}

	/* -------------------------------------------------- */
	/* Public methods to get all or one (random) neighbor */
	/* -------------------------------------------------- */
	
	/**
	 * @return an array of the four neighbors of the given cell
	 *         (suitable for iterating through with a for-each loop).
	 */
	public BoardCell[] getNeighbors(BoardCell center) {
		BoardCell[] neighborsArray = { getNorthNeighbor(center),
				                      getSouthNeighbor(center), 
				                      getEastNeighbor(center),
				                      getWestNeighbor(center) };
		return neighborsArray;
	}

	/** 
	 * @return an open neighbor of the given cell
	 *         (or some other neighbor if there are no open neighbors)
	 */
	public BoardCell getRandomNeighboringCell(BoardCell start) {
		BoardCell[] neighborsArray = getNeighbors(start);
		for (BoardCell mc : neighborsArray) {
			if (mc.isOpen()) {
				return mc;
			}
		}
		// If we didn't find an open space, just return the first neighbor.
		return neighborsArray[0];
	}

	/* ----------------------------------------- */
    /* Methods to set the snake's (movement) mode */
	/* ----------------------------------------- */

	/**
	 * Makes the snake head north.
	 */
	public void setDirectionNorth() {
		this.currentMode = SnakeMode.GOING_NORTH;
	}

	/**
	 * Makes the snake head south.
	 */
	public void setDirectionSouth() {
		this.currentMode = SnakeMode.GOING_SOUTH;
	}

	/**
	 * Makes the snake head east.
	 */
	public void setDirectionEast() {
		this.currentMode = SnakeMode.GOING_EAST;
	}

	/**
	 * Makes the snake head west.
	 */
	public void setDirectionWest() {
		this.currentMode = SnakeMode.GOING_WEST;
	}

	/**
	 * Makes the snake switch to AI mode.
	 */
	public void setMode_AI() {
		this.currentMode = SnakeMode.AI_MODE;
	}

	/**
	 * Picks an initial movement mode for the snake.
	 */
	public void setStartDirection() {
		this.setDirectionEast();
	}

	/* ------------------- */
	/* snake Access Methods */
	/* ------------------- */
	
	/**
	 * @return the cell containing the snake's head
	 */
	public BoardCell getSnakeHead() {
		return this.snakeCells.peekLast();
	}

	/**
	 * @return the cell containing the snake's tail
	 */
	public BoardCell getSnakeTail() {
		return this.snakeCells.peekFirst();
	}
	
	/**
	 * @return the snake body cell adjacent to the head
	 */
	public BoardCell getSnakeNeck() {
		int lastSnakeCellIndex = this.snakeCells.size() - 1;
		return this.snakeCells.get(lastSnakeCellIndex - 1);
	}

	/* ------------------------------ */
	/* Helper method used by the view */
	/* ------------------------------ */
	
	/**
	 * @param r  between 0 and this.getNumRows()-1 inclusive
	 * @param c  between 0 and this.getNumColumns()-1 inclusive
	 * @return The color of cell c in row r.
	 */
	public Color getCellColor(int row, int col) {
		BoardCell cell = getCell(row, col);
		return cell.getCellColor();
	}

	/* ---------------------------- */
	/* Helper method(s) for reverse */
	/* ---------------------------- */

	public void reverseHelper() {
		// Step 1: unlabel the head
		this.getSnakeHead().becomeBody();
		
		// Step 2: reverse the body parts
		LinkedList<BoardCell> reverseSnakeCells = new LinkedList<BoardCell>();
		for (BoardCell currentCell : this.snakeCells) {
			reverseSnakeCells.addFirst(currentCell);
		}
		this.snakeCells = reverseSnakeCells;

		// Step 3: relabel the head
		this.snakeCells.getLast().becomeHead();
		
		// Step 4: calculate the new direction after reversing!	
		if (getNorthNeighbor(this.getSnakeNeck()).equals(this.getSnakeHead())) {
			this.currentMode = SnakeMode.GOING_NORTH;
		} else if (getEastNeighbor(this.getSnakeNeck()).equals(this.getSnakeHead())) {
			this.currentMode = SnakeMode.GOING_EAST;
		} else if (getSouthNeighbor(this.getSnakeNeck()).equals(this.getSnakeHead())) {
			this.currentMode = SnakeMode.GOING_SOUTH;
		} else {
			this.currentMode = SnakeMode.GOING_WEST;
		}
	}

	/* ------------------------------------- */
	/* Methods to reset the model for search */
	/* ------------------------------------- */

	/**
	 * Clears the search-related fields in all the cells,
	 * in preparation for a new breadth-first search. 
	 */
	public void resetCellsForNextSearch() {
		for (BoardCell[] row : this.boardCells2D) {
			for (BoardCell cell : row) {
				cell.clear_RestartSearch();
			}
		}
	}

	/* ---------------- */
	/* Game-Over Status */
	/* ---------------- */
	
	/** 
	 * Sets the game-over flag.
	 */
	public void setGameOver() {
		this.gameOver = true;
	}

	/** 
	 * @return Should we display the game-over message?
	 */
	public boolean getGameOver() {
		return this.gameOver;
	}

	/* ----------------------------------------------------- */
	/* Testing Infrastructure - You don't need these methods */
	/* ----------------------------------------------------- */

	// Constructor used exclusively for testing!
	public SpampedeData(TestGame gameNum) {
		// Want pictures of the test boards?
		// http://tinyurl.com/spampedeTestBoards
		this.boardCells2D = new BoardCell[6][6];
		this.addWalls();
		this.fillRemainingCells();
		if (gameNum.snakeAtStart()) {
			this.testing_snakeAtStartLocation(gameNum);
			this.setDirectionEast();
		} else {
			this.testing_snakeNotAtStartLocation(gameNum);
		}

	}

	private void testing_snakeAtStartLocation(TestGame gameNum) {
		this.placeSnakeAtStartLocation();
		if (gameNum == TestGame.G1) {
			this.getCell(1, 3).becomeSpam();
		} else if (gameNum == TestGame.G2) {
			this.getCell(2, 2).becomeSpam();
		} else if (gameNum == TestGame.G3) {
			this.getCell(1, 4).becomeSpam();
		} else if (gameNum == TestGame.G4) {
			this.getCell(2, 1).becomeSpam();
		} else if (gameNum == TestGame.G5) {
			this.getCell(4, 1).becomeSpam();
		} else if (gameNum == TestGame.G6) {
			this.getCell(1, 3).becomeSpam();
			this.getCell(3, 1).becomeSpam();
		} else if (gameNum == TestGame.G7) {
			this.getCell(2, 2).becomeSpam();
			this.getCell(1, 4).becomeSpam();
		} else if (gameNum == TestGame.G8) {
			this.getCell(1, 4).becomeSpam();
			this.getCell(4, 2).becomeSpam();
		} else if (gameNum == TestGame.G9) {
			this.getCell(2, 1).becomeSpam();
			this.getCell(2, 4).becomeSpam();
		} else if (gameNum == TestGame.G10) {
			this.getCell(4, 1).becomeSpam();
			this.getCell(4, 4).becomeSpam();
		} else if (gameNum == TestGame.G11) {
			// No spam :)
		}
		// Add all spam to the spam cells
		int height = this.getNumRows();
		int width = this.getNumColumns();
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				BoardCell cell = this.getCell(row, column);
				if (cell.isSpam()) {
					this.spamCells.add(cell);
				}
			}
		}
	}

	private void testing_snakeNotAtStartLocation(TestGame gameNum) {
		if (gameNum == TestGame.G12) {
			BoardCell body2 = this.getCell(2, 3);
			BoardCell body1 = this.getCell(2, 2);
			BoardCell head = this.getCell(2, 1);
			this.snakeCells.add(body2);
			this.snakeCells.add(body1);
			this.snakeCells.add(head);
			head.becomeHead();
			body2.becomeBody();
			body1.becomeBody();
		} else if (gameNum == TestGame.G13) {
			BoardCell body2 = this.getCell(3, 2);
			BoardCell body1 = this.getCell(2, 2);
			BoardCell head = this.getCell(2, 1);
			this.snakeCells.add(body2);
			this.snakeCells.add(body1);
			this.snakeCells.add(head);
			head.becomeHead();
			body2.becomeBody();
			body1.becomeBody();
		} else if (gameNum == TestGame.G14) {
			BoardCell body2 = this.getCell(2, 2);
			BoardCell body1 = this.getCell(3, 2);
			BoardCell head = this.getCell(3, 1);
			this.snakeCells.add(body2);
			this.snakeCells.add(body1);
			this.snakeCells.add(head);
			head.becomeHead();
			body2.becomeBody();
			body1.becomeBody();
		} else if (gameNum == TestGame.G15) {
			BoardCell body2 = this.getCell(3, 2);
			BoardCell body1 = this.getCell(3, 3);
			BoardCell head = this.getCell(3, 4);
			this.snakeCells.add(body2);
			this.snakeCells.add(body1);
			this.snakeCells.add(head);
			head.becomeHead();
			body2.becomeBody();
			body1.becomeBody();
		}
	}

	public String toString() {
		String result = "";
		for (int r = 0; r < this.getNumRows(); r++) {
			for (int c = 0; c < this.getNumColumns(); c++) {
				BoardCell cell = this.getCell(r, c);
				result += cell.toStringType();
			}
			result += "\n";
		}
		return result;
	}
	
	public String toStringParents() {
		String result = "";
		for (int r = 0; r < this.getNumRows(); r++) {
			for (int c = 0; c < this.getNumColumns(); c++) {
				BoardCell cell = this.getCell(r, c);
				result += cell.toStringParent() + "\t";
			}
			result += "\n";
		}
		return result;
	}

}
