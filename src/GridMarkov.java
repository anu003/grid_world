/**
 *  The MIT License (MIT)
 *
 *  Copyright (c) 2014 Kyle Hollins Wray, University of Massachusetts
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of
 *  this software and associated documentation files (the "Software"), to deal in
 *  the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 *  the Software, and to permit persons to whom the Software is furnished to do so,
 *  subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */


import java.io.File;
import java.io.FileWriter;


public class GridMarkov {

	private int n;
	private int terminalState = -1;
	
	private static final int m = 4;
	private static final int LEFT_ACTION = 0;
	private static final int UP_ACTION = 1;
	private static final int RIGHT_ACTION = 2;
	private static final int DOWN_ACTION = 3;
	
	private int z;
	private int inTheDarkObservation = -1;
	private int absorbingObservation = -1;
	
	private double T[][][] = null;
	private static final double FORWARD_PROBABILITY = 0.8;
	private static final double DEVIATE_PROBABILITY = 0.1; // Two of these.
	
	private double O[][][] = null;
	private static final double LIGHT_LOCALIZATION_PROBABILITY = 1.0;
	private static final double DIM_LIGHT_LOCALIZATION_PROBABILITY = 0.8;
	
	private double R[] = null;
	private static final double EMPTY_REWARD = -0.03;
	private static final double OBSTACLE_REWARD = 0.0;
	private static final double SUCCESS_REWARD = 1.0;
	private static final double FAILURE_REWARD = -1.0;
	private static final double DEAD_END_REWARD = -1.0;
	private static final double TERMINAL_STATE_REWARD = 0.0;
	private static final double LIGHT_REWARD = -0.03;
	
	private int s0 = -1;
	private int r;
	private double B[][] = null;
	private static final double CELL_AND_NEIGHBOR_PROBABILITY = 0.75;
	
	private int horizon = 250;
	private double gamma = 0.9;
	
	/**
	 * The constructor.
	 */
	public GridMarkov() {
	}
	
	/**
	 * Resolve the state.
	 * @param 	sx	The state x value.
	 * @param 	sy	The state y value.
	 * @param 	w	The grid width.
	 * @param 	h	The grid height.
	 * @return
	 */
	private int resolve_state(int sx, int sy, int w, int h) {
		return sy * w + sx;
	}

	/**
	 * Create the internal (PO)MDP variables.
	 * @param 	grid	The x-by-y grid, meaning stored 0 1 2; 3 4 5; 6 7 8; etc.
	 * @param 	w		The width of the grid.
	 * @param 	h		The height of the grid.
	 */
	public void create(int grid[][], int w, int h) {
		// Create the states (left to right, then top to bottom). Absorbing is the final one.
		n = w * h + 1; // Always create an absorbing goal state.
		terminalState = w * h;

		// Create the state transitions.
		int s = 0;

		// Setup the initial zeros.
		T = new double[n][m][n];
		for (s = 0; s < n; s++) {
			for (int a = 0; a < m; a++) {
				for (int sp = 0; sp < n; sp++) {
					T[s][a][sp] = 0.0;
				}
			}
		}

		// Actually assign the correct values of state transitions
		// for each of the actions.

		// Move LEFT.
		s = 0;
		for (int sy = 0; sy < h; sy++) {
			for (int sx = 0; sx < w; sx++) {
				if (grid[sx][sy] == GridPanel.GridCellType.OBSTACLE ||
						grid[sx][sy] == GridPanel.GridCellType.DEAD_END) {
					// Obstacles and dead ends self-loop.
					T[s][LEFT_ACTION][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][LEFT_ACTION][terminalState] = 1.0;
				} else {
					// Move LEFT. FORWARD!
					if (sx == 0 || grid[sx - 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][LEFT_ACTION][s] += FORWARD_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx - 1, sy, w, h);
						T[s][LEFT_ACTION][sp] += FORWARD_PROBABILITY; // Yay!
					}

					// Move LEFT. Deviate UP.
					if (sy == 0 || grid[sx][sy - 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][LEFT_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy - 1, w, h);
						T[s][LEFT_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}

					// Move LEFT. Deviate DOWN.
					if (sy == h - 1 || grid[sx][sy + 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][LEFT_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy + 1, w, h);
						T[s][LEFT_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}
				}

				s++;
			}
		}

		// Move UP.
		s = 0;
		for (int sy = 0; sy < h; sy++) {
			for (int sx = 0; sx < w; sx++) {
				if (grid[sx][sy] == GridPanel.GridCellType.OBSTACLE ||
						grid[sx][sy] == GridPanel.GridCellType.DEAD_END) {
					// Obstacles and dead ends self-loop.
					T[s][UP_ACTION][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][UP_ACTION][terminalState] = 1.0;
				} else {
					// Move UP. FORWARD!
					if (sy == 0 || grid[sx][sy - 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][UP_ACTION][s] += FORWARD_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy - 1, w, h);
						T[s][UP_ACTION][sp] += FORWARD_PROBABILITY; // Yay!
					}

					// Move UP. Deviate LEFT.
					if (sx == 0 || grid[sx - 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][UP_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx - 1, sy, w, h);
						T[s][UP_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}

					// Move UP. Deviate RIGHT.
					if (sx == w - 1 || grid[sx + 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][UP_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx + 1, sy, w, h);
						T[s][UP_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}
				}

				s++;
			}
		}

		// Move RIGHT.
		s = 0;
		for (int sy = 0; sy < h; sy++) {
			for (int sx = 0; sx < w; sx++) {
				if (grid[sx][sy] == GridPanel.GridCellType.OBSTACLE ||
						grid[sx][sy] == GridPanel.GridCellType.DEAD_END) {
					// Obstacles and dead ends self-loop.
					T[s][RIGHT_ACTION][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][RIGHT_ACTION][terminalState] = 1.0;
				} else {
					// Move RIGHT. FORWARD!
					if (sx == w - 1 || grid[sx + 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][RIGHT_ACTION][s] += FORWARD_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx + 1, sy, w, h);
						T[s][RIGHT_ACTION][sp] += FORWARD_PROBABILITY; // Yay!
					}

					// Move RIGHT. Deviate UP.
					if (sy == 0 || grid[sx][sy - 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][RIGHT_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy - 1, w, h);
						T[s][RIGHT_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}

					// Move RIGHT. Deviate DOWN.
					if (sy == h - 1 || grid[sx][sy + 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][RIGHT_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy + 1, w, h);
						T[s][RIGHT_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}
				}

				s++;
			}
		}

		// Move DOWN.
		s = 0;
		for (int sy = 0; sy < h; sy++) {
			for (int sx = 0; sx < w; sx++) {
				if (grid[sx][sy] == GridPanel.GridCellType.OBSTACLE ||
						grid[sx][sy] == GridPanel.GridCellType.DEAD_END) {
					// Obstacles and dead ends self-loop.
					T[s][DOWN_ACTION][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][DOWN_ACTION][terminalState] = 1.0;
				} else {
					// Move DOWN. FORWARD!
					if (sy == h - 1 || grid[sx][sy + 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][DOWN_ACTION][s] += FORWARD_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy + 1, w, h);
						T[s][DOWN_ACTION][sp] += FORWARD_PROBABILITY; // Yay!
					}

					// Move DOWN. Deviate LEFT.
					if (sx == 0 || grid[sx - 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][DOWN_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx - 1, sy, w, h);
						T[s][DOWN_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}

					// Move DOWN. Deviate RIGHT.
					if (sx == w - 1 || grid[sx + 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][DOWN_ACTION][s] += DEVIATE_PROBABILITY; // Self-transition.
					} else {
						int sp = resolve_state(sx + 1, sy, w, h);
						T[s][DOWN_ACTION][sp] += DEVIATE_PROBABILITY; // Yay!
					}
				}

				s++;
			}
		}
		
		// Assign the terminal state to be absorbing.
		for (int a = 0; a < m; a++) {
			T[terminalState][a][terminalState] = 1.0;
		}
		
		// There is one observation for each light spot, so figure out how many there are.
		z = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (grid[x][y] == GridPanel.GridCellType.LIGHT) {
					z++;
				}
			}
		}
		
		// There are two more observations: one for the "in the dark", and one for "absorbing".
		inTheDarkObservation = z;
		z++;
		absorbingObservation = z;
		z++;
		
		// Also, for the sake of easy in programming, store the observation index in a grid,
		// as well as the four areas around it, assuming they are not also lights.
		int observationGrid[][] = new int[w][h];
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				observationGrid[x][y] = inTheDarkObservation;
			}	
		}
		
		int o = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// The agent always knows if it is in an absorbing state, so these count as
				// lights too; however, they do not emit towards empty neighbor cells like lights
				// do. Thus, check for the light state, 
				if (grid[x][y] == GridPanel.GridCellType.LIGHT) {
					observationGrid[x][y] = o;
					
					// For each neighboring cell, if it is an empty cell, set it
					// with this observation. These get set to +z to mark that they
					// are dim light.
					if (x > 0 && grid[x - 1][y] == GridPanel.GridCellType.EMPTY) {
						observationGrid[x - 1][y] = o + z;
					}
					if (y > 0 && grid[x][y - 1] == GridPanel.GridCellType.EMPTY) {
						observationGrid[x][y - 1] = o + z;
					}
					if (x < w - 1 && grid[x + 1][y] == GridPanel.GridCellType.EMPTY) {
						observationGrid[x + 1][y] = o + z;
					}
					if (y < h - 1 && grid[x][y + 1] == GridPanel.GridCellType.EMPTY) {
						observationGrid[x][y + 1] = o + z;
					}
					
					o++;
				} else if (grid[x][y] != GridPanel.GridCellType.EMPTY) {
					// Logically, this must be a non-light, non-empty cell (i.e., an absorbing state of some kind).
					observationGrid[x][y] = absorbingObservation;
				}
			}
		}
		
		// Create the observation transitions.
		O = new double[m][n][z];
		for (int sp = 0; sp < n - 1; sp++) { // Note: The minus one is because the final state is a terminal state.
			// Get the grid location to figure out if this state is a "light"
			// state or, if not, possibly a "dim light" state.
			int x = (int)(sp % w);
			int y = (int)(sp / w);
			
			for (int a = 0; a < m; a++) {
				for (o = 0; o < z; o++) {
					O[a][sp][o] = 0.0;
				}
			}
			
			for (int a = 0; a < m; a++) {
				// Handle the "in the dark" case, then the "absorbing" case, then the "dim light" case, then the "light" case.
				if (observationGrid[x][y] == inTheDarkObservation) {
					O[a][sp][inTheDarkObservation] = 1.0;
				} else if (observationGrid[x][y] == absorbingObservation) {
					O[a][sp][absorbingObservation] = 1.0;
				} else if (observationGrid[x][y] >= z) {
					O[a][sp][observationGrid[x][y] - z] = DIM_LIGHT_LOCALIZATION_PROBABILITY;
					O[a][sp][inTheDarkObservation] = 1.0 - DIM_LIGHT_LOCALIZATION_PROBABILITY;
				} else {
					O[a][sp][observationGrid[x][y]] = LIGHT_LOCALIZATION_PROBABILITY;
					O[a][sp][inTheDarkObservation] = 1.0 - LIGHT_LOCALIZATION_PROBABILITY;
				}
			}
		}
		
		// Handle the terminal state now. The only reason this is separate from the code above is due to the fact that
		// it does not have an x-y location (in observationGrid or the grid itself).
		for (int a = 0; a < m; a++) {
			for (o = 0; o < z; o++) {
				O[a][terminalState][o] = 0.0;
			}
			O[a][terminalState][absorbingObservation] = 1.0;
		}

		// Create the rewards.
		R = new double[n];
		s = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (grid[x][y] == GridPanel.GridCellType.EMPTY) {
					R[s] = EMPTY_REWARD;
				} else if (grid[x][y] == GridPanel.GridCellType.OBSTACLE) {
					R[s] = OBSTACLE_REWARD;
				} else if (grid[x][y] == GridPanel.GridCellType.SUCCESS) {
					R[s] = SUCCESS_REWARD;
				} else if (grid[x][y] == GridPanel.GridCellType.FAILURE) {
					R[s] = FAILURE_REWARD;
				} else if (grid[x][y] == GridPanel.GridCellType.DEAD_END) {
					R[s] = DEAD_END_REWARD;
				} else if (grid[x][y] == GridPanel.GridCellType.LIGHT) {
					R[s] = LIGHT_REWARD;
				}
				s++;
			}
		}

		// The terminal state has it's own reward (of zero).
		R[terminalState] = TERMINAL_STATE_REWARD;

		// To define the set of beliefs, we must first compute the number of belief points. These belief points
		// come in a few varieties: (1) collapsed belief points over each non-obstacle state, and (2) distributed
		// belief over empty/light cells' neighbors, that are also empty/light.
		r = 0;
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				// For (1).
				if (grid[x][y] != GridPanel.GridCellType.OBSTACLE) {
					r++;
				}
				
				// For (2).
				if (grid[x][y] == GridPanel.GridCellType.EMPTY || grid[x][y] == GridPanel.GridCellType.LIGHT) {
					r++;
				}
				
//				if (x > 0 && (grid[x - 1][y] == GridPanel.GridCellType.EMPTY || grid[x - 1][y] == GridPanel.GridCellType.LIGHT)) {
//					r++;
//				}
//				if (y > 0 && (grid[x][y - 1] == GridPanel.GridCellType.EMPTY || grid[x][y - 1] == GridPanel.GridCellType.LIGHT)) {
//					r++;
//				}
//				if (x < w - 1 && (grid[x + 1][y] == GridPanel.GridCellType.EMPTY || grid[x + 1][y] == GridPanel.GridCellType.LIGHT)) {
//					r++;
//				}
//				if (y < h - 1 && (grid[x][y + 1] == GridPanel.GridCellType.EMPTY || grid[x][y + 1] == GridPanel.GridCellType.LIGHT)) {
//					r++;
//				}
			}
		}
		
		r++; // Terminal state has a belief point.
		
		B = new double[r][n];
		for (int i = 0; i < r; i++) {
			for (s = 0; s < n; s++) {
				B[i][s] = 0.0;
			}
		}
		
		// Now actually create (1).
		int i = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (grid[x][y] != GridPanel.GridCellType.OBSTACLE) {
					B[i][y * w + x] = 1.0;
					i++;
				}
			}
		}
		
		// Create the terminal state belief point.
		B[i][terminalState] = 1.0;
		i++;
		
		// Actually create (2).
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (grid[x][y] == GridPanel.GridCellType.EMPTY || grid[x][y] == GridPanel.GridCellType.LIGHT) {
					// First count the neighbors that are valid.
					int numNeighbors = 0;
					if (x > 0 && (grid[x - 1][y] == GridPanel.GridCellType.EMPTY || grid[x - 1][y] == GridPanel.GridCellType.LIGHT)) {
						numNeighbors++;
					}
					if (y > 0 && (grid[x][y - 1] == GridPanel.GridCellType.EMPTY || grid[x][y - 1] == GridPanel.GridCellType.LIGHT)) {
						numNeighbors++;
					}
					if (x < w - 1 && (grid[x + 1][y] == GridPanel.GridCellType.EMPTY || grid[x + 1][y] == GridPanel.GridCellType.LIGHT)) {
						numNeighbors++;
					}
					if (y < h - 1 && (grid[x][y + 1] == GridPanel.GridCellType.EMPTY || grid[x][y + 1] == GridPanel.GridCellType.LIGHT)) {
						numNeighbors++;
					}
					
					// Assign the probability for the state at <x, y>.
					B[i][y * w + x] = CELL_AND_NEIGHBOR_PROBABILITY; 
					
					// Now actually assign the probabilities for the neighbors.
					if (x > 0 && (grid[x - 1][y] == GridPanel.GridCellType.EMPTY || grid[x - 1][y] == GridPanel.GridCellType.LIGHT)) {
						B[i][y * w + (x - 1)] = (1.0 - CELL_AND_NEIGHBOR_PROBABILITY) / (double)numNeighbors;
					}
					if (y > 0 && (grid[x][y - 1] == GridPanel.GridCellType.EMPTY || grid[x][y - 1] == GridPanel.GridCellType.LIGHT)) {
						B[i][(y - 1) * w + x] = (1.0 - CELL_AND_NEIGHBOR_PROBABILITY) / (double)numNeighbors;
					}
					if (x < w - 1 && (grid[x + 1][y] == GridPanel.GridCellType.EMPTY || grid[x + 1][y] == GridPanel.GridCellType.LIGHT)) {
						B[i][y * w + (x + 1)] = (1.0 - CELL_AND_NEIGHBOR_PROBABILITY) / (double)numNeighbors;
					}
					if (y < h - 1 && (grid[x][y + 1] == GridPanel.GridCellType.EMPTY || grid[x][y + 1] == GridPanel.GridCellType.LIGHT)) {
						B[i][(y + 1) * w + x] = (1.0 - CELL_AND_NEIGHBOR_PROBABILITY) / (double)numNeighbors;
					}
					
					i++;
				}
			}
		}
	}
	
	public boolean saveMDP(File file) {
		try {
			FileWriter fileWriter = new FileWriter(file);

			// Write the first line ("header") for the raw MDP file: <n, m, k, s0, h, g>.
			fileWriter.write(Integer.toString(n) + "," + Integer.toString(m) + ",1," + s0 + "," + horizon + "," + gamma + "\n");

			// Save the state transitions.
			for (int a = 0; a < m; a++) {
				for (int s = 0; s < n; s++) {
					for (int sp = 0; sp < n; sp++) {
						fileWriter.write(Double.toString(T[s][a][sp]));
						if (sp != n - 1) {
							fileWriter.write(",");
						}
					}

					fileWriter.write("\n");
				}
			}

			// Save the rewards.
			for (int s = 0; s < n; s++) {
				for (int a = 0; a < m; a++) {
					fileWriter.write(Double.toString(R[s]));
					if (a != m - 1) {
						fileWriter.write(",");
					}
				}
				
				if (s != n - 1) {
					fileWriter.write("\n");
				}
			}

			fileWriter.close();
		} catch (Exception e) {
			System.out.println("Failed to save the MDP to file '" + file.getName() + "'.");
			return false;
		}

		return true;
	}
	
	public boolean savePOMDP(File file) {
		try {
			FileWriter fileWriter = new FileWriter(file);

			// Write the first line ("header") for the raw POMDP file: <n, m, z, r, k, h, g>.
			fileWriter.write(Integer.toString(n) + "," + Integer.toString(m) + "," + Integer.toString(z) + "," + Integer.toString(r) + ",1," + horizon + "," + gamma + "\n");

			// Save the state transitions.
			for (int a = 0; a < m; a++) {
				for (int s = 0; s < n; s++) {
					for (int sp = 0; sp < n; sp++) {
						fileWriter.write(Double.toString(T[s][a][sp]));
						if (sp != n - 1) {
							fileWriter.write(",");
						}
					}

					fileWriter.write("\n");
				}
			}

			// Save the observation transitions.
			for (int a = 0; a < m; a++) {
				for (int sp = 0; sp < n; sp++) {
					for (int o = 0; o < z; o++) {
						fileWriter.write(Double.toString(O[a][sp][o]));
						if (o != z - 1) {
							fileWriter.write(",");
						}
					}

					fileWriter.write("\n");
				}
			}

			// Save the rewards.
			for (int s = 0; s < n; s++) {
				for (int a = 0; a < m; a++) {
					fileWriter.write(Double.toString(R[s]));
					if (a != m - 1) {
						fileWriter.write(",");
					}
				}
				
				fileWriter.write("\n");
			}

			// Save the rewards.
			for (int i = 0; i < r; i++) {
				for (int s = 0; s < n; s++) {
					fileWriter.write(Double.toString(B[i][s]));
					if (s != n - 1) {
						fileWriter.write(",");
					}
				}
				
				if (i != r - 1) {
					fileWriter.write("\n");
				}
			}

			fileWriter.close();
		} catch (Exception e) {
			System.out.println("Failed to save the POMDP to file '" + file.getName() + "'.");
			return false;
		}

		return true;
	}

}
