import java.util.Random;

public class Autoassociator {
	private int weights[][];
	private int trainingCapacity;

	public Autoassociator(CourseArray courses) {
		int numCourses = courses.length();
		weights = new int[numCourses][numCourses];
		trainingCapacity = numCourses;
	}

	public int getTrainingCapacity() {
		return trainingCapacity;
	}

	public void training(int pattern[]) {
		for (int i = 0; i < pattern.length; i++) {
			for (int j = 0; j < pattern.length; j++) {
				if (i != j) {
					weights[i][j] += pattern[i] * pattern[j];
				}
			}
		}
	}

	public int unitUpdate(int neurons[]) {
		Random rand = new Random();
		int index = rand.nextInt(neurons.length);
		int sum = 0;

		for (int j = 0; j < neurons.length; j++) {
			sum += weights[index][j] * neurons[j];
		}

		neurons[index] = sum >= 0 ? 1 : -1;

		return index;
	}


	public void unitUpdate(int neurons[], int index) {
		int sum = 0;

		for (int j = 0; j < neurons.length; j++) {
			sum += weights[index][j] * neurons[j];
		}

		neurons[index] = sum >= 0 ? 1 : -1;
	}

	public void chainUpdate(int neurons[], int steps) {
		for (int i = 0; i < steps; i++) {
			int index = unitUpdate(neurons);
			unitUpdate(neurons, index);
		}
	}

	public void fullUpdate(int neurons[]) {
		boolean stable = false;

		while (!stable) {
			int[] oldState = neurons.clone();
			chainUpdate(neurons, 1);
			stable = java.util.Arrays.equals(oldState, neurons);
		}
	}
}
