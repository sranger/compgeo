package com.stephenwranger.compgeo.algorithms.delaunay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.convexhull.ConvexHullJarvisMarch;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.Triangle2D;
import com.stephenwranger.graphics.renderables.Circle;

public class DelaunayTriangulation {
   private final List<Tuple2d>    vertices  = new ArrayList<Tuple2d>();
   private final List<Triangle2D> triangles = new ArrayList<Triangle2D>();

   public DelaunayTriangulation() {

   }

   public void addVertex(final Tuple2d vertex) {
      this.vertices.add(vertex);

      if (this.vertices.size() == 3) {
         this.triangles.add(new Triangle2D(this.vertices.get(0), this.vertices.get(1), this.vertices.get(2)));
      } else if (this.vertices.size() > 3) {
         final List<Triangle2D> needsReplacing = new ArrayList<Triangle2D>();
         Circle c = null;

         for (final Triangle2D t : this.triangles) {
            c = t.getCircumscribedCircle();

            if (c.contains(vertex)) {
               needsReplacing.add(t);
            }
         }

         // TODO: add new triangles
         final List<Tuple2d> triVerts = new ArrayList<Tuple2d>();
         final List<Tuple2d> output = new ArrayList<Tuple2d>();

         for (final Triangle2D tri : needsReplacing) {
            triVerts.addAll(Arrays.asList(tri.getCorners()));
            this.triangles.remove(tri);
         }

         if (triVerts.size() >= 2) {
            final ConvexHullJarvisMarch hull = new ConvexHullJarvisMarch();
            hull.compute(triVerts, output, Long.MAX_VALUE);
            // output.addAll(triVerts);

            Tuple2d v1, v2;

            for (int i = 0; i < output.size(); i++) {
               v1 = output.get(i);
               v2 = output.get((i + 1) % output.size());

               this.triangles.add(new Triangle2D(v1, v2, vertex));
            }
         }
      }
   }

   public Iterable<Triangle2D> getTriangles() {
      return Collections.unmodifiableList(this.triangles);
   }

   public void clear() {
      this.triangles.clear();
      this.vertices.clear();
   }
}
