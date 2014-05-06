package hcc.stepuplife;

import java.util.Random;

public class ExerciseRecommender {
//	private static final int exerciseImageids[] = { R.drawable.exercise1,
//			R.drawable.exercise2, R.drawable.exercise3, R.drawable.exercise4,
//			R.drawable.exercise5 };
//	private static final int exerciseImageids[] = { R.drawable.exercise1,
//		R.drawable.exercise2};
	
	private static final int exerciseImageAnimids[] = { R.drawable.lunges_anim,
		R.drawable.pushups_anim};
	
	ExerciseRecommender recommender;
	private static boolean init = false;
	private static Random randomNumberGen;

//	public static int getRandomExerciseId() {
//		if (!init) {
//			randomNumberGen = new Random();
//			init = true;
//		}
//		return exerciseImageids[Math.abs(randomNumberGen.nextInt()) % exerciseImageids.length];
//	}

	public static int getRandomExerciseAnimId() {
		// TODO Auto-generated method stub
		if (!init) {
			randomNumberGen = new Random();
			init = true;
		}
		return exerciseImageAnimids[Math.abs(randomNumberGen.nextInt()) % exerciseImageAnimids.length];
	}
}