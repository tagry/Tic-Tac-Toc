import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

class Settings {
	public static boolean DEBUG = false;

	public static final int NB_GENERATION = 50;

	public static final int POPULATION_SIZE = 20;

	public static final int MAX_SELECTION = 3;

	public static final int MAX_CHILDREN = 2;

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

	private static final int GRID_SIZE = 3;

	// [row][col]
	private static State[][] initialGrid = new State[GRID_SIZE][GRID_SIZE];

	private static State[][] grid = new State[GRID_SIZE][GRID_SIZE];

	public static void initBoard() {
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				initialGrid[i][j] = State.NOBODY;
				grid[i][j] = State.NOBODY;
			}
		}
	}

	public static void playByMeInitial(Move move) {
		if (move.row != -1)
			initialGrid[move.row][move.col] = State.ME;
	}

	public static void playByHimInitial(Move move) {
		if (move.row != -1)
			initialGrid[move.row][move.col] = State.HIM;
	}

	public static State SimulateGame(Individual individual1, Individual individual2, int validActionCount) {
		resetBoard();
		int individual1Offset = 0;
		int individual2Offset = 0;

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

			if (winner == State.ME) {
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

			if (winner == State.HIM) {
				return State.HIM;
			}
		}

		return State.NOBODY;
	}

	private static boolean isEmptyTule(Move move) {
		return grid[move.row][move.col] == State.NOBODY;
	}

	private static void playByMe(Move move) {
		grid[move.row][move.col] = State.ME;
		updateWinner();
	}

	private static void playByHim(Move move) {
		grid[move.row][move.col] = State.HIM;
		updateWinner();
	}

	private static void updateWinner() {
		for (int i = 0; i < 3; i++) {
			// check rows
			if (grid[i][0] != State.NOBODY && grid[i][0] == grid[i][1] && grid[i][0] == grid[i][2]) {
				winner = grid[i][0];
			}

			// check cols
			if (grid[0][i] != State.NOBODY && grid[0][i] == grid[1][i] && grid[0][i] == grid[2][i]) {
				winner = grid[0][i];
			}
		}

		// check diags
		if (grid[0][0] != State.NOBODY && grid[0][0] == grid[1][1] && grid[0][0] == grid[2][2]) {
			winner = grid[0][0];
		}
		if (grid[2][0] != State.NOBODY && grid[2][0] == grid[1][1] && grid[2][0] == grid[0][2]) {
			winner = grid[2][0];
		}
	}

	private static void resetBoard() {
		winner = State.NOBODY;

		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				grid[i][j] = initialGrid[i][j];
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

	private List<Individual> individualList = new ArrayList<>(Settings.POPULATION_SIZE);

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
		for (int i = 0; i < Settings.POPULATION_SIZE; i++) {
			individualList.add(new Individual(validActionCount, moves));
		}
	}

	public Move getBest() {
		sortIndividuals();
		return individualList.get(0).getMove(0);
	}

	public void sortIndividuals() {
		for (Individual individual : individualList) {
			individual.calculateScore(individualList);
		}

		Collections.sort(individualList, comparator);
	}

	public void updateIndividuals(List<Individual> newIndividuals) {
		individualList = individualList.subList(0, individualList.size() - Settings.MAX_CHILDREN);
		individualList.addAll(newIndividuals);
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

		for (int i = 0; i < Settings.MAX_SELECTION; i++) {
			for (int j = 0; j < Settings.MAX_SELECTION; j++) {
				if (i != j) {
					Couple couple = new Couple(individualsSelected.get(i), individualsSelected.get(j));
					couples.add(couple);
				}
			}
		}

		Collections.shuffle(couples);

		if (couples.size() < Settings.MAX_CHILDREN) {
			return couples;
		}

		return couples.subList(0, Settings.MAX_CHILDREN);
	}

	public List<Individual> selectionIndividuals() {
		if (individualList.size() < Settings.MAX_SELECTION)
			return individualList;

		return individualList.subList(0, Settings.MAX_SELECTION);
	}

	public void mutation() {
		for (Individual individual : individualList) {
			individual.mutation();
		}
	}

	@Override
	public String toString() {
		String result = "";

		for (Individual individual : individualList) {
			result += individualList.indexOf(individual) + " ---> " + individual.toString() + "\n";
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

	public void calculateScore(List<Individual> individuals) {
		int scoreCalculated = 0;

		for (Individual individual : individuals) {
			if (individual != this && getPlayAgainsDecision()) {
				State state = Board.SimulateGame(this, individual, validActionCount);
				if (state == State.ME)
					scoreCalculated += Settings.POINTS_BY_VICTORY;
				else if (state == State.NOBODY)
					scoreCalculated += Settings.POINTS_BY_EQUALITY;
				else
					scoreCalculated += Settings.POINTS_BY_LOOSE;
			}
		}
		score = scoreCalculated;
	}

	private boolean getPlayAgainsDecision() {
		return 0 + (int) (Math.random() * ((100 - 0) + 1)) < Settings.PLAY_AGAINS_PROBABILITY;
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
				population.sortIndividuals();

				if (Settings.DEBUG) {
					System.err.println("GENERATION " + i);
					System.err.println(population);
				}

				List<Individual> selection = population.selectionIndividuals();
				List<Couple> couples = population.getCouples(selection);
				List<Individual> newIndividuals = population.getNewGenerationIndividuals(couples);

				population.updateIndividuals(newIndividuals);
				population.mutation();
			}

			population.sortIndividuals();
			Move myMove = population.getBest();
			Board.playByMeInitial(myMove);

			System.out.println(myMove.toString());
		}
	}

}
