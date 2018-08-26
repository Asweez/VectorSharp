package vectorsharp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class Display implements ActionListener, KeyListener, WindowListener {
	private JFrame frame;
	private JTextPane textArea;
	private JButton compile;
	private JButton save;
	private JTextArea compilerOutput;

	public void display(String code) {
		frame = new JFrame("Vector# Editor");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 500);
		frame.setMinimumSize(new Dimension(300, 200));
		frame.setLocation(frame.getToolkit().getScreenSize().width / 2 - 400, frame.getToolkit().getScreenSize().height / 2 - 250);
		frame.addWindowListener(this);
		
		JPanel panel = new JPanel();
		frame.add(panel);
		panel.setLocation(400, 250);
		panel.setBackground(Color.red);
		panel.setLayout(new GridBagLayout());

		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setText(code);
		textArea.setForeground(Color.white);
		textArea.setBackground(new Color(0.1f, 0.1f, 0.1f));
		textArea.setCaretColor(Color.white);

		formatAll();

		textArea.addKeyListener(this);

		compile = new JButton("Compile & Run");
		compile.addActionListener(this);
		save = new JButton("Save");
		save.addActionListener(this);
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.X_AXIS));
		tempPanel.add(save);
		tempPanel.add(compile);
		// compile.setPreferredSize(new Dimension(800, 20));

		compilerOutput = new JTextArea();
		compilerOutput.setEditable(false);
		compilerOutput.setForeground(Color.white);
		compilerOutput.setBackground(new Color(0.1f, 0.1f, 0.1f));

		JScrollPane temp1 = new JScrollPane(textArea);
		JScrollPane temp2 = new JScrollPane(compilerOutput);
		temp1.setPreferredSize(new Dimension(1, 1));
		tempPanel.setPreferredSize(new Dimension(1, 1));
		temp2.setPreferredSize(new Dimension(1, 1));
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 8;
		c.weighty = 0.6f;
		c.weightx = 1f;
		c.fill = GridBagConstraints.BOTH;
		panel.add(temp1, c);
		c.gridx = 0;
		c.gridy = 8;
		c.gridheight = 1;
		c.weighty = 0.1f;
		panel.add(tempPanel, c);
		c.gridx = 0;
		c.gridy = 9;
		c.weighty = 0.3f;
		c.gridheight = 5;
		panel.add(temp2, c);

		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(compile)) {
			save();
			compilerOutput.setText(VectorSharpCompiler.interpret(textArea.getText()));
		} else if (e.getSource().equals(save)) {
			save();
		}
	}

	private void save() {
		File file = new File("code.vsharp");
		PrintWriter out;
		try {
			out = new PrintWriter(file);
			out.println(textArea.getText());
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	public void formatAll() {
		int i = 0;
		String text = textArea.getText();
		while(i < text.length()){
			int[] indices = findWord(text, i);
			String word = text.substring(indices[0], indices[1]);
			for(Style s : styles){
				for(String s1 : s.applicableSubstrings){
					if(s1.equals(word)){
						textArea.getStyledDocument().setCharacterAttributes(indices[0], indices[1] - indices[0], s.attribute, true);
					}
				}
			}
			i = indices[1] + 1;
		}
	}
	
	public static int[] findWord(String s, int caretPosition){
		caretPosition--;
		while (caretPosition >= 0 && s.charAt(caretPosition) != ' ' && s.charAt(caretPosition) != '\n') {
			caretPosition--;
		}
		caretPosition++;
		int[] toReturn = new int[2];
		toReturn[0] = caretPosition;
		while(caretPosition < s.length() && s.charAt(caretPosition) != ' ' && s.charAt(caretPosition) != '\n'){
			caretPosition++;
		}
		toReturn[1] = caretPosition;
		return toReturn;
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		int[] indices = findWord(textArea.getText(), textArea.getCaretPosition());
		String word = textArea.getText().substring(indices[0], indices[1]);
		for (Style s : styles) {
			for(String s1 : s.applicableSubstrings){
				if(s1.equals(word)){
					textArea.getStyledDocument().setCharacterAttributes(indices[0], indices[1] - indices[0], s.attribute, true);
					return;
				}
			}
		}
		textArea.getStyledDocument().setCharacterAttributes(indices[0], indices[1] - indices[0], defaultAttr, true);
		textArea.setCharacterAttributes(defaultAttr, true);
	}

	public static final List<Style> styles = new ArrayList<>();
	public static SimpleAttributeSet defaultAttr;

	static {
		SimpleAttributeSet bold_green_attr = new SimpleAttributeSet();
		StyleConstants.setBold(bold_green_attr, true);
		StyleConstants.setForeground(bold_green_attr, new Color(0.5f, 1f, 0.5f));
		Style bold_green_style = new Style(bold_green_attr, new String[] { "vector"});
		styles.add(bold_green_style);
		
		SimpleAttributeSet bold_blue_attr = new SimpleAttributeSet();
		StyleConstants.setBold(bold_blue_attr, true);
		StyleConstants.setForeground(bold_blue_attr, new Color(0.5f, 0.5f, 1f));
		Style bold_blue_style = new Style(bold_blue_attr, new String[] { "double"});
		styles.add(bold_blue_style);
		
		SimpleAttributeSet red_attr = new SimpleAttributeSet();
		StyleConstants.setBold(red_attr, true);
		StyleConstants.setForeground(red_attr, new Color(1f, 0.5f, 0.5f));
		Style red_style = new Style(red_attr, new String[] { "print" });
		styles.add(red_style);
		
		defaultAttr = new SimpleAttributeSet();
		StyleConstants.setForeground(defaultAttr, Color.white);
	}

	public static class Style {
		public SimpleAttributeSet attribute;
		public String[] applicableSubstrings;

		public Style(SimpleAttributeSet attribute, String[] apStrings) {
			this.attribute = attribute;
			applicableSubstrings = apStrings;
		}

		public List<int[]> searchAllWords(String s) {
			List<int[]> toReturn = new ArrayList<>();
			int i = 0;
			while (i < s.length() - 1) {
				char c = s.charAt(i);
				while (c == ' ' || c == '\n') {
					i++;
					if (i >= s.length())
						return toReturn;
					c = s.charAt(i);
				}
				String s1 = "";
				while (c != ' ' && c != '\n') {
					s1 += c;
					if(i == s.length() - 1){
						break;
					}
					c = s.charAt(++i);
				}
				for (String s2 : applicableSubstrings) {
					if (s2.equals(s1)) {
						toReturn.add(new int[] { i - s2.length(), s2.length() });
					}
				}
			}
			return toReturn;
		}
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		save();
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}
