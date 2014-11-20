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
import java.awt.event.TextEvent;
import java.awt.event.TextListener;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JSpinner;
import javax.swing.JButton;
import javax.swing.JSlider;


public class GridWorld implements ActionListener, ChangeListener, KeyListener {

	private JFrame fraMain;
	private GridPanel pnlGrid;

	private JSpinner sprWidth;
	private JSpinner sprHeight;

	private JButton btnSaveGridWorld;
	private JButton btnLoadGridWorld;
	private JButton btnExportRawMdp;
	private JButton btnImportMdpPolicy;

	private JButton btnPlayPause;
	private JSlider sldSpeed;

	/**
	 * When you load a grid world, you need to lock the controls (e.g., spinners)
	 * since you must update their value, but not execute their update code in
	 * the listener functions like actionPerformed, etc.
	 */
	private boolean locked = false;

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
		fraMain.setBounds(100, 100, 600, 380);
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

		btnSaveGridWorld = new JButton("Save Grid World");
		btnSaveGridWorld.setToolTipText("Save the current grid world to a file to load later.");
		btnSaveGridWorld.addActionListener(this);

		btnLoadGridWorld = new JButton("Load Grid World");
		btnLoadGridWorld.setToolTipText("Load a grid world from a file.");
		btnLoadGridWorld.addActionListener(this);

		btnExportRawMdp = new JButton("Export Raw MDP");
		btnExportRawMdp.setToolTipText("Export the underlying MDP as a raw text file, with state transitions and rewards, for use by a planner.");
		btnExportRawMdp.addActionListener(this);

		btnImportMdpPolicy = new JButton("Import MDP Policy");
		btnImportMdpPolicy.setToolTipText("Import a policy from a file which was solved using an exported raw MDP file.");
		btnImportMdpPolicy.addActionListener(this);

		btnPlayPause = new JButton("Play");
		btnPlayPause.setToolTipText("Play or pause the robot animation.");
		btnPlayPause.addActionListener(this);

		sldSpeed = new JSlider();
		sldSpeed.setToolTipText("Adjust the speed of the robot.");
		sldSpeed.setValue(4);
		sldSpeed.setMaximum(8);
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
						.addComponent(btnSaveGridWorld, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addComponent(btnLoadGridWorld, GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
						.addComponent(btnExportRawMdp, GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_pnlControl.createSequentialGroup()
							.addComponent(btnImportMdpPolicy, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
							.addGap(1))
						.addGroup(Alignment.TRAILING, gl_pnlControl.createSequentialGroup()
							.addGap(1)
							.addComponent(btnPlayPause, GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE))
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
					.addComponent(btnSaveGridWorld)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnLoadGridWorld)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnExportRawMdp)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnImportMdpPolicy)
					.addPreferredGap(ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
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
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				pnlGrid.saveGrid(fc.getSelectedFile());
			}
		} else if (e.getSource() == btnLoadGridWorld) {
			final JFileChooser fc = new JFileChooser();
			if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
				// Load, and upon success, set the width and height of the spinners.
				boolean success = pnlGrid.loadGrid(fc.getSelectedFile());
				if (success) {
					locked = true;
					sprWidth.setValue((int)pnlGrid.getGridWidth());
					sprHeight.setValue((int)pnlGrid.getGridHeight());
					locked = false;
				}
			}
		} else if (e.getSource() == btnExportRawMdp) {
			GridMDP mdp = new GridMDP();
			mdp.create(pnlGrid.getGrid(), pnlGrid.getGridWidth(), pnlGrid.getGridHeight());
			final JFileChooser fc = new JFileChooser();
			if (fc.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
				mdp.saveMDP(fc.getSelectedFile());
			}
		} else if (e.getSource() == btnImportMdpPolicy) {

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

}
