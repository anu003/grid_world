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


import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.JSlider;


public class GridWorld implements ActionListener, ChangeListener, KeyListener, MouseWheelListener {

	/** The main frame (window). */
	private JFrame fraMain;
	
	/** The panel for rendering the grid world. */
	private GridPanel pnlGrid;
	
	/** The panel color to tell the user which brush he/she is using. */
	private JPanel pnlBrush;
	
	/** The human-readable meaning of the brush color. */
	private JLabel labBrush;
	
	/** The spinner for changing the width of the grid world. */
	private JSpinner sprWidth;
	
	/** The spinner for changing the height of the grid world. */
	private JSpinner sprHeight;
	
	/** The button for saving a grid world file. */
	private JButton btnSaveGridWorld;
	
	/** The button for loading a grid world file. */
	private JButton btnLoadGridWorld;
	
	/** The button for exporting a raw MDP file. */
	private JButton btnExportRawMDP;
	
	/** The button for exporting a raw SSP file. */
	private JButton btnExportRawSSP;
	
	/** The button for importing an MDP policy file. */
	private JButton btnImportMDPSSPPolicy;
	
	/** The button for exporting a raw POMDP file. */
	private JButton btnExportRawPOMDP;
	
	/** The button for importing a POMDP policy file. */
	private JButton btnImportPOMDPPolicy;

	/** The button which plays or pauses robot execution, given the initial starting point. */
	private JButton btnPlayPause;
	
	/** A slider which controls the speed of the robot as it moves around. For visualization only. */
	private JSlider sldSpeed;

	/**
	 * When you load a grid world, you need to lock the controls (e.g., spinners)
	 * since you must update their value, but not execute their update code in
	 * the listener functions like actionPerformed, etc.
	 */
	private boolean locked = false;
	
	/**
	 * The currently opened file. The file type will change based on what you do.
	 */
	private File currentFile = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GridWorld window = new GridWorld();
					window.fraMain.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GridWorld() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		fraMain = new JFrame();
		fraMain.setTitle("Grid World");
		fraMain.setBounds(100, 100, 800, 600);
		fraMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JSplitPane splMain = new JSplitPane();
		splMain.setDividerLocation(180);
		fraMain.getContentPane().add(splMain, BorderLayout.CENTER);

		JPanel pnlControl = new JPanel();
		pnlControl.setBorder(new TitledBorder(null, "Controls", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		splMain.setLeftComponent(pnlControl);

		JLabel lblWidth = new JLabel("Width:");
		JLabel lblHeight = new JLabel("Height:");

		sprWidth = new JSpinner();
		sprWidth.setValue(5);
		sprWidth.addChangeListener(this);
		sprWidth.addKeyListener(this);

		sprHeight = new JSpinner();
		sprHeight.setValue(5);
		sprHeight.addChangeListener(this);
		sprHeight.addKeyListener(this);

		pnlBrush = new JPanel();
		pnlBrush.setToolTipText("The brush which will be drawn when you left-click in the grid. Use the scroll wheel to change this.");
		pnlBrush.addMouseWheelListener(this);
		pnlBrush.setSize(160, 100);
		
		labBrush = new JLabel("Undefined");

		btnSaveGridWorld = new JButton("Save Grid World");
		btnSaveGridWorld.setToolTipText("Save the current grid world to a file to load later.");
		btnSaveGridWorld.addActionListener(this);

		btnLoadGridWorld = new JButton("Load Grid World");
		btnLoadGridWorld.setToolTipText("Load a grid world from a file.");
		btnLoadGridWorld.addActionListener(this);

		btnExportRawMDP = new JButton("Export Raw MDP");
		btnExportRawMDP.setToolTipText("Export the underlying MDP as a raw text file, with state transitions and rewards, for use by a planner.");
		btnExportRawMDP.addActionListener(this);

		btnExportRawSSP = new JButton("Export Raw SSP");
		btnExportRawSSP.setToolTipText("Export the underlying SSP as a raw text file, with state transitions and costs, for use by a planner.");
		btnExportRawSSP.addActionListener(this);

		btnImportMDPSSPPolicy = new JButton("Import MDP/SSP Policy");
		btnImportMDPSSPPolicy.setToolTipText("Import a policy from a file which was solved using an exported raw MDP/SSP file.");
		btnImportMDPSSPPolicy.addActionListener(this);

		btnExportRawPOMDP = new JButton("Export Raw POMDP");
		btnExportRawPOMDP.setToolTipText("Export the underlying POMDP as a raw text file, with state transitions, observation transitions, and rewards, for use by a planner.");
		btnExportRawPOMDP.addActionListener(this);

		btnImportPOMDPPolicy = new JButton("Import POMDP Policy");
		btnImportPOMDPPolicy.setToolTipText("Import a policy from a file which was solved using an exported raw POMDP file.");
		btnImportPOMDPPolicy.addActionListener(this);

		btnPlayPause = new JButton("Play");
		btnPlayPause.setToolTipText("Play or pause the robot animation.");
		btnPlayPause.addActionListener(this);

		sldSpeed = new JSlider();
		sldSpeed.setToolTipText("Adjust the speed of the robot.");
		sldSpeed.setValue(4);
		sldSpeed.setMaximum(8);
		sldSpeed.setSnapToTicks(true);
		sldSpeed.addChangeListener(this);

		JLabel lblSpeed = new JLabel("Speed:");

		GroupLayout gl_pnlControl = new GroupLayout(pnlControl);
		gl_pnlControl.setHorizontalGroup(
			gl_pnlControl.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlControl.createSequentialGroup()
					.addGroup(gl_pnlControl.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlControl.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_pnlControl.createParallelGroup(Alignment.LEADING)
								.addComponent(lblWidth)
								.addComponent(lblHeight))
							.addGap(18)
							.addGroup(gl_pnlControl.createParallelGroup(Alignment.TRAILING)
								.addComponent(sprWidth, GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
								.addComponent(sprHeight, GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)))
						.addComponent(labBrush, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(pnlBrush, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(btnSaveGridWorld, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(btnLoadGridWorld, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(btnExportRawMDP, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(btnExportRawSSP, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_pnlControl.createSequentialGroup()
							.addComponent(btnImportMDPSSPPolicy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(1))
						.addComponent(btnExportRawPOMDP, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_pnlControl.createSequentialGroup()
							.addComponent(btnImportPOMDPPolicy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(1))
						.addGroup(Alignment.TRAILING, gl_pnlControl.createSequentialGroup()
							.addGap(1)
							.addComponent(btnPlayPause, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE))
						.addGroup(gl_pnlControl.createSequentialGroup()
							.addContainerGap()
							.addComponent(lblSpeed)
							.addGap(18)
							.addComponent(sldSpeed, GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)))
					.addContainerGap())
		);
		gl_pnlControl.setVerticalGroup(
			gl_pnlControl.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlControl.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnlControl.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblWidth)
						.addComponent(sprWidth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlControl.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblHeight)
						.addComponent(sprHeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(labBrush)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(pnlBrush)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnSaveGridWorld)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLoadGridWorld)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnExportRawMDP)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnExportRawSSP)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnImportMDPSSPPolicy)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnExportRawPOMDP)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnImportPOMDPPolicy)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnPlayPause)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_pnlControl.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_pnlControl.createSequentialGroup()
							.addComponent(lblSpeed)
							.addContainerGap())
						.addComponent(sldSpeed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
		);
		pnlControl.setLayout(gl_pnlControl);

		pnlGrid = new GridPanel((int)sprWidth.getValue(), (int)sprHeight.getValue());
		pnlGrid.setBorder(null);
		splMain.setRightComponent(pnlGrid);
		GroupLayout gl_pnlVisualization = new GroupLayout(pnlGrid);
		gl_pnlVisualization.setHorizontalGroup(
			gl_pnlVisualization.createParallelGroup(Alignment.LEADING)
				.addGap(0, 477, Short.MAX_VALUE)
		);
		gl_pnlVisualization.setVerticalGroup(
			gl_pnlVisualization.createParallelGroup(Alignment.LEADING)
				.addGap(0, 330, Short.MAX_VALUE)
		);
		pnlGrid.setLayout(gl_pnlVisualization);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnSaveGridWorld) {
			final JFileChooser fc = new JFileChooser();
			try {
				fc.setSelectedFile(new File(currentFile.getAbsolutePath().substring(0, currentFile.getAbsolutePath().lastIndexOf('.')) + ".gw"));
			} catch (Exception ex) { }
			
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				pnlGrid.saveGrid(currentFile);
			}
		} else if (e.getSource() == btnLoadGridWorld) {
			final JFileChooser fc = new JFileChooser();
			try {
				fc.setSelectedFile(new File(currentFile.getAbsolutePath().substring(0, currentFile.getAbsolutePath().lastIndexOf('.')) + ".gw"));
			} catch (Exception ex) { }
			
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				// Load, and upon success, set the width and height of the spinners.
				currentFile = fc.getSelectedFile();
				boolean success = pnlGrid.loadGrid(currentFile);
				
				if (success) {
					locked = true;
					sprWidth.setValue((int)pnlGrid.getGridWidth());
					sprHeight.setValue((int)pnlGrid.getGridHeight());
					locked = false;
				}
			}
		} else if (e.getSource() == btnExportRawMDP) {
			GridMarkov mdp = new GridMarkov();
			mdp.create(pnlGrid.getGrid(), pnlGrid.getGridWidth(), pnlGrid.getGridHeight(), false);
			
			final JFileChooser fc = new JFileChooser();
			try {
				fc.setSelectedFile(new File(currentFile.getAbsolutePath().substring(0, currentFile.getAbsolutePath().lastIndexOf('.')) + ".raw"));
			} catch (Exception ex) { }
			
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				mdp.saveMDP(currentFile);
			}
		} else if (e.getSource() == btnExportRawSSP) {
			GridMarkov mdp = new GridMarkov();
			mdp.create(pnlGrid.getGrid(), pnlGrid.getGridWidth(), pnlGrid.getGridHeight(), true);
			
			final JFileChooser fc = new JFileChooser();
			try {
				fc.setSelectedFile(new File(currentFile.getAbsolutePath().substring(0, currentFile.getAbsolutePath().lastIndexOf('.')) + ".raw"));
			} catch (Exception ex) { }
			
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				mdp.saveMDP(currentFile);
			}
		} else if (e.getSource() == btnImportMDPSSPPolicy) {

		} else if (e.getSource() == btnExportRawPOMDP) {
			GridMarkov pomdp = new GridMarkov();
			pomdp.create(pnlGrid.getGrid(), pnlGrid.getGridWidth(), pnlGrid.getGridHeight(), false);
			
			final JFileChooser fc = new JFileChooser();
			try {
				fc.setSelectedFile(new File(currentFile.getAbsolutePath().substring(0, currentFile.getAbsolutePath().lastIndexOf('.')) + ".raw"));
			} catch (Exception ex) { }
			
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				currentFile = fc.getSelectedFile();
				pomdp.savePOMDP(currentFile);
			}
		} else if (e.getSource() == btnImportPOMDPPolicy) {

		} else if (e.getSource() == btnPlayPause) {
			if (btnPlayPause.getText().equals("Play")) {
				btnPlayPause.setText("Pause");
			} else {
				btnPlayPause.setText("Play");
			}
			pnlGrid.toggleRobot();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == sprWidth || e.getSource() == sprHeight) {
			if (!locked) {
				pnlGrid.update((int)sprWidth.getValue(), (int)sprHeight.getValue());
			}
			pnlGrid.repaint();
		} else if (e.getSource() == sldSpeed) {
			pnlGrid.setSpeed((sldSpeed.getValue() + 1) * 250);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		if (e.getSource() == sprWidth || e.getSource() == sprHeight) {
			if (!locked) {
				pnlGrid.update((int)sprWidth.getValue(), (int)sprHeight.getValue());
			}
			pnlGrid.repaint();
		}
	}

	@Override
	public void keyPressed(KeyEvent e) { }

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.getSource() == pnlBrush) {
			if (e.getWheelRotation() > 0) {
				pnlGrid.incrementBrush();
			} else {
				pnlGrid.decrementBrush();
			}
			pnlBrush.setBackground(pnlGrid.getBrushColor());
			pnlBrush.repaint();
			
			labBrush.setText("Brush: " + pnlGrid.getBrushName());
		}
	}

}
