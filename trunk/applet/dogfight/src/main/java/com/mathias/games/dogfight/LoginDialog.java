package com.mathias.games.dogfight;

import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.mathias.drawutils.FormDialog;

public class LoginDialog extends FormDialog {

	private JTextField mUserField = new JTextField(20);

	private JPasswordField mPwdField = new JPasswordField(20);

	public LoginDialog() {
		super("Login", false);
		initUI(262, 90);
	}

	public String getCredentials() {
		char[] pwd = mPwdField.getPassword();
		return mUserField.getText().trim() + "|" + new String(pwd);
	}

	protected boolean validateDialog() {
		return true;
	}

	protected void setupForm() {
		addItem("User:", mUserField);
		addItem("Password:", mPwdField);
	}

}
