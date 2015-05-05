package com.stephenwranger.compgeo.assignment2;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.AlgorithmUtils;
import com.stephenwranger.compgeo.algorithms.segments.BruteForceSegmentIntersectionAlgorithm;
import com.stephenwranger.compgeo.algorithms.segments.LineSweepSegmentIntersectionAlgorithm;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.utils.TimeUtils;

public class Assignment2Benchmarks {

   public static void main(final String[] args) {
      final List<LineSegment> input = new ArrayList<LineSegment>();
      final int[] testSizes = new int[] { 10, 100, 1000 };// , 10000, 100000 };
      int runCount = 10;

      if (args.length >= 1) {
         try {
            final int count = Integer.parseInt(args[0]);
            System.out.println("run count: " + count);
            runCount = count;
         } catch (final Exception e) {
            // nothing; no count was given or was invalid
         }
      }

      final BruteForceSegmentIntersectionAlgorithm bruteforce = new BruteForceSegmentIntersectionAlgorithm();
      final LineSweepSegmentIntersectionAlgorithm lineSweep = new LineSweepSegmentIntersectionAlgorithm();

      final List<Long> bruteforceDurations = new ArrayList<Long>();
      final List<Long> lineSweepDurations = new ArrayList<Long>();
      boolean bfValid = true;
      boolean lsValid = true;
      final JProgressBar tests = new JProgressBar(0, testSizes.length - 1);
      tests.setStringPainted(true);
      tests.setString("0%");
      final JProgressBar runs = new JProgressBar(0, runCount - 1);
      runs.setStringPainted(true);
      runs.setString("0%");

      if (args.length >= 1 && args[args.length - 1].equals("--ui")) {
         final JFrame frame = new JFrame("Benchmark Status");
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               tests.setPreferredSize(new Dimension(400, 100));
               runs.setPreferredSize(new Dimension(400, 100));
               frame.getContentPane().add(tests);
               frame.getContentPane().add(runs);
               frame.setLayout(new GridLayout(0, 1));
               frame.setLocation(200, 200);
               frame.pack();
               frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
               frame.setVisible(true);
            }
         });
      }

      for(int i = 0; i < testSizes.length; i++) {
         bruteforceDurations.clear();
         lineSweepDurations.clear();

         for(int j = 0; j < runCount; j++) {
            input.clear();
            AlgorithmUtils.getRandomSegments(testSizes[i], input);

            final List<Tuple2d> output = null;

            if (bfValid) {
               bruteforceDurations.add(Assignment2Benchmarks.run(bruteforce, input, output));
               if (bruteforceDurations.get(j) == -1) {
                  bfValid = false;
               }
            }

            if (lsValid) {
               lineSweepDurations.add(Assignment2Benchmarks.run(lineSweep, input, output));
               if (lineSweepDurations.get(j) == -1) {
                  lsValid = false;
               }
            }

            runs.setValue(j + 1);
            runs.setString(((j + 1) / runCount * 100.0) + "%");
         }
         System.out.println("\nPoint Count: " + testSizes[i]);
         System.out.println("Runs: " + runCount);
         System.out.println("Brute Force Average:  "
               + ((bfValid) ? TimeUtils.formatNanoseconds(Assignment2Benchmarks.average(bruteforceDurations)) + "("
                     + Assignment2Benchmarks.average(bruteforceDurations) + " ns)" : "Timed Out"));
         System.out.println("Line Sweep Average:   "
               + ((lsValid) ? TimeUtils.formatNanoseconds(Assignment2Benchmarks.average(lineSweepDurations)) + "("
                     + Assignment2Benchmarks.average(lineSweepDurations) + " ns)" : "Timed Out"));

         tests.setValue(i + 1);
         tests.setString((((i + 1) / testSizes.length) * 100.0) + "%");
      }
   }

   private static long run(final Algorithm<LineSegment, Tuple2d> algorithm, final List<LineSegment> input, final List<Tuple2d> output) {
      if (output != null) {
         output.clear();
      }

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
