package com.stephenwranger.compgeo.algorithms.convexhull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.compgeo.algorithms.AlgorithmUtils;
import com.stephenwranger.graphics.math.Tuple2d;

public class ConvexHullJarvisMarch implements Algorithm<Tuple2d> {
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

      int lowest = 0;

      for (int i = 1; i < input.size(); i++) {
         if (input.get(i).x < input.get(lowest).x) {
            lowest = i;
         } else if (input.get(i).x == input.get(lowest).x && input.get(i).y < input.get(lowest).y) {
            lowest = i;
         }
      }

      final int[] next = new int[input.size()];
      Arrays.fill(next, -1);

      int i = 0;
      next[0] = lowest;

      try {
         do {
            if ((System.nanoTime() - startTime) / 1000000l > timeout) {
               return false;
            }

            next[i + 1] = ConvexHullJarvisMarch.nextPoint(input, next[i]);
            i++;
         } while (next[i] != next[0]);
      } catch (final Exception e) {
         e.printStackTrace();
         for (final int index : next) {
            if (index != -1) {
               System.err.println(index + ", " + input.get(index));
            }
         }

         for (final Tuple2d t : input) {
            System.err.println(t);
         }

         return false;
      }

      Tuple2d point;

      for (final int index : next) {
         if (index != -1) {
            point = input.get(index);
            minX = Math.min(minX, point.x);
            maxX = Math.max(maxX, point.x);
            minY = Math.min(minY, point.y);
            maxY = Math.max(maxY, point.y);
            output.add(input.get(index));
         }
      }

      final double centerX = (maxX - minX) / 2.0 + minX;
      final double centerY = (maxY - minY) / 2.0 + minY;
      Collections.sort(output, AlgorithmUtils.getComparator(new Tuple2d(centerX, centerY)));

      return true;
   }

   public static int nextPoint(final List<Tuple2d> input, final int p) {
      int q = p;
      int turn;

      for (int r = 0; r < input.size(); r++) {
         if (r != q) {
            turn = ConvexHullJarvisMarch.turn(input.get(p), input.get(r), input.get(q));

            if (turn == -1 || (turn == 0 && ConvexHullJarvisMarch.dist(input, p, r) > ConvexHullJarvisMarch.dist(input, p, q))) {
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
