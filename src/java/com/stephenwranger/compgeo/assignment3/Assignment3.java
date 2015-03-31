package com.stephenwranger.compgeo.assignment3;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.trapezoids.TrapezoidalMapAlgorithm;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.utils.TimeUtils;

public class Assignment3 {
   private static final String USAGE_STRING = (System.getProperty("os.name").toLowerCase().contains("windows")) ? "run_assignment3.bat X:/path/to/inputFile"
         : "./run_assignment3.sh </path/to/inputFile>";

   public static void main(final String[] args) {
      if(args.length == 0) {
         System.err.println("Usage: " + Assignment3.USAGE_STRING);
         System.exit(1);
      }

      final File file = new File(args[0]);

      if(!file.exists() || !file.isFile()) {
         System.err.println("Input File specified " + ((!file.exists()) ? "does not exist." : "is not a file. ") + file.getAbsolutePath());
         System.exit(1);
      }

      try (final BufferedReader fin = new BufferedReader(new FileReader(file));) {
         final int numberOfSegments = Integer.parseInt(fin.readLine());
         final String[] boundValues = fin.readLine().split(" ");
         final Rectangle2D bounds = new Rectangle(Integer.parseInt(boundValues[0]), Integer.parseInt(boundValues[1]), Integer.parseInt(boundValues[2]), Integer.parseInt(boundValues[3]));
         final List<LineSegment> segments = new ArrayList<LineSegment>();
         String[] segment;
         Tuple2d v1, v2;

         for (int i = 0; i < numberOfSegments; i++) {
            segment = fin.readLine().split(" ");
            v1 = new Tuple2d(Integer.parseInt(segment[0]), Integer.parseInt(segment[1]));
            v2 = new Tuple2d(Integer.parseInt(segment[2]), Integer.parseInt(segment[3]));
            segments.add(new LineSegment(v1, v2));
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
         file.delete();

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
         }
      } catch (final IOException e) {
         e.printStackTrace();
      }
   }
}
