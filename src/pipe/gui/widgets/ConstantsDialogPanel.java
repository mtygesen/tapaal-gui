package pipe.gui.widgets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Pattern;

import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.NumberFormatter;

import pipe.gui.CreateGui;
import dk.aau.cs.gui.undo.Command;
import dk.aau.cs.model.tapn.Constant;
import dk.aau.cs.model.tapn.TimedArcPetriNetNetwork;

/*
 * LeftConstantsPane.java
 *
 * Created on 08-10-2009, 13:51:42
 */

/**
 * 
 * @author Morten Jacobsen
 */
public class ConstantsDialogPanel extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6734583459331431789L;
	private JRootPane rootPane;
	private TimedArcPetriNetNetwork model;
	private int lowerBound;
	private int upperBound;

	private String oldName;

	/** Creates new form LeftConstantsPane */

	public ConstantsDialogPanel() {
		initComponents();
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model) {
		initComponents();

		rootPane = pane;
		this.model = model;

		this.oldName = "";

		// Set up initial values
		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1);
		valueSpinner.setModel(spinnerModel);
		setupValueEditor();

		nameTextField.setText(oldName);

		// wire up buttons
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				onOK();
			}
		});

		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exit();
			}
		});

		rootPane.setDefaultButton(okButton);
	}

	private void setupValueEditor() {
		valueSpinner.setEditor(new JSpinner.NumberEditor(valueSpinner));
		
		// Disable nonnumeric keys in value spinner
		JFormattedTextField txt = ((JSpinner.NumberEditor) valueSpinner.getEditor()).getTextField();
		((NumberFormatter) txt.getFormatter()).setAllowsInvalid(false);
	}

	public ConstantsDialogPanel(JRootPane pane, TimedArcPetriNetNetwork model,
			Constant constant) {
		this(pane, model);

		this.oldName = constant.name();
		this.lowerBound = constant.lowerBound();
		this.upperBound = constant.upperBound();

		SpinnerNumberModel spinnerModel = new SpinnerNumberModel(constant
				.value(), 0, constant.upperBound(), 1);
		valueSpinner.setModel(spinnerModel);
		setupValueEditor();
		nameTextField.setText(oldName);
	}

	public void onOK() {
		String newName = nameTextField.getText();

		if (!Pattern.matches("[a-zA-Z]([\\_a-zA-Z0-9])*", newName)) {

			System.err
					.println("Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z]*");
			JOptionPane
					.showMessageDialog(
							CreateGui.getApp(),
							"Acceptable names for constants are defined by the regular expression:\n[a-zA-Z][_a-zA-Z0-9]*",
							"Error", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		if (newName.trim().isEmpty()) {
			JOptionPane.showMessageDialog(CreateGui.getApp(),
					"You must specify a name.", "Missing name",
					JOptionPane.ERROR_MESSAGE);
		} else {
			int val = (Integer) valueSpinner.getValue();

			if (!oldName.equals("")) {
				if (!oldName.equals(newName)
						&& model.isConstantNameUsed(newName)) {
					JOptionPane
							.showMessageDialog(
									CreateGui.getApp(),
									"There is already another constant with the same name.\n\n"
											+ "Please choose a different name for the constant.",
									"Error", JOptionPane.INFORMATION_MESSAGE);
					nameTextField.setText(oldName);
					return;
				}
				
				//Kyrke - This is messy, but a quck fix for bug #815487			
				//Check that the value is within the allowed bounds
				if (!( this.lowerBound <= val && val <= this.upperBound )){
					
					JOptionPane.showMessageDialog(
							CreateGui.getApp(),
							"The specified value is invalid for the current net.\n"
									+ "Updating the constant to the specified value invalidates the guard\n"
									+ "on one or more arcs.",
							"Constant value invalid for current net",
							JOptionPane.ERROR_MESSAGE);
					return;
					
				}
				
				Command edit = model.updateConstant(oldName, new Constant(
						newName, val));
				if (edit == null) {
					JOptionPane
							.showMessageDialog(
									CreateGui.getApp(),
									"The specified value is invalid for the current net.\n"
											+ "Updating the constant to the specified value invalidates the guard\n"
											+ "on one or more arcs.",
									"Constant value invalid for current net",
									JOptionPane.ERROR_MESSAGE);
					return;
				} else {
					CreateGui.getCurrentTab().drawingSurface().getUndoManager()
							.addNewEdit(edit);
					CreateGui.getCurrentTab().drawingSurface().repaintAll();
				}
				
				
				
			} else {
				Command edit = model.addConstant(newName, val);
				if (edit == null) {
					JOptionPane
							.showMessageDialog(
									CreateGui.getApp(),
									"A constant with the specified name already exists.",
									"Constant exists",
									JOptionPane.ERROR_MESSAGE);
				} else
					CreateGui.getView().getUndoManager().addNewEdit(edit);
			}

			model.buildConstraints();
		}

		exit();
	}

	public void exit() {
		rootPane.getParent().setVisible(false);
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed"
	// desc="Generated Code">//GEN-BEGIN:initComponents
	private void initComponents() {

		nameTextField = new javax.swing.JTextField();
		Dimension size = new Dimension(125, 25);
		nameTextField.setPreferredSize(size);
		nameTextField.setMinimumSize(size);
		nameLabel = new javax.swing.JLabel();
		valueLabel = new javax.swing.JLabel();
		valueSpinner = new javax.swing.JSpinner();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();

		nameLabel.setText("Name:");

		valueLabel.setText("Value:");

		okButton.setText("OK");

		cancelButton.setText("Cancel");

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
		this.setLayout(layout);
		layout
				.setHorizontalGroup(layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.LEADING)
														.addGroup(
																layout
																		.createSequentialGroup()
																		.addComponent(
																				okButton)
																		.addPreferredGap(
																				javax.swing.LayoutStyle.ComponentPlacement.RELATED)
																		.addComponent(
																				cancelButton))
														.addGroup(
																layout
																		.createParallelGroup(
																				javax.swing.GroupLayout.Alignment.LEADING,
																				false)
																		.addGroup(
																				layout
																						.createSequentialGroup()
																						.addComponent(
																								nameLabel)
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																						.addComponent(
																								nameTextField,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								javax.swing.GroupLayout.PREFERRED_SIZE))
																		.addGroup(
																				layout
																						.createSequentialGroup()
																						.addComponent(
																								valueLabel)
																						.addPreferredGap(
																								javax.swing.LayoutStyle.ComponentPlacement.RELATED,
																								javax.swing.GroupLayout.DEFAULT_SIZE,
																								Short.MAX_VALUE)
																						.addComponent(
																								valueSpinner,
																								javax.swing.GroupLayout.PREFERRED_SIZE,
																								78,
																								javax.swing.GroupLayout.PREFERRED_SIZE))))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
		layout
				.setVerticalGroup(layout
						.createParallelGroup(
								javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(
								layout
										.createSequentialGroup()
										.addContainerGap()
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(nameLabel)
														.addComponent(
																nameTextField,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(
																valueLabel)
														.addComponent(
																valueSpinner,
																javax.swing.GroupLayout.PREFERRED_SIZE,
																javax.swing.GroupLayout.DEFAULT_SIZE,
																javax.swing.GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(
												javax.swing.LayoutStyle.ComponentPlacement.RELATED)
										.addGroup(
												layout
														.createParallelGroup(
																javax.swing.GroupLayout.Alignment.BASELINE)
														.addComponent(okButton)
														.addComponent(
																cancelButton))
										.addContainerGap(
												javax.swing.GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)));
	}// </editor-fold>//GEN-END:initComponents

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JButton cancelButton;
	private javax.swing.JLabel nameLabel;
	private javax.swing.JTextField nameTextField;
	private javax.swing.JButton okButton;
	private javax.swing.JLabel valueLabel;
	private javax.swing.JSpinner valueSpinner;
	// End of variables declaration//GEN-END:variables

}
