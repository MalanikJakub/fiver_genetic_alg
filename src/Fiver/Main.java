package Fiver;

import java.util.ArrayList;

public class Main {

    final static int rows = 4;
    final static int cols = 4;
    final static int popSize = 30;
    final static int evaluations = 5000;
    final static int tournamentSize = 3;
    final static double pC = 0.6;
    final static double pM = 0.2;
    static Individual[] oldPop;
    static Individual[] newPop;
    static Individual[] offspringPop;
    static double fbest;
    static double favg;
    static double fworst;
    static double fsum;
    static int indbest;
    static int indworst;

    public static void main(String[] args) {

        int currGen = 0;
        int totEvals = 0;
        Individual.rows = rows;
        Individual.cols = cols;

        oldPop = new Individual[popSize];
        newPop = new Individual[popSize];
        offspringPop = new Individual[2];

        totEvals = initPop(oldPop);         // initialize population.

        getStats(oldPop);
        reportResults(totEvals);
        printBest(oldPop);

        ArrayList<Individual> initialFront = new ArrayList<Individual>();
        initialFront.add(oldPop[indbest]);  // elitism - add best individual to the new population

        evolvePop(oldPop, totEvals);        // apply evolution on the population

        getStats(oldPop);
        ArrayList<Individual> finalFront = new ArrayList<Individual>();
        finalFront.add(oldPop[indbest]);
        System.out.println("--------------------------------END------------------------------------");
        reportResults(totEvals);
        printBest(oldPop);

    }

    /**
     * Randomly initialize population.
     * @param pop Array of individuals to initialize
     * @return number of evaluations
     */
    public static int initPop(Individual[] pop) {
        int evals = 0;
        for (int i = 0; i < popSize; i++) {
            pop[i] = new Individual();
            pop[i].calcFitness();
            evals++;
        }
        return evals;
    }

    /**
     * Evolves the population using crossover and mutation
     * @param pop population to be evolved
     * @param totEvals current total of evaluations
     */
    public static void evolvePop(Individual[] pop, int totEvals) {
        while (totEvals < evaluations) {
            int generated = 0;

            // Elitism
            for (int i = 0; i < 5; i++) {
                newPop[generated++] = oldPop[indbest].clone();
            }
            // Fill the new generation
            while (generated < popSize) {
                int[] par = selection(oldPop);
                Individual parent1 = oldPop[par[0]].clone();
                Individual parent2 = oldPop[par[1]].clone();
                if (Math.random() < pC) {
                    offspringPop = parent1.crossover(parent2);
                    offspringPop[0].mutation();
                    offspringPop[1].mutation();
                } else {
                    offspringPop[0] = parent1;
                    offspringPop[1] = parent2;
                    offspringPop[0].mutation();
                    offspringPop[1].mutation();
                }
                offspringPop[0].calcFitness();
                offspringPop[1].calcFitness();
                totEvals += 2;

                newPop[generated++] = offspringPop[0];
                if (generated < popSize) {
                    newPop[generated++] = offspringPop[1];
                }
            }

            Individual[] temp;
            temp = oldPop;
            oldPop = newPop;
            newPop = temp;

            getStats(oldPop);
            reportResults(totEvals);
            printBest(oldPop);

        }


    }

    public static int[] selection(Individual[] p) {
        int[] chosen = new int[2];
        chosen[0] = tournamentSelection(p);
        chosen[1] = chosen[0];
        while (chosen[0] == chosen[1]) {
            chosen[1] = tournamentSelection(p);
        }
        return chosen;
    }

    /**
     * Tournaments selection where from number of randomly selected individuals
     * is chosen one to be used in the next population.
     * @param p population
     * @return index of the winning individual
     */
    public static int tournamentSelection(Individual[] p) {
        int index;
        int winnerIndex = (int) (Math.random() * popSize);
        double winnerFitness = p[winnerIndex].fitness;

        for (int i = 0; i < tournamentSize - 1; i++) {
            index = (int) (Math.random() * popSize);
            if (p[index].fitness > winnerFitness) {
                winnerFitness = p[index].fitness;
                winnerIndex = index;
            }
        }
        return winnerIndex;
    }

    public static void getStats(Individual[] p) {
        fbest = -10000.0;
        fworst = 10000;
        favg = 0.0;
        fsum = 0.0;
        indbest = 0;
        indworst = 0;

        for (int i = 0; i < popSize; i++) {
            if (fbest <= p[i].fitness) {
                fbest = p[i].fitness;   // Store best fitness value.
                indbest = i;              // Store best fitness individual index.
            }
            if (fworst >= p[i].fitness) {
                fworst = p[i].fitness;    // Store worst fitness value.
                indworst = i;             // Stroe worst fitness individual index.
            }
            fsum += p[i].fitness;           // Calculate fitness sum of whole population.
        }

        favg = fsum / popSize;  // Calculate average fitness value in population.

    }

    public static void reportResults(int evals) {
        System.out.println("evals: " + evals + "\t fbest: " + fbest + "\t favg: " + favg);
    }

    public static void printBest(Individual[] p) {
        // Print the values of x on standard output.
        System.out.println("\tbest solution fitness: " + p[indbest].fitness);
        p[indbest].printFlipMap();
        p[indbest].printBoard();
    }
}

class Individual {

    public static int rows;         // number of rows
    public static int cols;         // number of columns
    public boolean[][] flipMap;     // map of "player" turns on the game board
    public double fitness;          // fitness of the individual

    /**
     * Constructor of Individual without parameters generates random
     * individual which consist of 2D boolean array. The array represents
     * the game board and true values represent position where squares are
     * chosen for play.
     */
    public Individual() {
        flipMap = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() < 0.5) {
                    flipMap[i][j] = true;
                }
            }
        }
    }

    /**
     * Overloaded constructor for cases when there is no need to generate random
     * positions.
     * @param b dummy parameter
     */
    public Individual(boolean b) {
        flipMap = new boolean[rows][cols];
    }

    /**
     * Overriden clone method for Individual.
     * @return
     */
    @Override
    public Individual clone() {
        Individual ind = new Individual(false);
        for (int i = 0; i < rows; i++) {
            System.arraycopy(this.flipMap[i], 0, ind.flipMap[i], 0, cols);
        }
        ind.fitness = this.fitness;
        return ind;
    }

    /**
     * Calcultates fitness of the individual and stores it in the individual.
     */
    public void calcFitness() {
        int flipped = 0;
        int flips = 0;
        boolean[][] board;
        board = this.getBoard();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j]) {
                    flipped++;
                }
                if (this.flipMap[i][j]) {
                    flips++;
                }
            }
        }
        if (1.0 / flips < 1) {       // value behind decimal point represents number of flipped positions.
            this.fitness = flipped + 1.0 / flips;
        } else {
            this.fitness = flipped;
        }
    }

    /**
     * Creates and returns representation of the board after flipping positions
     * specified by the flipMap.
     * @return
     */
    public boolean[][] getBoard() {
        boolean[][] board = new boolean[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.flipMap[i][j]) {
                    flip(board, i, j);
                }
            }
        }
        return board;
    }

    /**
     * Applies one player turn on the board. Negates position of [row][column]
     * and also all adjacent fields.
     * @param b 2D boolean array representing game board
     * @param r row of the turn
     * @param c column of the turn
     */
    public void flip(boolean[][] b, int r, int c) {
        b[r][c] = !b[r][c];
        if (r > 0) {
            b[r - 1][c] = !b[r - 1][c];
        }
        if (r < rows - 1) {
            b[r + 1][c] = !b[r + 1][c];
        }
        if (c > 0) {
            b[r][c - 1] = !b[r][c - 1];
        }
        if (c < cols - 1) {
            b[r][c + 1] = !b[r][c + 1];
        }
    }

    /**
     * Prints out the board.
     */
    public void printBoard() {
        boolean[][] board = this.getBoard();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (board[i][j]) {
                    System.out.print(" #");
                } else {
                    System.out.print(" -");
                }
            }
            System.out.println("");
        }
        System.out.println("");
    }

    /**
     * Prints out the flipMap.
     */
    public void printFlipMap() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.flipMap[i][j]) {
                    System.out.print(" #");
                } else {
                    System.out.print(" -");
                }
            }
            System.out.println("");
        }
        System.out.println("");
    }

    /**
     * Executes crossover of this individual with another specified individual.
     * Chance of swapping of genes between the two individuals
     * @param mate individual for crossover
     * @return array with 2 individuals
     */
    public Individual[] crossover(Individual mate) {
        Individual[] I = new Individual[2];
        I[0] = this.clone();
        I[1] = mate.clone();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() < 0.5) {
                    I[0].flipMap[i][j] = mate.flipMap[i][j];
                } else {
                    I[0].flipMap[i][j] = this.flipMap[i][j];
                }
                if (Math.random() < 0.5) {
                    I[1].flipMap[i][j] = this.flipMap[i][j];
                } else {
                    I[1].flipMap[i][j] = mate.flipMap[i][j];
                }

            }
        }

        return I;
    }

    /**
     * Mutation of the individual. With probability pM the cells of the flipMap are negated.
     */
    public void mutation() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (Math.random() < Main.pM) {
                    this.flipMap[i][j] = !this.flipMap[i][j];
                }
            }
        }
    }
}
