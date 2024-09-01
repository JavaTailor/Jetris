package config;

import utils.Status;

public class RandomManager {
    public static long UniversialRandomSeed = -1;
    public static void setRandomSeed(long seed) {

        UniversialRandomSeed = Status.initialSeed;
        MainRandom.setRandomSeed(UniversialRandomSeed);
        FuzzingRandom.setRandomSeed(UniversialRandomSeed);
        ExecutionRandom.setRandomSeed(UniversialRandomSeed);
    }
    public static void updateSeed(long seed) {

        UniversialRandomSeed = seed;
        MainRandom.updateSeed(UniversialRandomSeed);
        FuzzingRandom.updateSeed(UniversialRandomSeed);
        ExecutionRandom.updateSeed(UniversialRandomSeed);
    }
}
