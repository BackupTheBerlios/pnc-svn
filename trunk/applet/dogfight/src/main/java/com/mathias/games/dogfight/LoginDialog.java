package com.mathias.games.dogfight;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mathias.drawutils.FormDialog;

public class LoginDialog extends FormDialog {

	private static final long serialVersionUID = 8760776430254915007L;

	private JTextField mUserField = new JTextField(20);

	private JPasswordField mPwdField = new JPasswordField(20);

	public LoginDialog() {
		super("Login", false);
		initUI(262, 90);
	}

	public String getUsername() {
		return mUserField.getText().trim();
	}

	public String getPassword() {
		return new String(mPwdField.getPassword());
	}

	protected boolean validateDialog() {
		return true;
	}

	protected void setupForm() {
		addItem("User:", mUserField);
		addItem("Password:", mPwdField);
	}

}
