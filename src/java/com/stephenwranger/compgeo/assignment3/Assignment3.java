package com.stephenwranger.compgeo.assignment3;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.stephenwranger.compgeo.algorithms.trapezoids.TrapezoidalMapAlgorithm;
import com.stephenwranger.graphics.Scene2d;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Trapezoid;
import com.stephenwranger.graphics.utils.TimeUtils;

public class Assignment3 {
   private static final String USAGE_STRING = (System.getProperty("os.name").toLowerCase().contains("windows")) ? "run_assignment3.bat X:/path/to/inputFile"
         : "./run_assignment3.sh </path/to/inputFile>";

   public static void main(final String[] args) {
      if (args.length == 0 || args.length > 2) {
         System.err.println("Usage: " + Assignment3.USAGE_STRING);
         System.exit(1);
      }

      final File file = new File(args[0]);

      if(!file.exists() || !file.isFile()) {
         System.err.println("Input File specified " + ((!file.exists()) ? "does not exist." : "is not a file. ") + file.getAbsolutePath());
         System.exit(1);
      }

      boolean showUi = false;

      if (args.length == 2) {
         if (args[1].equals("--ui")) {
            showUi = true;
         }
      }

      final List<LineSegment> segments = new ArrayList<LineSegment>();
      Rectangle2D bounds = null;
      int numberOfSegments = 0;

      try (final BufferedReader fin = new BufferedReader(new FileReader(file));) {
         numberOfSegments = Integer.parseInt(fin.readLine());
         final String[] boundValues = fin.readLine().split(" ");
         bounds = new Rectangle(Integer.parseInt(boundValues[0]), Integer.parseInt(boundValues[1]), Integer.parseInt(boundValues[2]),
               Integer.parseInt(boundValues[3]));
         String[] segment;
         Tuple2d v1, v2;

         for (int i = 0; i < numberOfSegments; i++) {
            segment = fin.readLine().split(" ");
            v1 = new Tuple2d(Integer.parseInt(segment[0]), Integer.parseInt(segment[1]));
            v2 = new Tuple2d(Integer.parseInt(segment[2]), Integer.parseInt(segment[3]));
            segments.add(new LineSegment(v1, v2));
         }
      } catch (final IOException e) {
         e.printStackTrace();
         return;
      }

      System.out.println("numberOfSegments: " + numberOfSegments);
      System.out.println("bounds: " + bounds);
      System.out.println("segments");
      for (final LineSegment s : segments) {
         System.out.println("\t" + s);
      }

      final List<String[]> output = new ArrayList<String[]>();
      final TrapezoidalMapAlgorithm algorithm = new TrapezoidalMapAlgorithm(bounds);
      final long startTime = System.nanoTime();

      algorithm.compute(segments, output, 5 * 60 * 1000);
      final long endTime = System.nanoTime();
      final long duration = endTime - startTime;

      System.out.println("Complete");
      System.out.println("Duration: " + TimeUtils.formatNanoseconds(duration) + " (" + duration + "ns)");

      final File outfile = new File("assignment3.csv");

      if (showUi) {
         final JFrame frame = new JFrame("Computational Geometry: Assignment 2");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

         final Scene2d scene2d = new Scene2d(1000, 1000);

         for (final Trapezoid t : algorithm.trapezoids) {
            t.setScale(10);
            t.setLineWidth(1f);
            scene2d.addRenderable2d(t);
         }

         for (final LineSegment s : segments) {
            s.setScale(10);
            s.setLineWidth(6f);
            scene2d.addRenderable2d(s);
         }

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

      try (final BufferedWriter fout = new BufferedWriter(new FileWriter(outfile))) {
         for(final String[] row : output) {
            for (int j = 0; j < row.length; j++) {
               fout.write(row[j]);

               if (j < row.length - 1) {
                  fout.write(",");
               }
            }

            fout.write("\n");
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }

      try (final BufferedReader fin = new BufferedReader(new InputStreamReader(System.in))) {
         String[] split;
         double x = -1, y = -1;

         while(true) {
            System.out.print("\nInput Point Query [x y] (exit to quit): ");
            split = fin.readLine().split(" ");

            if (split.length == 1 && split[0].equals("exit")) {
               break;
            }

            if(split.length != 2) {
               System.err.println("Invalid input, please enter only x and y values separated with a space then press enter to submit request.");
               continue;
            }

            try {
               x = Double.parseDouble(split[0]);
               y = Double.parseDouble(split[1]);
            } catch (final Exception e) {
               System.err.println("Only numeric values accepted.");
               continue;
            }

            algorithm.printQuery(new Tuple2d(x, y));
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }
   }
}
