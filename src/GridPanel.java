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


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.swing.JPanel;
import javax.swing.Timer;


public class GridPanel extends JPanel implements MouseListener, ComponentListener, ActionListener {

	/**
	 * Auto-generated serial ID.
	 */
	private static final long serialVersionUID = 3728396685715564240L;

	/**
	 * The width of the grid world.
	 */
	private int width;

	/**
	 * The height of the grid world.
	 */
	private int height;

	/**
	 * The step of the x-axis.
	 */
	private int stepX;

	/**
	 * The step of the y-axis.
	 */
	private int stepY;

	/**
	 * The grid world.
	 */
	private int grid[][];

	/**
	 * The policy within the grid world.
	 */
	private int policy[][];

	/**
	 * The robot timer for moving around.
	 */
	private Timer tmrRobot;

	/**
	 * The current x location of the robot (in grid coordinates).
	 */
	private int robotX;

	/**
	 * The current y location of the robot (in grid coordinates).
	 */
	private int robotY;

	/**
	 * The various types of cells for the grid.
	 */
	public static class GridCellType {
		public final static int EMPTY = 0;
		public final static int OBSTACLE = 1;
		public final static int SUCCESS = 2;
		public final static int FAILURE = 3;
		public final static int DEAD_END = 4;
		public final static int NUM_CELL_TYPES = 5;

		public final static Color colors[] = {
			Color.white, Color.gray, Color.green, Color.red, Color.black
		};
	}

	/**
	 * The various types of actions to make at each state in the grid.
	 */
	public static class Action {
		public final static int EAST = 0;
		public final static int NORTH = 1;
		public final static int WEST = 2;
		public final static int SOUTH = 3;
		public final static int NUM_ACTIONS = 4;
	}

	/**
	 * The main constructor for the GridPanel class.
	 * @param 	width	The width in cells.
	 * @param 	height	The height in cells.
	 */
	public GridPanel(int width, int height) {
		super();

		this.addMouseListener(this);
		this.addComponentListener(this);
		this.setDoubleBuffered(true);

		this.grid = new int[width][height];
		this.width = width;
		this.height = height;
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				this.grid[i][j] = GridCellType.EMPTY;
			}
		}

		robotX = 0;
		robotY = 0;

		tmrRobot = new Timer(1000, this);
		tmrRobot.stop();
	}

	/**
	 * Toggle the robot, enabled/disabled motion.
	 */
	public void toggleRobot() {
		if (tmrRobot.isRunning()) {
			tmrRobot.stop();
		} else {
			tmrRobot.start();
		}
	}

	/**
	 * Set the speed in milliseconds.
	 * @param	speed	The speed in milliseconds.
	 */
	public void setSpeed(int speed) {
		tmrRobot.setDelay(speed);
	}

	/**
	 * Get the grid with
	 * @return	The grid width.
	 */
	public int getGridWidth() {
		return this.width;
	}

	/**
	 * Get the grid height.
	 * @return	The grid height.
	 */
	public int getGridHeight() {
		return this.height;
	}

	/**
	 * Get the current grid.
	 * @return	The current grid.
	 */
	public int[][] getGrid() {
		return grid;
	}

	/**
	 * Load a grid from a file in a CSV format.
	 * @param	file	The file to load.
	 * @return	The success of loading.
	 */
	public boolean loadGrid(File file) {
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			// Read the first line of the buffered reader object.
			String line = bufferedReader.readLine();

			String data[] = line.split(",");
			if (data.length != 2) {
				System.out.println("File contains an invalid first line.");
				bufferedReader.close();
				return false;
			}

			int newWidth = Integer.parseInt(data[0]);
			int newHeight = Integer.parseInt(data[1]);

			int newGrid[][] = new int[newWidth][newHeight];

			// Attempt to read the entire grid. Invalid elements get converted to empty, and if a row
			// is detected that contains an invalid number of elements, it returns an error.
			int row = 0;
			while ((line = bufferedReader.readLine()) != null) {
				if (row >= newHeight) {
					System.out.println("Too many lines. Breaking load early.");
					break;
				}

				data = line.split(",");

				if (data.length != newWidth) {
					System.out.println("Line " + (row + 1) + " in file '" + file.getName() + "' is invalid.");
					bufferedReader.close();
					return false;
				}

				for (int col = 0; col < data.length; col++) {
					newGrid[col][row] = Integer.parseInt(data[col]);
					if (newGrid[col][row] < 0 || newGrid[col][row] >= GridCellType.NUM_CELL_TYPES) {
						System.out.println("Invalid cell integer '" + newGrid[col][row] + "' in file '" +
									file.getName() + "'. Default to empty.");
						newGrid[col][row] = GridCellType.EMPTY;
					}
				}

				row++;
			}

			// If it gets here, then we know we can overwrite the current grid safely.
			grid = new int[newWidth][newHeight];
			for (int i = 0; i < newWidth; i++) {
				for (int j = 0; j < newHeight; j++) {
					grid[i][j] = newGrid[i][j];
				}
			}
			this.width = newWidth;
			this.height = newHeight;

			bufferedReader.close();
		} catch (Exception e) {
			System.out.println("Failed to load the grid for file '" + file.getName() + "'.");
			e.printStackTrace();
			return false;
		}

		repaint();

		return true;
	}

	/**
	 * Save the current grid to the file specified in a raw CSV format.
	 * @param	file	The file to save to.
	 * @return	The success of saving.
	 */
	public boolean saveGrid(File file) {
		try {
			FileWriter fileWriter = new FileWriter(file);

			// Write the first two lines, specifying width and height.
			fileWriter.write(Integer.toString(this.width) + "," + Integer.toString(this.height) + "\n");

			// Write the rest of the grid world. Note: This saves it in row-col order to be
			// pretty. It is also loaded in this order.
			for (int j = 0; j < this.height; j++) {
				for (int i = 0; i < this.width; i++) {
					fileWriter.write(Integer.toString(grid[i][j]));
					if (i != this.width - 1) {
						fileWriter.write(",");
					}
				}
				if (j != this.height - 1) {
					fileWriter.write("\n");
				}
			}

			fileWriter.close();
		} catch (Exception e) {
			System.out.println("Failed to save the grid to file '" + file.getName() + "'.");
			return false;
		}

		return true;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		Graphics2D g2 = (Graphics2D)g;

		// Constantly update the step sizes for rendering.
		this.stepX = (int)(this.getWidth() / this.width);
		this.stepY = (int)(this.getHeight() / this.height);

		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				g2.setColor(GridCellType.colors[grid[i][j]]);
				g2.fillRect(i * this.stepX, j * this.stepY, this.stepX, this.stepY);
			}
		}
	}

	/**
	 * Update the width and height of the grid world.
	 * @param width		The new grid width.
	 * @param height	The new grid height.
	 */
	public void update(int width, int height) {
		// Copy the current values in the grid array.
		int temp[][] = new int[this.width][this.height];
		for (int i = 0; i < this.width; i++) {
			for (int j = 0; j < this.height; j++) {
				temp[i][j] = grid[i][j];
			}
		}

		// Update the grid accordingly.
		grid = new int[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i < this.width && j < this.height) {
					grid[i][j] = temp[i][j];
				} else {
					grid[i][j] = GridCellType.EMPTY;
				}
			}
		}

		this.width = width;
		this.height = height;
		this.stepX = (int)(this.getWidth() / this.width);
		this.stepY = (int)(this.getHeight() / this.height);

		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON3) {
			int i = (int)(e.getX() / this.stepX);
			int j = (int)(e.getY() / this.stepY);

			grid[i][j]++;
			if (grid[i][j] >= GridCellType.NUM_CELL_TYPES) {
				grid[i][j] = 0;
			}

			repaint();
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

	@Override
	public void componentResized(ComponentEvent e) {
		this.stepX = (int)(this.getWidth() / this.width);
		this.stepY = (int)(this.getHeight() / this.height);
		repaint();
	}

	@Override
	public void componentMoved(ComponentEvent e) { }

	@Override
	public void componentShown(ComponentEvent e) { }

	@Override
	public void componentHidden(ComponentEvent e) { }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == tmrRobot) {
			// First, do nothing if the robot is located in an obstacle.

			// If the policy is defined, then we can update the robot by randomly choosing a next state.

			// Note that it stands still if it walks into an obstacle.

			System.out.println("Robot Moves.");
		}
	}

}
