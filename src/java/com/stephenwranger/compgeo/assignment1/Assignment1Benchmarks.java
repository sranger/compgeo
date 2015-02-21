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
      final int[] testSizes = new int[] { 10, 100, 1000, 10000, 100000 };
      final int runCount = 10;

      final ConvexHullBruteForce bruteforce = new ConvexHullBruteForce();
      final ConvexHullJarvisMarch jarvis = new ConvexHullJarvisMarch();

      final List<Long> bruteforceDurations = new ArrayList<Long>();
      final List<Long> jarvisDurations = new ArrayList<Long>();
      boolean bfValid = true;
      boolean jmValid = true;

      for(int i = 0; i < testSizes.length; i++) {
         bruteforceDurations.clear();
         jarvisDurations.clear();

         for(int j = 0; j < runCount; j++) {
            input.clear();
            AlgorithmUtils.getRandomPoints(testSizes[i], input);

            final List<Tuple2d> output = new ArrayList<Tuple2d>();

            if (bfValid) {
               bruteforceDurations.add(Assignment1Benchmarks.run(bruteforce, input, output));
               if (bruteforceDurations.get(j) == -1) {
                  bfValid = false;
               }
            }

            if (jmValid) {
               jarvisDurations.add(Assignment1Benchmarks.run(jarvis, input, output));
               if (jarvisDurations.get(j) == -1) {
                  jmValid = false;
               }
            }
         }
         System.out.println("\nPoint Count: " + testSizes[i]);
         System.out.println("Runs: " + runCount);
         System.out.println("Brute Force Average:  " + ((bfValid) ? TimeUtils.formatNanoseconds(Assignment1Benchmarks.average(bruteforceDurations)) : "Timed Out"));
         System.out.println("Jarvis March Average: " + ((jmValid) ? TimeUtils.formatNanoseconds(Assignment1Benchmarks.average(jarvisDurations)) : "Timed Out"));
      }
   }

   private static long run(final Algorithm<Tuple2d> algorithm, final List<Tuple2d> input, final List<Tuple2d> output) {
      output.clear();

      final long startTime = System.nanoTime();
      final boolean success = algorithm.compute(input, output, 5 * 60 * 1000);
      final long endTime = System.nanoTime();
      return (success) ? endTime - startTime : -1;
   }

   private static long average(final List<Long> values) {
      long total = 0;

      for(final long value : values) {
         total += value;
      }

      return total / values.size();
   }
}
