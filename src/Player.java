import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

import com.sun.org.apache.xerces.internal.util.Status;

class Settings {
	public static boolean DEBUG = false;

	public static final int NB_GENERATION = 50;

	public static final int MY_POPULATION_SIZE = 20;

	public static final int HIS_POPULATION_SIZE = 20;

	public static final int MY_MAX_SELECTION = 3;

	public static final int HIS_MAX_SELECTION = 3;

	public static final int MAX_MY_CHILDREN = 2;

	public static final int MAX_HIS_CHILDREN = 2;

	// %
	public static final int MUTATION_PROBABILITY = 10;

	// %
	public static final int MUTATION_PROBABILITY_BY_GENE = 30;

	public static final int MAX_MUTATION = 3;

	public static final int MIN_MUTATION = 1;

	public static final int POINTS_BY_VICTORY = 3;

	public static final int POINTS_BY_EQUALITY = 1;

	public static final int POINTS_BY_LOOSE = 0;

	// %
	public static final int PLAY_AGAINS_PROBABILITY = 20;
}

enum State {
	ME, HIM, NOBODY;
}

class Board {
	private static State winner = State.NOBODY;
	
	private static State winnerSmall = State.NOBODY;
	
	private static State initialWinnerSmall = State.NOBODY;

	private static final int FULL_GRID_SIZE = 9;

	private static final int MAIN_GRID_SIZE = 3;

	// [row][col]
	private static State[][] initialGrid = new State[FULL_GRID_SIZE][FULL_GRID_SIZE];

	private static State[][] grid = new State[FULL_GRID_SIZE][FULL_GRID_SIZE];

	private static State[][] initialMainGrid = new State[MAIN_GRID_SIZE][MAIN_GRID_SIZE];

	private static State[][] mainGrid = new State[MAIN_GRID_SIZE][MAIN_GRID_SIZE];

	private static Move initialLastMovePlayed = new Move(-1,-1);
	
	private static Move lastMovePlayed = new Move(-1,-1);

	private static State lastPlayerPlayed = State.NOBODY;
	
	public static void initBoard() {
		for (int i = 0; i < FULL_GRID_SIZE; i++) {
			for (int j = 0; j < FULL_GRID_SIZE; j++) {
				initialGrid[i][j] = State.NOBODY;
				grid[i][j] = State.NOBODY;
			}
		}
	}

	public static void playByMeInitial(Move move) {
		if (move.row != -1)
			initialGrid[move.row][move.col] = State.ME;
		updateInitialMainGrid();
		updateInitialWinnerOnSmallGrid();
	}

	public static void playByHimInitial(Move move) {
		initialLastMovePlayed.setMove(move.row, move.col);
		if (move.row != -1)
			initialGrid[move.row][move.col] = State.HIM;
		updateInitialMainGrid();
		updateInitialWinnerOnSmallGrid();
	}

	public static State SimulateGame(Individual individual1, Individual individual2, int validActionCount) {
		resetBoard();
		int individual1Offset = 0;
		int individual2Offset = 0;
		
		//System.err.println(individual1.getMovesString());
		//System.err.println(individual2.getMovesString());

		for (int i = 0; i < validActionCount && individual1Offset + individual2Offset < validActionCount; i++) {
			Move moveIndividual1 = individual1.getMove(individual1Offset);

			while (!isEmptyTule(moveIndividual1)) {
				individual1Offset++;

				if (individual1Offset + individual2Offset >= validActionCount)
					break;

				moveIndividual1 = individual1.getMove(individual1Offset);
			}
			playByMe(moveIndividual1);
			individual1Offset++;

			//System.err.println(initialWinnerSmall + " " + winnerSmall);
			if (winner == State.ME || 
					(initialWinnerSmall == State.NOBODY && winnerSmall == State.ME)) {
				System.err.println("WIN");
				return State.ME;
			}

			Move moveIndividual2 = individual2.getMove(individual2Offset);

			while (!isEmptyTule(moveIndividual2)) {
				individual2Offset++;

				if (individual1Offset + individual2Offset >= validActionCount)
					break;

				moveIndividual2 = individual2.getMove(individual2Offset);
			}
			playByHim(moveIndividual2);
			individual2Offset++;
			
			if (winner == State.HIM || 
					(initialWinnerSmall == State.NOBODY && winnerSmall == State.HIM)) {
				System.err.println("LOOSE");
				return State.HIM;
			}
		}

		System.err.println("NOBODY");
		return State.NOBODY;
	}

	private static boolean isEmptyTule(Move move) {
		return grid[move.row][move.col] == State.NOBODY;
	}

	private static void playByMe(Move move) {
		grid[move.row][move.col] = State.ME;
		updateMainGrid();
		updateWinner();
		updateWinnerOnSmallGrid();
	}

	private static void playByHim(Move move) {
		grid[move.row][move.col] = State.HIM;
		updateMainGrid();
		updateWinner();
		updateWinnerOnSmallGrid();
	}

	private static void updateInitialMainGrid() {
		for (int i = 0; i < MAIN_GRID_SIZE; i++) {
			for (int j = 0; j < MAIN_GRID_SIZE; j++) {
				if (initialMainGrid[i][j] != State.NOBODY) {
					initialMainGrid[i][j] = getMainTuleStatue(i, j, initialGrid);
				}
			}
		}
	}
	
	private static void updateMainGrid() {
		for (int i = 0; i < MAIN_GRID_SIZE; i++) {
			for (int j = 0; j < MAIN_GRID_SIZE; j++) {
				if (mainGrid[i][j] != State.NOBODY) {
					mainGrid[i][j] = getMainTuleStatue(i, j, grid);
				}
			}
		}
	}

	private static State getMainTuleStatue(int row, int col, State[][] gridChosen) {
		if(row == -1)
			return State.NOBODY;
			
		for (int i = 0; i < 3; i++) {
			// check rows

			if (gridChosen[row / MAIN_GRID_SIZE + i][col / MAIN_GRID_SIZE + 0] != State.NOBODY
					&& gridChosen[row / MAIN_GRID_SIZE + i][col / MAIN_GRID_SIZE
							+ 0] == gridChosen[row / MAIN_GRID_SIZE + i][col / MAIN_GRID_SIZE + 1]
					&& gridChosen[row / MAIN_GRID_SIZE + i][col / MAIN_GRID_SIZE
							+ 0] == gridChosen[row / MAIN_GRID_SIZE + i][col / MAIN_GRID_SIZE + 2]) {
				return gridChosen[row / MAIN_GRID_SIZE + i][col / MAIN_GRID_SIZE + 0];
			}

			// check cols
			if (gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE + i] != State.NOBODY
					&& gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE
							+ i] == gridChosen[row / MAIN_GRID_SIZE + 1][col / MAIN_GRID_SIZE + i]
					&& gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE
							+ i] == gridChosen[row / MAIN_GRID_SIZE + 2][col / MAIN_GRID_SIZE + i]) {
				return gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE + i];
			}
		}

		// check diags
		if (gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE + 0] != State.NOBODY
				&& gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE
						+ 0] == gridChosen[row / MAIN_GRID_SIZE + 1][col / MAIN_GRID_SIZE + 1]
				&& gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE
						+ 0] == gridChosen[row / MAIN_GRID_SIZE + 2][col / MAIN_GRID_SIZE + 2]) {
			return gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE + 0];
		}
		if (gridChosen[row / MAIN_GRID_SIZE + 2][col / MAIN_GRID_SIZE + 0] != State.NOBODY
				&& gridChosen[row / MAIN_GRID_SIZE + 2][col / MAIN_GRID_SIZE
						+ 0] == gridChosen[row / MAIN_GRID_SIZE + 1][col / MAIN_GRID_SIZE + 1]
				&& gridChosen[row / MAIN_GRID_SIZE + 2][col / MAIN_GRID_SIZE
						+ 0] == gridChosen[row / MAIN_GRID_SIZE + 0][col / MAIN_GRID_SIZE + 2]) {
			return gridChosen[row / MAIN_GRID_SIZE + 2][col / MAIN_GRID_SIZE + 0];
		}
		
		return State.NOBODY;
	}

	private static void updateWinnerOnSmallGrid() {
		//TODO look at it ! 
		winnerSmall = getMainTuleStatue(initialLastMovePlayed.row, initialLastMovePlayed.col, grid);
	}
	
	private static void updateInitialWinnerOnSmallGrid() {
		winnerSmall = getMainTuleStatue(initialLastMovePlayed.row, initialLastMovePlayed.col, initialGrid);
	}
	
	private static void updateWinner() {
		for (int i = 0; i < 3; i++) {
			// check rows
			if (mainGrid[i][0] != State.NOBODY && mainGrid[i][0] == mainGrid[i][1]
					&& mainGrid[i][0] == mainGrid[i][2]) {
				winner = mainGrid[i][0];
			}

			// check cols
			if (mainGrid[0][i] != State.NOBODY && mainGrid[0][i] == mainGrid[1][i]
					&& mainGrid[0][i] == mainGrid[2][i]) {
				winner = mainGrid[0][i];
			}
		}

		// check diags
		if (mainGrid[0][0] != State.NOBODY && mainGrid[0][0] == mainGrid[1][1] && mainGrid[0][0] == mainGrid[2][2]) {
			winner = mainGrid[0][0];
		}
		if (mainGrid[2][0] != State.NOBODY && mainGrid[2][0] == mainGrid[1][1] && mainGrid[2][0] == mainGrid[0][2]) {
			winner = mainGrid[2][0];
		}
	}

	private static void resetBoard() {
		winner = State.NOBODY;
		winnerSmall = State.NOBODY;
		lastMovePlayed.setMove(-1, -1);
		lastPlayerPlayed = State.NOBODY;

		for (int i = 0; i < FULL_GRID_SIZE; i++) {
			for (int j = 0; j < FULL_GRID_SIZE; j++) {
				grid[i][j] = initialGrid[i][j];
			}
		}

		for (int i = 0; i < MAIN_GRID_SIZE; i++) {
			for (int j = 0; j < MAIN_GRID_SIZE; j++) {
				mainGrid[i][j] = initialMainGrid[i][j];
			}
		}
	}
}

class Couple {
	private Individual individual1;

	private Individual individual2;

	public Couple(Individual individual1, Individual individual2) {
		this.individual1 = individual1;
		this.individual2 = individual2;
	}

	public Individual getSon() {
		return new Individual(individual1, individual2);
	}
}

class Move {
	public int row;

	public int col;

	public Move(int row, int col) {
		this.row = row;
		this.col = col;
	}
	
	public void setMove(int row, int col) {
		this.row = row;
		this.col = col;
	}

	@Override
	public String toString() {
		return row + " " + col;
	}
}

class PositionnedMove {
	private Move move;
	private int rank;

	public PositionnedMove(Move move, int rank) {
		this.move = move;
		this.rank = rank;
	}

	public Move getMove() {
		return move;
	}

	public int getRank() {
		return rank;
	}
}

class PositionedMoveComparator implements Comparator<PositionnedMove> {

	@Override
	public int compare(PositionnedMove o1, PositionnedMove o2) {
		if (o1.getRank() > o2.getRank()) {
			return 1;
		} else if (o1.getRank() < o2.getRank()) {
			return -1;
		} else {
			return 0;
		}
	}
}

class Population {

	private int validActionCount = 0;

	private List<Individual> myIndividuals = new ArrayList<>(Settings.MY_POPULATION_SIZE);

	private List<Individual> hisIndividuals = new ArrayList<>(Settings.MY_POPULATION_SIZE);

	private IndividualComparator comparator = new IndividualComparator();

	private class IndividualComparator implements Comparator<Individual> {
		@Override
		public int compare(Individual arg0, Individual arg1) {
			if (arg0.getFitnessScore() > arg1.getFitnessScore()) {
				return -1;
			} else if (arg0.getFitnessScore() < arg1.getFitnessScore()) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public Population(int validActionCount, List<Move> moves) {
		this.validActionCount = validActionCount;

		for (int i = 0; i < Settings.MY_POPULATION_SIZE; i++) {
			myIndividuals.add(new Individual(validActionCount, moves));
		}

		for (int i = 0; i < Settings.HIS_POPULATION_SIZE; i++) {
			hisIndividuals.add(new Individual(validActionCount, moves));
		}
	}

	public void resetScores() {
		for (Individual individual : myIndividuals) {
			individual.resetScore();
		}

		for (Individual individual : hisIndividuals) {
			individual.resetScore();
		}
	}

	public void calculateScores() {
		for (Individual individual : myIndividuals) {
			calculateScoreByMines(individual, hisIndividuals);
		}
	}

	public void calculateScoreByMines(Individual myIndividual, List<Individual> opponentIndividuals) {

		for (Individual individual : opponentIndividuals) {
			if (getPlayAgainsDecision()) {
				State state = Board.SimulateGame(myIndividual, individual, validActionCount);
				if (state == State.ME) {
					//System.err.println("WIN");
					myIndividual.increaseScore(Settings.POINTS_BY_VICTORY);
					individual.increaseScore(Settings.POINTS_BY_LOOSE);
				} else if (state == State.NOBODY) {
					//System.err.println("NOBODY");
					myIndividual.increaseScore(Settings.POINTS_BY_EQUALITY);
					individual.increaseScore(Settings.POINTS_BY_EQUALITY);
				} else {
					//System.err.println("LOOSE");
					myIndividual.increaseScore(Settings.POINTS_BY_LOOSE);
					individual.increaseScore(Settings.POINTS_BY_VICTORY);
				}
			}
		}
	}

	private static boolean getPlayAgainsDecision() {
		return 0 + (int) (Math.random() * ((100 - 0) + 1)) < Settings.PLAY_AGAINS_PROBABILITY;
	}

	public Move getMyBest() {
		sortIndividuals();
		return myIndividuals.get(0).getMove(0);
	}

	public void sortIndividuals() {
		calculateScores();
		Collections.sort(myIndividuals, comparator);
		Collections.sort(hisIndividuals, comparator);
	}

	public void updateIndividuals(List<Individual> myNewIndividuals, List<Individual> hisNewIndividuals) {
		myIndividuals = myIndividuals.subList(0, myIndividuals.size() - Settings.MAX_MY_CHILDREN);
		myIndividuals.addAll(myNewIndividuals);

		hisIndividuals = hisIndividuals.subList(0, hisIndividuals.size() - Settings.MAX_HIS_CHILDREN);
		hisIndividuals.addAll(hisNewIndividuals);
	}

	public List<Individual> getNewGenerationIndividuals(List<Couple> couples) {
		List<Individual> newIndividuals = new ArrayList<>();

		for (Couple couple : couples) {
			newIndividuals.add(couple.getSon());
		}

		return newIndividuals;
	}

	public List<Couple> getCouples(List<Individual> individualsSelected) {
		List<Couple> couples = new ArrayList<>();

		for (int i = 0; i < Settings.MY_MAX_SELECTION; i++) {
			for (int j = 0; j < Settings.MY_MAX_SELECTION; j++) {
				if (i != j) {
					Couple couple = new Couple(individualsSelected.get(i), individualsSelected.get(j));
					couples.add(couple);
				}
			}
		}

		Collections.shuffle(couples);

		if (couples.size() < Settings.MAX_MY_CHILDREN) {
			return couples;
		}

		return couples.subList(0, Settings.MAX_MY_CHILDREN);
	}

	public List<Individual> mySelectionIndividuals() {
		if (myIndividuals.size() < Settings.MY_MAX_SELECTION)
			return myIndividuals;

		return myIndividuals.subList(0, Settings.MY_MAX_SELECTION);
	}

	public List<Individual> hisSelectionIndividuals() {
		if (hisIndividuals.size() < Settings.HIS_MAX_SELECTION)
			return hisIndividuals;

		return hisIndividuals.subList(0, Settings.HIS_MAX_SELECTION);
	}

	public void mutation() {
		for (Individual individual : myIndividuals) {
			individual.mutation();
		}

		for (Individual individual : hisIndividuals) {
			individual.mutation();
		}
	}

	@Override
	public String toString() {
		String result = "MY INDIVIDUALS\n";

		for (Individual individual : myIndividuals) {
			result += myIndividuals.indexOf(individual) + " ---> " + individual.toString() + "\n";
		}

		result += "HIS INDIVIDUALS\n";
		for (Individual individual : hisIndividuals) {
			result += hisIndividuals.indexOf(individual) + " ---> " + individual.toString() + "\n";
		}

		return result;
	}

}

class Individual {
	private List<Move> initialMoves = new ArrayList<>();
	private List<Move> moves = new ArrayList<>();
	private int score = 0;
	private int validActionCount = 0;

	private static PositionedMoveComparator positionedMoveComparator = new PositionedMoveComparator();

	public Individual(int validActionCount, List<Move> moves) {
		this.validActionCount = validActionCount;

		this.moves.addAll(moves);
		Collections.shuffle(this.moves);

		this.initialMoves.addAll(moves);
	}

	public Individual(Individual individual1, Individual individual2) {
		int crossoverPoint = getRandomCrossoverPoint();
		this.validActionCount = individual1.getValidActionCount();

		for (int i = 0; i < validActionCount; i++) {
			if (i < crossoverPoint) {
				Move move = individual1.getMove(i);
				if (this.moves.contains(move)) {
					this.moves.add(null);
				} else {
					this.moves.add(individual1.getMove(i));
				}
			} else {
				Move move = individual2.getMove(i);
				if (this.moves.contains(move)) {
					this.moves.add(null);
				} else {
					this.moves.add(individual2.getMove(i));
				}
			}
		}

		this.initialMoves.addAll(individual1.initialMoves);

		this.solveInvalidSolution(individual1, individual2);
	}

	private void solveInvalidSolution(Individual individual1, Individual individual2) {
		List<PositionnedMove> notUsedMoves = new ArrayList<>();

		for (Move move : this.initialMoves) {
			if (!this.moves.contains(move)) {
				int rank = individual1.getRank(move) + individual2.getRank(move);
				notUsedMoves.add(new PositionnedMove(move, rank));
			}
		}

		Collections.sort(notUsedMoves, positionedMoveComparator);

		for (PositionnedMove positionedMove : notUsedMoves) {
			int index = moves.indexOf(null);
			this.moves.set(index, positionedMove.getMove());
		}

		this.initialMoves.clear();
		this.initialMoves.addAll(this.moves);
	}

	public int getValidActionCount() {
		return validActionCount;
	}

	public Move getMove(int i) {
		return this.moves.get(i);
	}

	public int getRank(Move move) {
		return this.moves.indexOf(move);
	}

	public void mutation() {
		if (randomDecisionMutation()) {
			for (int i = 0; i < getNbMutation(); i++) {
				if (randomMutationByGene()) {
					int randomGene1 = getRandomGene();
					int randomGene2 = getRandomGene();

					swapGene(randomGene1, randomGene2);
				}
			}
		}
	}

	private void swapGene(int index1, int index2) {
		Move firstMove = moves.get(index1);
		moves.set(index1, moves.get(index2));
		moves.set(index2, firstMove);
	}

	private int getNbMutation() {
		int maxMutation = Math.min(Settings.MAX_MUTATION, validActionCount);

		return Settings.MIN_MUTATION + (int) (Math.random() * ((maxMutation - Settings.MIN_MUTATION) + 1));
	}

	private int getRandomGene() {
		return 0 + (int) (Math.random() * ((validActionCount - 1) + 1));
	}

	private boolean randomMutationByGene() {
		return 0 + (int) (Math.random() * ((100 - 0) + 1)) < Settings.MUTATION_PROBABILITY_BY_GENE;
	}

	private boolean randomDecisionMutation() {
		return 0 + (int) (Math.random() * ((100 - 0) + 1)) < Settings.MUTATION_PROBABILITY;
	}

	public int getFitnessScore() {
		return this.score;
	}

	public void resetScore() {
		this.score = 0;
	}

	public void increaseScore(int points) {
		this.score += points;
	}

	private int getRandomCrossoverPoint() {
		return 1 + (int) (Math.random() * ((validActionCount - 1) + 1));
	}

	public List<Move> getMoves() {
		return moves;
	}

	public String getMovesString() {
		return moves.toString();
	}

	@Override
	public String toString() {
		return "" + score;
	}
}

class Player {

	public static void main(String args[]) {
		Scanner in = new Scanner(System.in);
		List<Move> possibleMoves = new ArrayList<>();
		Board.initBoard();

		// game loop
		while (true) {
			int opponentRow = in.nextInt();
			int opponentCol = in.nextInt();

			possibleMoves.clear();

			Board.playByHimInitial(new Move(opponentRow, opponentCol));

			int validActionCount = in.nextInt();

			for (int i = 0; i < validActionCount; i++) {
				int row = in.nextInt();
				int col = in.nextInt();

				possibleMoves.add(new Move(row, col));
			}

			Population population = new Population(validActionCount, possibleMoves);

			for (int i = 0; i < Settings.NB_GENERATION; i++) {
				population.resetScores();
				population.sortIndividuals();

				if (Settings.DEBUG) {
					System.err.println("GENERATION " + i);
					System.err.println(population);
				}

				List<Individual> mySelection = population.mySelectionIndividuals();
				List<Couple> myCouples = population.getCouples(mySelection);
				List<Individual> myNewIndividuals = population.getNewGenerationIndividuals(myCouples);

				List<Individual> hisSelection = population.hisSelectionIndividuals();
				List<Couple> hisCouples = population.getCouples(hisSelection);
				List<Individual> hisNewIndividuals = population.getNewGenerationIndividuals(hisCouples);

				population.updateIndividuals(myNewIndividuals, hisNewIndividuals);
				population.mutation();
			}

			population.sortIndividuals();
			Move myMove = population.getMyBest();
			Board.playByMeInitial(myMove);

			System.out.println(myMove.toString());
		}
	}

}
