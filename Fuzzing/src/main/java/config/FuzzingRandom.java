package config;

import org.junit.Test;

import java.util.*;

public class FuzzingRandom {

    private static int probability;
    private static long randomSeed = -1;
    private static Random random;
    public static int getProbability() {
        return probability;
    }
    public static void setProbability(int probability) {
        FuzzingRandom.probability = probability;
    }
    public static long getRandomSeed() {
        return randomSeed;
    }
    public static void setRandomSeed(long seed) {
        randomSeed = seed;
    }
    public void setRandom(Random rnd) {
        random = rnd;
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

    public static boolean flipCoin(int prob) {
        if (random == null){
            getRandom();
        }
        return random.nextInt(100) < prob;
    }
    public static boolean flipCoin() {
        if (random == null){
            getRandom();
        }
        if (probability <= 0){
            probability = 50;
        }
        int prob = random.nextInt(100);
        return prob < probability;
    }
    public static boolean randomUpTo(int prob) {
        return FuzzingRandom.nextChoice(100) < prob;
    }

    /**
     * roulette wheel selection
     * @param group
     * @return
     */
    public static <K> K randomUpTo(HashMap<K, Integer> group) {

        List<Map.Entry<K, Integer>> values = new ArrayList<>(group.entrySet());
        Collections.sort(values, new Comparator<Map.Entry<K, Integer>>(){
            @Override
            public int compare(Map.Entry<K, Integer> o1, Map.Entry<K, Integer> o2) {
                return (o1.getValue() - o2.getValue());
            }
        });
        int choice = FuzzingRandom.nextChoice(100);
        for (Map.Entry<K, Integer> value : values) {
            if (choice < value.getValue()) {
                return value.getKey();
            }
        }
        //this should not happen
        return (K) group.keySet().toArray()[FuzzingRandom.nextChoice(group.keySet().size())];
    }

    @Test
    public void test(){
        randomUpTo(FuzzingConfig.PROB_ARITH_GROUP);
    }
}
