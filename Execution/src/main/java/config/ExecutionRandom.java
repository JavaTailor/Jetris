package config;

import java.util.Random;

public class ExecutionRandom {
    private static long randomSeed = -1;
    public static Random random;

    public static void setRandomSeed(long randomSeed) {
        ExecutionRandom.randomSeed = randomSeed;
    }
    public static long getRandomSeed() {
        return randomSeed;
    }
    public static int nextChoice(int bound) {

        if (bound == 0) {
            return 0;
        }
        if (random == null){
            getRandom();
        }
        return random.nextInt(bound);
    }

    public static int nextChoice(int lowerBound, int upperBound) {

        if (lowerBound < 0) {
            lowerBound = 0;
        }
        if (upperBound == 0) {
            return 0;
        }
        if (upperBound == Integer.MAX_VALUE) {
            upperBound = Integer.MAX_VALUE - 1;
        }
        if (random == null){
            getRandom();
        }
        return random.nextInt(upperBound - lowerBound + 1) + lowerBound;
    }

    public static Random getRandom() {

        if (random != null) {
            return random;
        } else {
            if (randomSeed == -1) {
                long seed = System.currentTimeMillis();
                System.err.println("seed: " + seed);
                random = new Random(seed);
            } else {
                random = new Random(randomSeed);
            }
        }
        return random;
    }
    public static void updateSeed(long seed) {
        if (random != null) {
            random.setSeed(seed);
        } else {
            random = new Random(seed);
        }
    }

    public static boolean flipCoin() {
        if (random == null){
            getRandom();
        }
        int prob = random.nextInt(100);
        return prob < 50;
    }

    public static boolean flipCoin(int prob) {
        if (random == null){
            getRandom();
        }
        return random.nextInt(100) < prob;
    }
}
