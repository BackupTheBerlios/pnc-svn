package convertit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import convertit.util.Des;

public class CenterPanel extends JPanel {

	public CenterPanel(){
		final JTextField keyField = new JTextField("Key");
		add(keyField);

		final JTextField dataField = new JTextField("Data"); 
		add(dataField);

		final JTextField outputField = new JTextField("Output"); 
		add(outputField);

		add(createButton(new AbstractAction("Sec Attr"){
			@Override
			public void actionPerformed(ActionEvent e) {
				outputField.setText("WEEE");
			}
		}));

		add(createButton(new AbstractAction("DES ENC"){
			@Override
			public void actionPerformed(ActionEvent e) {
				String key = keyField.getText();
				String data = dataField.getText();
				outputField.setText(new Des(key).encrypt(data));
			}
		}));

		add(createButton(new AbstractAction("DES DEC"){
			@Override
			public void actionPerformed(ActionEvent e) {

			}
		}));
	}

	private JButton createButton(Action action) {
		JButton button = new JButton(action);
		//button.setText("");
		//button.setMargin(new Insets(0, 0, 0, 0));
		//button.setFocusable(false);
		return button;
	}

}
