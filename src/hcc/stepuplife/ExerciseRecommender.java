package hcc.stepuplife;

import java.util.Random;

public class ExerciseRecommender {
	private static final int exerciseImageids[] = { R.drawable.exercise1,
			R.drawable.exercise2, R.drawable.exercise3, R.drawable.exercise4,
			R.drawable.exercise5 };
	ExerciseRecommender recommender;
	private static boolean init = false;
	private static Random randomNumberGen;

	public static int getRandomExerciseId() {
		if (!init) {
			randomNumberGen = new Random();
			init = true;
		}
		return exerciseImageids[randomNumberGen.nextInt() % 5];
	}
}