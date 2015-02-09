package com.stephenwranger.compgeo.assignment1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.convexhull.ConvexHullBruteForce;
import com.stephenwranger.compgeo.algorithms.convexhull.ConvexHullGrahamsScan;
import com.stephenwranger.compgeo.algorithms.convexhull.ConvexHullJarvisMarch;
import com.stephenwranger.graphics.Scene2d;
import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.renderables.Graph;
import com.stephenwranger.graphics.utils.TimeUtils;

public class Assignment1 {
   public static final String USAGE_STRING = "Usage: Assignment1 <algorighm> <filename>\nUsage: Assignment1 <algorighm> <point count>";

   public enum ConvexHullAlgorithm {
      BruteForce, GrahamsScan, JarvisMarch
   };

   public static void main(final String[] args) {
      if (args.length < 1) {
         System.err.println("Algorithm and Filename missing");
         throw new InvalidParameterException(Assignment1.USAGE_STRING);
      }

      final ConvexHullAlgorithm algorithmType = ConvexHullAlgorithm.valueOf(args[0]);
      int pointCount = 0;

      try {
         pointCount = Integer.parseInt(args[1]);
      } catch (final NumberFormatException e) {
         // nothing; check file type
         pointCount = -1;
      }

      InputStream inputFile = null;

      if (pointCount == -1) {
         inputFile = Assignment1.class.getResourceAsStream(args[1]);

         if (inputFile == null) {
            System.err.println("Arguments must contain point count or input file; both invalid.");
            System.err.println("Point Count invalid or File " + args[1] + " does not exist.");
            throw new InvalidParameterException(Assignment1.USAGE_STRING);
         }
      }

      if(algorithmType == null) {
         System.err.println("Algorithm " + args[0] + " invalid; acceptable values are: " + Arrays.toString(ConvexHullAlgorithm.values()) + ".");
         throw new InvalidParameterException(Assignment1.USAGE_STRING);
      }

      Algorithm<Tuple2d> algorithm = null;

      switch(algorithmType) {
         case BruteForce:
            algorithm = new ConvexHullBruteForce();
            break;
         case GrahamsScan:
            algorithm = new ConvexHullGrahamsScan();
            break;
         case JarvisMarch:
            algorithm = new ConvexHullJarvisMarch();
            break;
      }

      if(algorithm != null) {
         final List<Tuple2d> input = new ArrayList<Tuple2d>();

         if (pointCount == -1) {
            try (final BufferedReader fin = new BufferedReader(new InputStreamReader(inputFile))) {
               pointCount = Integer.parseInt(fin.readLine());
               String[] values;

               for (int i = 0; i < pointCount; i++) {
                  values = fin.readLine().split(" ");
                  input.add(new Tuple2d(Integer.parseInt(values[0]), Integer.parseInt(values[1])));
               }
            } catch (final IOException e) {
               e.printStackTrace();
            }
         } else {
            final int range = (int) Math.pow(10.0, Math.floor(Math.log10(pointCount)));
            System.out.println("range: " + range);
            final Random random = new Random();

            for (int i = 0; i < pointCount; i++) {
               input.add(new Tuple2d(random.nextInt(range), random.nextInt(range)));
            }
         }

         final List<Tuple2d> output = new ArrayList<Tuple2d>();
         final List<Pair<Tuple2d, Tuple2d>> outputEdges = new ArrayList<>();

         final long startTime = System.nanoTime();
         algorithm.compute(input, output, outputEdges);
         final long endTime = System.nanoTime();
         final long duration = endTime - startTime;

         System.out.println("Complete");
         System.out.println("Duration: " + TimeUtils.formatNanoseconds(duration) + " (" + duration + "ns)");
         System.out.println("Count points: " + output.size());
         System.out.println("Count edges: " + outputEdges.size());
         System.out.println("Output Points");

         try (final BufferedWriter fout = new BufferedWriter(new FileWriter("output.txt"))) {
            fout.write(output.size() + "\n");

            for (final Tuple2d value : output) {
               System.out.println("\t" + value);
               fout.write(value.x + " " + value.y + "\n");
            }
         } catch (final IOException e) {
            e.printStackTrace();
         }

         final JFrame frame = new JFrame("Computational Geometry: Assignment 1");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

         final Scene2d scene2d = new Scene2d(1600, 1000);
         scene2d.addRenderable2d(new Graph(input, outputEdges));

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
