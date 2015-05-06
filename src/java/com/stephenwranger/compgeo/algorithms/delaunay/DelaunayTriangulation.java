package com.stephenwranger.compgeo.algorithms.delaunay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Triangle2D;
import com.stephenwranger.graphics.renderables.Circle;
import com.stephenwranger.graphics.utils.Iterative;
import com.stephenwranger.graphics.utils.IterativeListener;

public class DelaunayTriangulation implements Iterative {
   private final List<Tuple2d>                     vertices            = new ArrayList<Tuple2d>();
   private final Map<LineSegment, Set<Triangle2D>> edgesToTrianglesMap = new HashMap<LineSegment, Set<Triangle2D>>();
   private final Set<Triangle2D>                   triangles           = new HashSet<Triangle2D>();
   private final Set<IterativeListener>            listeners           = new HashSet<IterativeListener>();

   private final Triangle2D                        boundingTriangle;

   public DelaunayTriangulation(final Triangle2D boundingTriangle) {
      this.boundingTriangle = boundingTriangle;

      final Tuple2d[] corners = this.boundingTriangle.getCorners();
      this.addVertex(corners[0]);
      this.addVertex(corners[1]);
      this.addVertex(corners[2]);
   }

   public synchronized void addVertex(final Tuple2d vertex) {
      this.vertices.add(vertex);

      this.notifyListeners("Adding New Vertex: " + vertex);

      if (this.vertices.size() == 3) {
         this.triangles.add(new Triangle2D(this.vertices.get(0), this.vertices.get(1), this.vertices.get(2)));
      } else if (this.vertices.size() > 3) {
         final List<Triangle2D> needsReplacing = new ArrayList<Triangle2D>();
         Circle c = null;

         this.notifyListeners("Checking New Point against existing Triangle's Circumscribed Circles.");
         for (final Triangle2D t : this.triangles) {
            c = t.getCircumscribedCircle();

            if (c.contains(vertex)) {
               needsReplacing.add(t);
            }
         }

         this.notifyListeners("Found " + needsReplacing.size() + " triangles that need updating; removing them from the triangulation.");

         final List<LineSegment> edges = new ArrayList<LineSegment>();
         final List<LineSegment> output = new ArrayList<LineSegment>();
         final Set<Triangle2D> newTriangles = new HashSet<Triangle2D>();

         for (final Triangle2D tri : needsReplacing) {
            edges.addAll(Arrays.asList(tri.getLineSegments()));
            this.removeTriangle(tri);
         }

         this.notifyListeners("Removing any non-unique edges from removed triangles.");

         if (!edges.isEmpty()) {
            DelaunayTriangulation.getUniqueEdges(edges, output);
            this.notifyListeners("Number of unique edges found: " + output.size());
            this.notifyListeners("Creating new Triangles from unique edge list and inserted vertex.");

            Triangle2D triangle;

            for (final LineSegment segment : output) {
               triangle = new Triangle2D(segment.min, segment.max, vertex);
               newTriangles.add(triangle);
               this.addTriangle(triangle);
               this.notifyListeners("Triangle added: " + triangle);
            }
         } else {
            this.notifyListeners("No unique edges; something probably went wrong.");
         }

         if (!newTriangles.isEmpty()) {
            this.notifyListeners("Total Triangles added: " + newTriangles.size() + "; verifying Delaunay properties.");
            this.verifyDelaunay(newTriangles);
         } else {
            this.notifyListeners("No new Triangles; something probably went wrong.");
         }
      }

      this.notifyListeners("Addition of new Vertex complete.");
   }

   public Iterable<Triangle2D> getTriangles() {
      return Collections.unmodifiableCollection(this.triangles);
   }

   public void clear() {
      this.triangles.clear();
      this.vertices.clear();
      this.edgesToTrianglesMap.clear();

      final Tuple2d[] corners = this.boundingTriangle.getCorners();
      this.addVertex(corners[0]);
      this.addVertex(corners[1]);
      this.addVertex(corners[2]);
   }

   private void removeTriangle(final Triangle2D triangle) {
      this.triangles.remove(triangle);

      Set<Triangle2D> set;

      for (final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);

         if (set != null) {
            set.remove(triangle);
         }
      }
   }

   private void addTriangle(final Triangle2D triangle) {
      this.triangles.add(triangle);

      Set<Triangle2D> set;

      for (final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);

         if (set == null) {
            set = new HashSet<Triangle2D>();
            this.edgesToTrianglesMap.put(segment, set);
         }

         set.add(triangle);
      }
   }

   private void verifyDelaunay(final Set<Triangle2D> toCheck) {
      final Set<Triangle2D> toCheckNext = new HashSet<Triangle2D>();

      Set<Triangle2D> neighbors;
      Pair<Triangle2D, Triangle2D> newTriangles;
      boolean hasFlipped = false;

      for (final Triangle2D triangle : toCheck) {
         neighbors = this.getNeighbors(triangle);
         this.notifyListeners("Checking neighbors of " + triangle
               + " for invalid triangulation.\nThis occurs if the sum of the two internal angles opposite the shared"
               + " edge exceed 180 degrees. With our recursive iterative algorithm, it should rarely occur.");

         for (final Triangle2D neighbor : neighbors) {
            if (hasFlipped) {
               this.notifyListeners("Adding existing triangles to next verify loop: " + neighbor);
               toCheckNext.add(neighbor);
            } else if ((newTriangles = this.fixDelaunay(triangle, neighbor)) != null) {
               this.notifyListeners("Removing invalid delaunay triangulation and flipping shared edge.");
               this.removeTriangle(triangle);
               this.removeTriangle(neighbor);
               this.notifyListeners("Removed triangles: " + triangle + ", " + neighbor);

               this.addTriangle(newTriangles.left);
               this.addTriangle(newTriangles.right);
               this.notifyListeners("Flipped additions: " + newTriangles.left + ", " + newTriangles.right);

               toCheckNext.add(newTriangles.left);
               toCheckNext.add(newTriangles.right);

               hasFlipped = true;
            }
         }
      }

      if (!toCheckNext.isEmpty()) {
         this.notifyListeners("Verifying delaunay with remainder of new triangles list along with all flipped triangles.");
         this.verifyDelaunay(toCheckNext);
      } else {
         this.notifyListeners("Verification complete.");
      }
   }

   /**
    * Checks if the two given triangles form a valid delaunay triangulation and if not, flips their common edge.
    *
    * @param t1
    * @param t2
    * @return the new triangles or null if no changes were made
    */
   private Pair<Triangle2D, Triangle2D> fixDelaunay(final Triangle2D t1, final Triangle2D t2) {
      final LineSegment edge = t1.getCommonEdge(t2);
      final Pair<LineSegment, LineSegment> corner1 = t1.getOppositeEdges(edge);
      final Pair<LineSegment, LineSegment> corner2 = t2.getOppositeEdges(edge);

      final double angle = Math.toDegrees(Math.abs(corner1.left.getAngle(corner1.right)) + Math.abs(corner2.left.getAngle(corner2.right)));

      if (angle > 180.0) {
         final Tuple2d commonVertex1 = corner1.left.getCommonVertex(corner1.right);
         final Tuple2d commonVertex2 = corner2.left.getCommonVertex(corner2.right);
         final Triangle2D newT1 = new Triangle2D(edge.min, commonVertex1, commonVertex2);
         final Triangle2D newT2 = new Triangle2D(edge.max, commonVertex1, commonVertex2);

         return Pair.getInstance(newT1, newT2);
      }

      return null;
   }

   private Set<Triangle2D> getNeighbors(final Triangle2D triangle) {
      final Set<Triangle2D> neighbors = new HashSet<Triangle2D>();

      for (final LineSegment segment : triangle.getLineSegments()) {
         neighbors.addAll(this.edgesToTrianglesMap.get(segment));
      }

      // TODO: do this better
      neighbors.remove(triangle);

      return neighbors;
   }

   private static void getUniqueEdges(final List<LineSegment> input, final List<LineSegment> output) {
      output.clear();

      for (final LineSegment segment : input) {
         if (!output.remove(segment)) {
            output.add(segment);
         }
      }
   }

   @Override
   public void continueIteration() {
      // TODO Auto-generated method stub

   }

   @Override
   public void addIterativeListener(IterativeListener listener) {
      this.listeners.add(listener);
   }

   @Override
   public void removeIterativeListener(IterativeListener listener) {
      this.listeners.remove(listener);
   }

   private void notifyListeners(final String message, final Object... payload) {
      for (final IterativeListener listener : this.listeners) {
         listener.step(this, message, Arrays.asList(payload));
      }
   }
}
