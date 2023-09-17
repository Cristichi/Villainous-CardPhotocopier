package es.cristichi;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class MainInfoFrame extends JFrame {

	private static final long serialVersionUID = -3540314050900361822L;

	public MainInfoFrame(String windowName) {
		setName(windowName);
		setTitle(windowName);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new GridLayout(1, 1));

		JLabel label = new JLabel("Starting...");
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setBorder(new EmptyBorder(1, 5, 1, 5));
		add(label);

		setMinimumSize(new Dimension(500, 200));
		setMaximumSize(new Dimension(800, 500));
		setPreferredSize(new Dimension(500, 200));

		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setVisible(true);
	}

	public synchronized void replaceText(String... text) {
		getContentPane().removeAll();
		setLayout(new GridLayout(text.length, 1));
		for (String line : text) {
			JLabel lbl = new JLabel("<html>" + line + "</div></html>");
			lbl.setBorder(new EmptyBorder(1, 5, 1, 5));
			add(lbl);
		}
		getContentPane().repaint();
	}

	public synchronized void replaceText(String subtitle, List<String> text) {
		getContentPane().removeAll();
		setLayout(new GridLayout(text.size() + 1, 1));
		JLabel warningTitle = new JLabel("<html>"+subtitle+"<html>");
		warningTitle.setBorder(new EmptyBorder(2, 5, 2, 5));
		add(warningTitle);
		int index = 0;
		for (String line : text) {
			JLabel lbl = new JLabel("<html>" + (++index) + ": " + line + "</html>");
			lbl.setBorder(new EmptyBorder(1, 5, 1, 5));
			add(lbl);
		}
		getContentPane().repaint();
	}
}
