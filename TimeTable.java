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
	private int min, step;
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
		String logFileName = "logsForKfuS93.txt";

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

		String capButton[] = {"Load", "Start", "Step", "Print", "Continue", "Log", "Exit"};
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

		field[0].setText("19");
		field[1].setText("461");
		field[2].setText("kfu-s-93.stu");
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

	private void applyUpdates() {
		for (int i = 1; i < courses.length(); i++) {
			int[] currentTimeslot = courses.getTimeSlot(courses.slot(i));
			int suggestedIndex = autoassociator.unitUpdate(currentTimeslot);
			if (suggestedIndex != courses.slot(i)) {
				courses.setSlot(i, suggestedIndex);
			}
		}
		draw();
	}

	public void actionPerformed(ActionEvent click) {
		int clashes;
		String slotsInput = field[0].getText();
		String coursesInput = field[1].getText();
		String clashFileInput = field[2].getText();
		String itersInput = field[3].getText();
		String shiftsInput = field[4].getText();

		String logFileName = "logsForKfuS93.txt";

		switch (getButtonIndex((JButton) click.getSource())) {
			case 0:
				int slots = Integer.parseInt(field[0].getText());
				courses = new CourseArray(Integer.parseInt(field[1].getText()) + 1, slots);
				courses.readClashes(field[2].getText());
				autoassociator = new Autoassociator(courses);
				train();
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
				try{
					BufferedWriter writer = new BufferedWriter(new FileWriter(logFileName));
					System.out.println("Exam\tSlot\tClashes");
					for (int i = 1; i < courses.length(); i++) {
						writer.write("Exam: " + i + " Slot: " + courses.slot(i) + " Clashes: " + courses.status(i)+"\n");
						System.out.println(i + "\t" + courses.slot(i) + "\t" + courses.status(i));
					}
					writer.close();
				} catch (IOException e) {
					System.err.println("Error writing to log file: " + e.getMessage());
				}
				break;
			case 4:
				if (!shiftsInput.isEmpty()) {
					for (int iteration = 1; iteration <= Integer.parseInt(itersInput); iteration++) {
						courses.iterate(Integer.parseInt(shiftsInput));
						applyUpdates();
						draw();
						clashes = courses.clashesLeft();
						if (clashes < min) {
							min = clashes;
							step = iteration;
						}
					}
					System.out.println("Shift: " + shiftsInput + "\tMin clashes: " + min + "\tStep: " + step);
					setVisible(true);
				} else {
					System.out.println("Please enter a value for shift field!");
				}
				break;
			case 5:
				System.out.println("Logging started");
				try (BufferedWriter writer = new BufferedWriter(new FileWriter("logsForKfuS93.txt"))) {
					writer.write("Here you can find the logs of the algorithm:\n");
					writer.write("File: " + field[2].getText() + "\n");
					writer.write("Slots: " + field[0].getText() + "\n");
					writer.write("Courses: " + field[1].getText() + "\n");
					writer.write("Iterations: " + field[3].getText() + "\n");
					writer.write("Shifts: " + field[4].getText() + "\n");
					writer.write("Clashes: " + courses.clashesLeft() + "\n");
					writer.write("Slots:"+"\n");
					for (int i = 1; i < courses.length(); i++) {
						writer.write(i + "\t" + courses.slot(i) + "\t" + courses.status(i) + "\n");
					}
					writer.write("\n");

					writer.write("Min num of clashes: " + min + "\tStep: " + step + "\n");
					System.out.println("Min num of clashes: " + min + "\tStep: " + step);
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 6:
				System.exit(0);
		}

	}



	public void train() {
		String numOfSlots = field[0].getText();
		for (int i = 0; i <Integer.parseInt(numOfSlots); i++) {
			int[] timeSlots =courses.getTimeSlot(i);
			if(noClashes(timeSlots)) {autoassociator.training(timeSlots);}
		}
	}

	private boolean noClashes(int[] timeSlots) {
		for (int i = 0; i <timeSlots.length; i++) {
			if (timeSlots[i] == 1) {
				if (courses.maxClashSize(i) > 0) {
					return false;
				}
			}
		}
		return true;
	}

	public static void main(String[] args) {
		new TimeTable();
	}
}
