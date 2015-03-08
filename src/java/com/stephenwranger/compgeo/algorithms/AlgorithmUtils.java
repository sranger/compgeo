package com.stephenwranger.compgeo.algorithms;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.Vector2d;
import com.stephenwranger.graphics.renderables.LineSegment;

public class AlgorithmUtils {
   private AlgorithmUtils() {
      // statics only
   }

   public static void getRandomPoints(final int pointCount, final List<Tuple2d> output) {
      final int range = (int) Math.pow(10.0, Math.floor(Math.log10(pointCount)));
      final Random random = new Random();
      Tuple2d point;

      for (int i = 0; i < pointCount; i++) {
         do {
            point = new Tuple2d(random.nextDouble() * range, random.nextDouble() * range);
         } while (output.contains(point));

         output.add(point);
      }
   }

   public static void getRandomSegments(final int segmentCount, final List<LineSegment> output) {
      final int range = (int) Math.pow(10.0, Math.floor(Math.log10(segmentCount)));
      final Random random = new Random();
      Tuple2d p1, p2;

      for (int i = 0; i < segmentCount; i++) {
         do {
            p1 = new Tuple2d(random.nextDouble() * range, random.nextDouble() * range);
            p2 = new Tuple2d(random.nextDouble() * range, random.nextDouble() * range);
         } while (p1.x == p2.x);

         output.add(new LineSegment(p1, p2));
      }
   }

   public static Comparator<Tuple2d> getXAxisComparator() {
      return new Comparator<Tuple2d>() {
         @Override
         public int compare(final Tuple2d o1, final Tuple2d o2) {
            return o1.x < o2.x ? -1 : o1.x == o2.x ? 0 : 1;
         }
      };
   }

   public static Comparator<Tuple2d> getAngleComparator(final Tuple2d origin) {
      final Vector2d up = new Vector2d(0,1);

      return new Comparator<Tuple2d>() {
         @Override
         public int compare(final Tuple2d p0, final Tuple2d p1) {
            final Vector2d v0 = new Vector2d(p0.x - origin.x, p0.y - origin.y).normalize();
            final Vector2d v1 = new Vector2d(p1.x - origin.x, p1.y - origin.y).normalize();
            double angle0 = up.angleSigned(v0);
            double angle1 = up.angleSigned(v1);

            if(angle0 < 0) {
               angle0 = Math.PI + (Math.PI - Math.abs(angle0));
            }

            if(angle1 < 0) {
               angle1 = Math.PI + (Math.PI - Math.abs(angle1));
            }

            return (angle0 > angle1) ? -1 : (angle0 == angle1) ? 0 : 1;
         }
      };
   }
}
