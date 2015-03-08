package com.stephenwranger.compgeo.algorithms.convexhull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.AlgorithmUtils;
import com.stephenwranger.graphics.math.Tuple2d;

public class ConvexHullJarvisMarch implements Algorithm<Tuple2d, Tuple2d> {
   public ConvexHullJarvisMarch() {
      // nothing
   }

   @Override
   public boolean compute(final List<Tuple2d> input, final List<Tuple2d> output, final long timeout) {
      double minX = Double.MAX_VALUE;
      double maxX = -Double.MAX_VALUE;
      double minY = Double.MAX_VALUE;
      double maxY = -Double.MAX_VALUE;

      final long startTime = System.nanoTime();

      Tuple2d lowest = input.get(0);

      for (final Tuple2d point : input) {
         if (point.x < lowest.x) {
            lowest = point;
         } else if (point.x == lowest.x && point.y < lowest.y) {
            lowest = point;
         }
      }

      final List<Tuple2d> results = new ArrayList<Tuple2d>();
      Tuple2d next = lowest;
      Tuple2d previous = null;

      try {
         do {
            results.add(next);
            previous = next;
            if ((System.nanoTime() - startTime) / 1000000l > timeout) {
               return false;
            }

            if (results.size() > input.size()) {
               throw new Exception("results > input");
            }

            next = ConvexHullJarvisMarch.nextPoint(input, previous);
         } while (next != lowest);
      } catch (final Exception e) {
         e.printStackTrace();
         for (final Tuple2d point : results) {
            System.err.println(point);
         }

         int ctr = 0;
         for (final Tuple2d t : input) {
            System.err.println(ctr + ": " + t);
            ctr++;
         }

         return false;
      }

      for (final Tuple2d point : results) {
         minX = Math.min(minX, point.x);
         maxX = Math.max(maxX, point.x);
         minY = Math.min(minY, point.y);
         maxY = Math.max(maxY, point.y);
         output.add(point);
      }

      final double centerX = (maxX - minX) / 2.0 + minX;
      final double centerY = (maxY - minY) / 2.0 + minY;
      Collections.sort(output, AlgorithmUtils.getAngleComparator(new Tuple2d(centerX, centerY)));

      return true;
   }

   public static Tuple2d nextPoint(final List<Tuple2d> input, final Tuple2d p) {
      Tuple2d q = p;
      int turn;

      for (final Tuple2d r : input) {
         if (r != q) {
            turn = ConvexHullJarvisMarch.turn(p, r, q);

            if (turn == -1 || (turn == 0 && ConvexHullJarvisMarch.dist(p, r) > ConvexHullJarvisMarch.dist(p, q))) {
               q = r;
            }
         }
      }

      return q;
   }

   public static double dist(final List<Tuple2d> input, final int index1, final int index2) {
      final Tuple2d t1 = input.get(index1);
      final Tuple2d t2 = input.get(index2);
      return ConvexHullJarvisMarch.dist(t1, t2);
   }

   public static double dist(final Tuple2d t1, final Tuple2d t2) {
      return t1.distanceSquared(t2);
   }

   /**
    * Returns -1,0,1 for ccw,no,cw turns.
    *
    * @param p
    * @param q
    * @param r
    * @return
    */
   public static int turn(final Tuple2d p, final Tuple2d q, final Tuple2d r) {
      final double val = ((q.y - p.y) * (r.x - q.x) - (q.x - p.x) * (r.y - q.y)) + 0;
      return (val == 0) ? 0 : (val < 0) ? -1 : 1;
   }
}
