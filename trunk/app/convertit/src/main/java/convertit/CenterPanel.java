package convertit;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.mathias.util.Util;

import convertit.util.Des;

public class CenterPanel extends JPanel {

	public CenterPanel(){
		final JTextField keyField = new JTextField("Key", 20);
		add(keyField);

		final JTextField dataField = new JTextField("Data", 20); 
		add(dataField);

		final JTextField outputField = new JTextField("Output", 20); 
		add(outputField);

		add(createButton(new AbstractAction("Sec Attr"){
			public void actionPerformed(ActionEvent e) {
				outputField.setText("WEEE");
			}
		}));

		add(createButton(new AbstractAction("DES ENC"){
			public void actionPerformed(ActionEvent e) {
				String key = keyField.getText();
				String data = dataField.getText();
				outputField.setText(new Des(key).encrypt(data));
			}
		}));

		add(createButton(new AbstractAction("DES DEC"){
			public void actionPerformed(ActionEvent e) {

			}
		}));

		add(createButton(new AbstractAction("Hex2Text"){
			public void actionPerformed(ActionEvent e) {
				String data = dataField.getText();
				outputField.setText(data);
				outputField.setText(new String(Util.toBytes(data)));
			}
		}));

		add(createButton(new AbstractAction("Text2Hex"){
			public void actionPerformed(ActionEvent e) {
				String data = dataField.getText();
				outputField.setText(Util.toHex(data.getBytes()));
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
