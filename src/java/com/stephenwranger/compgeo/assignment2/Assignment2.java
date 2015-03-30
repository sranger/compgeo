package com.stephenwranger.compgeo.assignment2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.AlgorithmUtils;
import com.stephenwranger.compgeo.algorithms.segments.BruteForceSegmentIntersectionAlgorithm;
import com.stephenwranger.compgeo.algorithms.segments.LineSweepSegmentIntersectionAlgorithm;
import com.stephenwranger.graphics.Scene2d;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.renderables.Graph;
import com.stephenwranger.graphics.utils.TimeUtils;

public class Assignment2 {
   public static final String USAGE_STRING = "Usage: Assignment2 <algorighm> <filename>\nUsage: Assignment2 <algorighm> <segment count> [--ui]";

   public enum LineSegmentAlgorithm {
      LineSweepSegmentIntersection, BruteForceSegmentIntersection
   };

   public static void main(final String[] args) {
      if (args.length < 1) {
         System.err.println("Algorithm and Filename missing");
         throw new InvalidParameterException(Assignment2.USAGE_STRING);
      }

      final LineSegmentAlgorithm algorithmType = LineSegmentAlgorithm.valueOf(args[0]);
      int segmentCount = 0;

      try {
         segmentCount = Integer.parseInt(args[1]);
      } catch (final NumberFormatException e) {
         // nothing; check file type
         segmentCount = -1;
      }

      InputStream inputFile = null;

      if (segmentCount == -1) {
         inputFile = Assignment2.class.getResourceAsStream(args[1]);

         if (inputFile == null) {
            try {
               inputFile = new FileInputStream(args[1]);
            } catch (final FileNotFoundException e) {
               e.printStackTrace();
               inputFile = null;
            }
            if (inputFile == null) {
               System.err.println("Arguments must contain point count or input file; both invalid.");
               System.err.println("Point Count invalid or File " + args[1] + " does not exist.");
               throw new InvalidParameterException(Assignment2.USAGE_STRING);
            }
         }
      }

      if(algorithmType == null) {
         System.err.println("Algorithm " + args[0] + " invalid; acceptable values are: " + Arrays.toString(LineSegmentAlgorithm.values()) + ".");
         throw new InvalidParameterException(Assignment2.USAGE_STRING);
      }

      final boolean showUi = (args.length >= 3 && args[2].equals("--ui"));

      Algorithm<LineSegment, Tuple2d> algorithm = null;

      switch(algorithmType) {
         case BruteForceSegmentIntersection:
            algorithm = new BruteForceSegmentIntersectionAlgorithm();
            break;
         case LineSweepSegmentIntersection:
            algorithm = new LineSweepSegmentIntersectionAlgorithm();
            break;
      }

      if(algorithm != null) {
         final List<LineSegment> input = new ArrayList<LineSegment>();

         if (segmentCount == -1) {
            try (final BufferedReader fin = new BufferedReader(new InputStreamReader(inputFile))) {
               segmentCount = Integer.parseInt(fin.readLine());
               String[] values;
               Tuple2d p1, p2;

               for (int i = 0; i < segmentCount; i++) {
                  values = fin.readLine().split(" ");
                  do {
                     p1 = new Tuple2d(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
                     p2 = new Tuple2d(Integer.parseInt(values[2]), Integer.parseInt(values[3]));
                  } while (p1.x == p2.x);
                  input.add(new LineSegment(p1, p2));
               }
            } catch (final IOException e) {
               e.printStackTrace();
            }
         } else {
            AlgorithmUtils.getRandomSegments(segmentCount, input);
         }

         final List<Tuple2d> output = new ArrayList<Tuple2d>();
         final long startTime = System.nanoTime();
         algorithm.compute(input, output, 5 * 60 * 1000);
         final long endTime = System.nanoTime();
         final long duration = endTime - startTime;

         System.out.println("Complete");
         System.out.println("Duration: " + TimeUtils.formatNanoseconds(duration) + " (" + duration + "ns)");
         System.out.println("Count points: " + output.size());
         System.out.println("Output Points");

         Collections.sort(output, AlgorithmUtils.getXAxisComparator());

         try (final BufferedWriter fout = new BufferedWriter(new FileWriter("output_" + algorithmType.name() + "_" + input.size() + ".txt"))) {
            fout.write(output.size() + "\n");

            for (final Tuple2d value : output) {
               System.out.println("\t" + value);
               fout.write(value.x + " " + value.y + "\n");
            }
         } catch (final IOException e) {
            e.printStackTrace();
         }

         if(showUi) {
            final JFrame frame = new JFrame("Computational Geometry: Assignment 2");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            final Scene2d scene2d = new Scene2d(1600, 1000);
            scene2d.addRenderable2d(new Graph(output, input));

            frame.getContentPane().add(scene2d);

            SwingUtilities.invokeLater(new Runnable() {
               @Override
               public void run() {
                  frame.pack();
                  frame.setLocation(100, 100);
                  frame.setVisible(true);
               }
            });
         }
      }
   }
}
