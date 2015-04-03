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


public class GridMDP {

	private int n;
	private int terminalState = -1;

	private static final int m = 4;
	private static final int LEFT = 0;
	private static final int UP = 1;
	private static final int RIGHT = 2;
	private static final int DOWN = 3;

	private double T[][][] = null;
	private static final double FORWARD = 0.8;
	private static final double DEVIATE = 0.1; // Two of these.

	private double R[] = null;
	private static final double EMPTY = -0.03;
	private static final double OBSTACLE = 0.0;
	private static final double SUCCESS = 1.0;
	private static final double FAILURE = -1.0;
	private static final double DEAD_END = -1.0;
	private static final double TERMINAL_STATE = 0.0;

	/**
	 * The constructor.
	 */
	public GridMDP() {
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
	 *
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
					T[s][LEFT][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][LEFT][terminalState] = 1.0;
				} else {
					// Move LEFT. FORWARD!
					if (sx == 0 || grid[sx - 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][LEFT][s] += FORWARD; // Self-transition.
					} else {
						int sp = resolve_state(sx - 1, sy, w, h);
						T[s][LEFT][sp] += FORWARD; // Yay!
					}

					// Move LEFT. Deviate UP.
					if (sy == 0 || grid[sx][sy - 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][LEFT][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy - 1, w, h);
						T[s][LEFT][sp] += DEVIATE; // Yay!
					}

					// Move LEFT. Deviate DOWN.
					if (sy == h - 1 || grid[sx][sy + 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][LEFT][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy + 1, w, h);
						T[s][LEFT][sp] += DEVIATE; // Yay!
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
					T[s][UP][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][UP][terminalState] = 1.0;
				} else {
					// Move UP. FORWARD!
					if (sy == 0 || grid[sx][sy - 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][UP][s] += FORWARD; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy - 1, w, h);
						T[s][UP][sp] += FORWARD; // Yay!
					}

					// Move UP. Deviate LEFT.
					if (sx == 0 || grid[sx - 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][UP][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx - 1, sy, w, h);
						T[s][UP][sp] += DEVIATE; // Yay!
					}

					// Move UP. Deviate RIGHT.
					if (sx == w - 1 || grid[sx + 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][UP][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx + 1, sy, w, h);
						T[s][UP][sp] += DEVIATE; // Yay!
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
					T[s][RIGHT][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][RIGHT][terminalState] = 1.0;
				} else {
					// Move RIGHT. FORWARD!
					if (sx == w - 1 || grid[sx + 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][RIGHT][s] += FORWARD; // Self-transition.
					} else {
						int sp = resolve_state(sx + 1, sy, w, h);
						T[s][RIGHT][sp] += FORWARD; // Yay!
					}

					// Move RIGHT. Deviate UP.
					if (sy == 0 || grid[sx][sy - 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][RIGHT][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy - 1, w, h);
						T[s][RIGHT][sp] += DEVIATE; // Yay!
					}

					// Move RIGHT. Deviate DOWN.
					if (sy == h - 1 || grid[sx][sy + 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][RIGHT][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy + 1, w, h);
						T[s][RIGHT][sp] += DEVIATE; // Yay!
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
					T[s][DOWN][s] = 1.0;
				} else if (grid[sx][sy] == GridPanel.GridCellType.SUCCESS ||
						grid[sx][sy] == GridPanel.GridCellType.FAILURE) {
					// Success and failure states go to the absorbing terminal state.
					T[s][DOWN][terminalState] = 1.0;
				} else {
					// Move DOWN. FORWARD!
					if (sy == h - 1 || grid[sx][sy + 1] == GridPanel.GridCellType.OBSTACLE) {
						T[s][DOWN][s] += FORWARD; // Self-transition.
					} else {
						int sp = resolve_state(sx, sy + 1, w, h);
						T[s][DOWN][sp] += FORWARD; // Yay!
					}

					// Move DOWN. Deviate LEFT.
					if (sx == 0 || grid[sx - 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][DOWN][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx - 1, sy, w, h);
						T[s][DOWN][sp] += DEVIATE; // Yay!
					}

					// Move DOWN. Deviate RIGHT.
					if (sx == w - 1 || grid[sx + 1][sy] == GridPanel.GridCellType.OBSTACLE) {
						T[s][DOWN][s] += DEVIATE; // Self-transition.
					} else {
						int sp = resolve_state(sx + 1, sy, w, h);
						T[s][DOWN][sp] += DEVIATE; // Yay!
					}
				}

				s++;
			}
		}

		// Assign the terminal state to be absorbing.
		for (int a = 0; a < m; a++) {
			T[terminalState][a][terminalState] = 1.0;
		}

		// Create the rewards.
		R = new double[n];
		s = 0;
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (grid[x][y] == GridPanel.GridCellType.EMPTY) {
					R[s] = EMPTY;
				} else if (grid[x][y] == GridPanel.GridCellType.OBSTACLE) {
					R[s] = OBSTACLE;
				} else if (grid[x][y] == GridPanel.GridCellType.SUCCESS) {
					R[s] = SUCCESS;
				} else if (grid[x][y] == GridPanel.GridCellType.FAILURE) {
					R[s] = FAILURE;
				} else if (grid[x][y] == GridPanel.GridCellType.DEAD_END) {
					R[s] = DEAD_END;
				}
				s++;
			}
		}

		// The terminal state has it's own reward (of zero).
		R[terminalState] = TERMINAL_STATE;

		// DEBUG: Ensure state transitions properly sum to 1.
//		System.out.println("DEBUG: Ensure state transitions properly sum to 1.");
//		for (s = 0; s < n; s++) {
//			for (int a = 0; a < m; a++) {
//				double sum = 0.0;
//				for (int sp = 0; sp < n; sp++) {
//					sum += T[s][a][sp];
//				}
//				System.out.println(sum);
//			}
//		}
	}

	public boolean saveMDP(File file) {
		try {
			FileWriter fileWriter = new FileWriter(file);

			// Write the first line ("header") for the raw MDP file: <n, m, k, s0, h, g>.
			fileWriter.write(Integer.toString(n) + "," + Integer.toString(m) + ",1,0,0,0.9\n");

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

			// Save the four actions as matrix blocks.
			for (int a = 0; a < m; a++) {
				for (int s = 0; s < n; s++) {
					for (int sp = 0; sp < n; sp++) {
						fileWriter.write(Double.toString(T[s][a][sp]));
						if (sp != n - 1) {
							fileWriter.write(",");
						}
					}

					if (!(a == m - 1 && s == n - 1)) {
						fileWriter.write("\n");
					}
				}
			}

			fileWriter.close();
		} catch (Exception e) {
			System.out.println("Failed to save the MDP to file '" + file.getName() + "'.");
			return false;
		}

		return true;
	}

}
