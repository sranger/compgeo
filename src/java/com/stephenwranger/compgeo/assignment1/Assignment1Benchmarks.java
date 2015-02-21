package com.stephenwranger.compgeo.assignment1;

import java.util.ArrayList;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.AlgorithmUtils;
import com.stephenwranger.compgeo.algorithms.convexhull.ConvexHullBruteForce;
import com.stephenwranger.compgeo.algorithms.convexhull.ConvexHullJarvisMarch;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.utils.TimeUtils;

public class Assignment1Benchmarks {

   public static void main(String[] args) {
      final List<Tuple2d> input = new ArrayList<Tuple2d>();
      final int[] testSizes = new int[] { 10,100,1000 };
      final int runCount = 1000;

      final ConvexHullBruteForce bruteforce = new ConvexHullBruteForce();
      final ConvexHullJarvisMarch jarvis = new ConvexHullJarvisMarch();

      final List<Long> bruteforceDurations = new ArrayList<Long>();
      final List<Long> jarvisDurations = new ArrayList<Long>();

      for(int i = 0; i < testSizes.length; i++) {
         for(int j = 0; j < runCount; j++) {
            input.clear();
            AlgorithmUtils.getRandomPoints(testSizes[i], input);

            final List<Tuple2d> output = new ArrayList<Tuple2d>();

            bruteforceDurations.add(Assignment1Benchmarks.run(bruteforce, input, output));
            jarvisDurations.add(Assignment1Benchmarks.run(jarvis, input, output));
         }
         System.out.println("\nPoint Count: " + testSizes[i]);
         System.out.println("Runs: " + runCount);
         System.out.println("Brute Force Average:  " + TimeUtils.formatNanoseconds(Assignment1Benchmarks.average(bruteforceDurations)));
         System.out.println("Jarvis March Average: " + TimeUtils.formatNanoseconds(Assignment1Benchmarks.average(jarvisDurations)));
      }
   }

   private static long run(final Algorithm<Tuple2d> algorithm, final List<Tuple2d> input, final List<Tuple2d> output) {
      output.clear();

      final long startTime = System.nanoTime();
      algorithm.compute(input, output);
      final long endTime = System.nanoTime();
      return endTime - startTime;
   }

   private static long average(final List<Long> values) {
      long total = 0;

      for(final long value : values) {
         total += value;
      }

      return total / values.size();
   }
}
