import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class TimeTable extends JFrame implements ActionListener {

	private JPanel screen = new JPanel(), tools = new JPanel();
	private JButton tool[];
	private JTextField field[];
	private CourseArray courses;
	private Color CRScolor[] = {Color.RED, Color.GREEN, Color.BLACK};
	private JButton continueButton;
	private Autoassociator autoassociator;
	public TimeTable() {
		super("Dynamic Time Table");
		setSize(700, 800);
		setLayout(new BorderLayout());

		screen.setPreferredSize(new Dimension(400, 800));
		add(screen, BorderLayout.WEST);

		setTools();
		add(tools, BorderLayout.EAST);

		setVisible(true);
	}

	private void scheduling() {
		boolean improvement = true;
		int iterations = 0;
		String logFileName = "update_log.txt";

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName));

			while (improvement && courses != null && autoassociator != null) {
				improvement = false;
				int currentClashes = courses.clashesLeft();

				for (int i = 1; i < courses.length(); i++) {
					if (courses.status(i) > 0) {
						int originalSlot = courses.slot(i);
						int[] originalTimeslot = courses.getTimeSlot(i);

						for (int newSlot = 0; newSlot < Integer.parseInt(field[0].getText()); newSlot++) {
							if (newSlot != originalSlot) {
								courses.setSlot(i, newSlot);

								autoassociator.unitUpdate(originalTimeslot);

								if (courses.clashesLeft() < currentClashes) {
									draw();
									currentClashes = courses.clashesLeft();
									improvement = true;
									writer.write("Slots: " + field[0].getText() + ", Shift: " + field[4].getText() +
											", Iteration: " + iterations + ", Timeslot Index: " + i + "\n");
									break;
								} else {

									courses.setSlot(i, originalSlot);
								}
							}
						}
					}
				}
				iterations++;
				if (iterations > Integer.parseInt(field[3].getText())) {
					break;
				}
			}
			System.out.println("Continued scheduling with " + iterations + " iterations resulting in " + courses.clashesLeft() + " remaining clashes.");
			writer.close();
		} catch (IOException e) {
			System.err.println("Error writing to log file: " + e.getMessage());
		}

	}

	public void setTools() {
		String capField[] = {"Slots:", "Courses:", "Clash File:", "Iters:", "Shift:"};
		field = new JTextField[capField.length];

		String capButton[] = {"Load", "Start", "Step", "Print", "Continue", "Exit"};
		tool = new JButton[capButton.length];

		tools.setLayout(new GridLayout(capField.length + capButton.length, 1));

		for (int i = 0; i < field.length; i++) {
			JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			panel.add(new JLabel(capField[i]));
			field[i] = new JTextField(10);
			panel.add(field[i]);
			tools.add(panel);
		}

		for (int i = 0; i < tool.length; i++) {
			tool[i] = new JButton(capButton[i]);
			tool[i].addActionListener(this);
			tools.add(tool[i]);
		}

		field[0].setText("17");
		field[1].setText("381");
		field[2].setText("ear-f-83.stu");
		field[3].setText("1");
	}

	public void draw() {
		Graphics g = screen.getGraphics();
		int width = Integer.parseInt(field[0].getText()) * 10;
		for (int courseIndex = 1; courseIndex < courses.length(); courseIndex++) {
			g.setColor(CRScolor[courses.status(courseIndex) > 0 ? 0 : 1]);
			g.drawLine(0, courseIndex, width, courseIndex);
			g.setColor(CRScolor[CRScolor.length - 1]);
			g.drawLine(10 * courses.slot(courseIndex), courseIndex, 10 * courses.slot(courseIndex) + 10, courseIndex);
		}
	}
	
	private int getButtonIndex(JButton source) {
		int result = 0;
		while (source != tool[result]) result++;
		return result;
	}
	
	public void actionPerformed(ActionEvent click) {
		int min, step, clashes;
		
		switch (getButtonIndex((JButton) click.getSource())) {
		case 0:
			int slots = Integer.parseInt(field[0].getText());
			courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
			courses.readClashes(field[2].getText());
			draw();
			break;
		case 1:
			min = Integer.MAX_VALUE;
			step = 0;
			for (int i = 1; i < courses.length(); i++) courses.setSlot(i, 0);
			
			for (int iteration = 1; iteration <= Integer.parseInt(field[3].getText()); iteration++) {
				courses.iterate(Integer.parseInt(field[4].getText()));
				draw();
				clashes = courses.clashesLeft();
				if (clashes < min) {
					min = clashes;
					step = iteration;
				}
			}
			System.out.println("Shift = " + field[4].getText() + "\tMin clashes = " + min + "\tat step " + step);
			setVisible(true);
			break;
		case 2:
			courses.iterate(Integer.parseInt(field[4].getText()));
			draw();
			break;
		case 3:
			System.out.println("Exam\tSlot\tClashes");
			for (int i = 1; i < courses.length(); i++)
				System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
			break;
		case 4:
			scheduling();
			break;
		case 5:
			System.exit(0);
		}
	}

	public static void main(String[] args) {
		new TimeTable();
	}
}
