package com.mathias.drawutils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class FormDialog extends GenericDialog implements DocumentListener, ActionListener {

	private JPanel mMainPanel;

	private JButton mOkButton;

	private boolean mIsCancelled = true;

	private int mIndex;

	private ArrayList<JTextField> mTextFields = new ArrayList<JTextField>();

	public FormDialog(String title, boolean resizable) {
		super(title, resizable);
	}
	
	public boolean isCancelled() {
		return mIsCancelled;
	}

	public void changedUpdate(DocumentEvent e) {
		handleEnabling();
	}

	public void insertUpdate(DocumentEvent e) {
		handleEnabling();
	}

	public void removeUpdate(DocumentEvent e) {
		handleEnabling();
	}

	public void actionPerformed(ActionEvent e) {
		mOkButton.doClick();
	}

	protected abstract boolean validateDialog();

	protected abstract void setupForm();

	protected void addItem(Component component) {
		mMainPanel.add(component, Util.getGBC(0, mIndex, 2, 1, true, new Insets(4, 0, 0, 0)));
		mIndex++;
	}

	protected void addItem(String text, Component component) {
		mMainPanel.add(new JLabel(text), Util.getGBC(0, mIndex, false, new Insets(8, 0, 0, 0)));
		mMainPanel.add(component, Util.getGBC(1, mIndex, true, new Insets(8, 4, 0, 0)));
		mIndex++;

		if (component instanceof JTextField) {
			JTextField field = (JTextField) component;
			field.getDocument().addDocumentListener(this);
			field.addActionListener(this);
			mTextFields.add(field);
		}
	}

	protected void setupUI() {
		mMainPanel = new JPanel(new GridBagLayout());
		mMainPanel.setBorder(new EmptyBorder(6, 10, 10, 10));

		setupForm();

		mOkButton = new JButton("OK");
		mOkButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleClosing(false);
			}
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleClosing(true);
			}
		});

		mOkButton.setPreferredSize(cancelButton.getPreferredSize());
		mOkButton.setMinimumSize(cancelButton.getMinimumSize());

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		buttonPanel.setBorder(new EmptyBorder(2, 10, 10, 10));

		buttonPanel.add(new JLabel(""), Util.getGBC(0, 0, true, null));
		buttonPanel.add(mOkButton, Util.getGBC(1, 0, false, new Insets(0, 4, 0, 0)));
		buttonPanel.add(cancelButton, Util.getGBC(2, 0, false, new Insets(0, 4, 0, 0)));

		handleEnabling();

		if (mTextFields.size() != 0) {
			JTextField field = mTextFields.get(0);
			field.requestFocus();
			field.selectAll();
		}

		setComponent(mMainPanel, BorderLayout.CENTER);
		setComponent(buttonPanel, BorderLayout.SOUTH);
	}

	private void handleClosing(boolean cancelled) {
		mIsCancelled = cancelled;
		if (mIsCancelled) {
			setVisible(false);
		} else {
			if (validateDialog()) {
				setVisible(false);
			}
		}
	}

	private void handleEnabling() {
		for (JTextField f : mTextFields) {
			if (Util.isBlank(f.getText())) {
				mOkButton.setEnabled(false);
				return;
			}
		}
		mOkButton.setEnabled(true);
	}
	
}
