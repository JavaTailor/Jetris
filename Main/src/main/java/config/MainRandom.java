package config;

import java.util.Random;

public class MainRandom {

    private static long randomSeed = -1;
    public static Random random;

    public static void setRandomSeed(long randomSeed) {
        MainRandom.randomSeed = randomSeed;
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

        if (upperBound == 0) {
            return 0;
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
                System.err.println("seed" + seed);
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
}
