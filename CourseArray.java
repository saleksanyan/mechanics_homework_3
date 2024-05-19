import java.io.*;
import java.util.Objects;
import java.util.StringTokenizer;

public class CourseArray {

	private Course elements[];
	private int period;

	public CourseArray(int numOfCourses, int numOfSlots) {
		period = numOfSlots;
		elements = new Course[numOfCourses];
		for (int i = 1; i < elements.length; i++)
			elements[i] = new Course();
	}

	public void readClashes(String filename) {
		try {
			System.out.println("File name: " + filename);
			String filePath = Objects.requireNonNull(CourseArray.class.getResource(filename)).getPath();
			BufferedReader file =new BufferedReader(new FileReader(filePath));
			StringTokenizer line = new StringTokenizer(file.readLine());
			int count = line.countTokens(), i, j, k;
			int[] index;
			while (count > 0) {
				if (count > 1) {
					index = new int[count];
					i = 0;
					while (line.hasMoreTokens()) {
						index[i] = Integer.parseInt(line.nextToken());
						i++;
					}

					for (i = 0; i < index.length; i++)
						for (j = 0; j < index.length; j++)
							if (j != i) {
								k = 0;
								while (k < elements[index[i]].clashesWith.size()
										&& elements[index[i]].clashesWith.elementAt(k) != elements[index[j]])
									k++;
								if (k == elements[index[i]].clashesWith.size())
									elements[index[i]].addClash(elements[index[j]]);
							}

				}
				line = new StringTokenizer(file.readLine());
				count = line.countTokens();
			}
			file.close();
		}
		catch (NumberFormatException numberFormatException) {
			System.out.println(numberFormatException);
		}
		catch (FileNotFoundException fileNotFoundException) {
			System.out.println(fileNotFoundException.getMessage());
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}

	public int length() {
		return elements.length;
	}

	public int status(int index) {
		return elements[index].clashSize();
	}

	public int slot(int index) {
		return elements[index].mySlot;
	}

	public void setSlot(int index, int newSlot) {
		if (newSlot >= 0 && newSlot < period) {
			elements[index].mySlot = newSlot;
		} else {
			elements[index].mySlot = Math.max(0, Math.min(newSlot, period - 1));
		}
	}


	public int maxClashSize(int index) {
		return elements[index] == null || elements[index].clashesWith.isEmpty() ? 0 : elements[index].clashesWith.size();
	}

	public int getLength() {
		return elements.length;
	}

	public int clashesLeft() {
		int result = 0;
		for (int i = 1; i < elements.length; i++)
			result += elements[i].clashSize();

		return result;
	}

	public int[] getTimeSlot(int index) {
		int[] timeSlot = new int[elements.length];
		for (int i = 1; i < elements.length; i++) {
			timeSlot[i] = elements[i].mySlot == index ? 1 : -1;
		}
		return timeSlot;
	}


	public void iterate(int shifts) {
		if(elements!= null) {
			for (int index = 1; index < elements.length; index++) {
				elements[index].setForce();
				for (int move = 1; move <= shifts && elements[index].force != 0; move++) {
					elements[index].setForce();
					elements[index].shift(period);
				}
			}
		}
	}

	public void printResult() {
		for (int i = 1; i < elements.length; i++)
			System.out.println(i + "\t" + elements[i].mySlot);
	}
}