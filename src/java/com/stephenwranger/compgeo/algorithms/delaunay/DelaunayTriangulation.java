package com.stephenwranger.compgeo.algorithms.delaunay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.activity.InvalidActivityException;

import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.Tuple3d;
import com.stephenwranger.graphics.math.intersection.IntersectionUtils;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Triangle2d;
import com.stephenwranger.graphics.renderables.Circle;
import com.stephenwranger.graphics.utils.Iterative;
import com.stephenwranger.graphics.utils.IterativeListener;

public class DelaunayTriangulation implements Iterative {
   private final List<Tuple2d>                     vertices            = new ArrayList<>();
   private final Map<LineSegment, Set<Triangle2d>> edgesToTrianglesMap = new HashMap<>();
   private final Set<Triangle2d>                   triangles           = new CopyOnWriteArraySet<>();
   private final Set<IterativeListener>            listeners           = new HashSet<>();

   private final Triangle2d                        boundingTriangle;

   private boolean                                 isBusy              = false;
   private boolean                                 isWaiting           = false;
   private int                                     continueCtr         = 0;

   public DelaunayTriangulation(final Triangle2d boundingTriangle) {
      this.boundingTriangle = boundingTriangle;

      final Tuple2d[] corners = this.boundingTriangle.getCorners(false);
      this.addVertexImpl(corners[0], false);
      this.addVertexImpl(corners[1], false);
      this.addVertexImpl(corners[2], false);
   }

   //   /**
   //    * TODO: this does NOT triangulate correctly atm
   //    * @param triangle
   //    * @param sendIterativeListenerNotification
   //    * @throws InvalidActivityException
   //    */
   //   public void addTriangle(final Triangle2d triangle, final boolean sendIterativeListenerNotification) throws InvalidActivityException {
   //      if (this.isBusy) {
   //         throw new InvalidActivityException("Cannot add a new vertex while the triangulation is busy.");
   //      }
   //      
   //      this.vertices.addAll(Arrays.asList(triangle.getCorners(false)));
   //      this.triangles.add(triangle);
   //      DelaunayTriangulation.this.notifyListeners("Added static triangle to output", sendIterativeListenerNotification, triangle);
   //   }

   @Override
   public void addIterativeListener(final IterativeListener listener) {
      this.listeners.add(listener);
   }

   public void addVertex(final Tuple2d vertex, final boolean sendIterativeListenerNotifications) throws InvalidActivityException {
      if (this.isBusy) {
         throw new InvalidActivityException("Cannot add a new vertex while the triangulation is busy.");
      }
      final Tuple3d bary = this.boundingTriangle.getBarycentricCoordinate(vertex);

      if (IntersectionUtils.isLessOrEqual(bary.x, 0) || IntersectionUtils.isLessOrEqual(bary.y, 0) || IntersectionUtils.isGreaterThan(bary.x + bary.y + bary.z, 1)) {
         DelaunayTriangulation.this.notifyListeners("Vertex is on, or outside, the bounding triangle; skipping." + vertex + ", " + bary, sendIterativeListenerNotifications);
         return;
      }

      if (sendIterativeListenerNotifications) {
         this.isBusy = true;
         new Thread() {
            @Override
            public void run() {
               synchronized (DelaunayTriangulation.this) {
                  DelaunayTriangulation.this.addVertexImpl(vertex, sendIterativeListenerNotifications);
               }
            }
         }.start();
      } else {
         DelaunayTriangulation.this.addVertexImpl(vertex, sendIterativeListenerNotifications);
      }
   }

   public synchronized void clear() {
      this.triangles.clear();
      this.vertices.clear();
      this.edgesToTrianglesMap.clear();

      final Tuple2d[] corners = this.boundingTriangle.getCorners(false);
      this.addVertexImpl(corners[0], false);
      this.addVertexImpl(corners[1], false);
      this.addVertexImpl(corners[2], false);
   }

   @Override
   public void continueIteration() {
      this.continueCtr--;

      if (this.continueCtr == 0) {
         this.isWaiting = false;
      }
   }

   public Collection<Triangle2d> getImmutableTriangles() {
      return Collections.unmodifiableSet(this.triangles);
   }

   public Collection<Tuple2d> getImmutableVertices() {
      return Collections.unmodifiableList(this.vertices);
   }

   public Iterable<Triangle2d> getTriangles() {
      return Collections.unmodifiableCollection(this.triangles);
   }

   @Override
   public boolean isBusy() {
      return this.isBusy;
   }

   @Override
   public void removeIterativeListener(final IterativeListener listener) {
      this.listeners.remove(listener);
   }

   private void addTriangle(final Triangle2d triangle) {
      final Tuple2d[] corners = triangle.getCorners(false);
      final double left = (corners[1].y - corners[0].y) * (corners[2].x - corners[1].x);
      final double right = (corners[2].y - corners[1].y) * (corners[1].x - corners[0].x);

      // triangle vertices are colinear and we're going to ignore that
      if (IntersectionUtils.isEqual(left, right)) {
         return;
      }

      try {
         triangle.getCircumscribedCircle();
      } catch (final Exception e) {
         // invalid triangle; probably colinear and didn't get caught above somehow?
         return;
      }

      this.triangles.add(triangle);

      Set<Triangle2d> set;

      for (final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);

         if (set == null) {
            set = new HashSet<>();
            this.edgesToTrianglesMap.put(segment, set);
         }

         set.add(triangle);
      }
   }

   private void addVertexImpl(final Tuple2d vertex, final boolean sendIterativeListenerNotifications) {
      DelaunayTriangulation.this.vertices.add(vertex);

      DelaunayTriangulation.this.notifyListeners("Adding New Vertex: " + vertex, sendIterativeListenerNotifications, vertex);

      if (DelaunayTriangulation.this.vertices.size() == 3) {
         DelaunayTriangulation.this.addTriangle(new Triangle2d(DelaunayTriangulation.this.vertices.get(0), DelaunayTriangulation.this.vertices.get(1), DelaunayTriangulation.this.vertices.get(2), false));
      } else if (DelaunayTriangulation.this.vertices.size() > 3) {
         final List<Triangle2d> needsReplacing = new ArrayList<>();
         Circle c = null;

         DelaunayTriangulation.this.notifyListeners("Checking New Point against existing Triangles' Circumscribed Circles.", sendIterativeListenerNotifications);
         for (final Triangle2d t : DelaunayTriangulation.this.triangles) {
            c = t.getCircumscribedCircle();

            if (c.contains(vertex)) {
               DelaunayTriangulation.this.notifyListeners("\tFound intersecting circumcircle.", sendIterativeListenerNotifications, t, c);
               needsReplacing.add(t);
            }
         }

         DelaunayTriangulation.this.notifyListeners("Found " + needsReplacing.size() + " triangles that need updating; removing them from the triangulation.", sendIterativeListenerNotifications, needsReplacing.toArray());

         final List<LineSegment> edges = new ArrayList<>();
         final List<LineSegment> output = new ArrayList<>();
         final Set<Triangle2d> newTriangles = new HashSet<>();

         for (final Triangle2d tri : needsReplacing) {
            edges.addAll(Arrays.asList(tri.getLineSegments()));
            DelaunayTriangulation.this.removeTriangle(tri);
         }

         DelaunayTriangulation.this.notifyListeners("Removing any non-unique edges from removed triangles.", sendIterativeListenerNotifications);

         if (!edges.isEmpty()) {
            DelaunayTriangulation.getUniqueEdges(edges, output);
            edges.removeAll(output);
            DelaunayTriangulation.this.notifyListeners("Number of unique edges found: " + output.size() + ". Creating new Triangles from unique edge list and inserted vertex.", sendIterativeListenerNotifications, edges.toArray());

            Triangle2d triangle;

            for (final LineSegment segment : output) {
               triangle = new Triangle2d(segment.min, segment.max, vertex, false);
               if(!this.vertices.contains(segment.min)) System.out.println("1. vertex not in input: " + segment.min);
               if(!this.vertices.contains(segment.max)) System.out.println("1. vertex not in input: " + segment.max);

               // final double left = (segment.max.y - segment.min.y) * (vertex.x - segment.max.x);
               // final double right = (vertex.y - segment.max.y) * (segment.max.x - segment.min.x);
               //
               // // triangle vertices are colinear and we're going to ignore that
               // if (IntersectionUtils.isEqual(left, right)) {
               // continue;
               // }

               try {
                  triangle.getCircumscribedCircle();
               } catch (final Exception e) {
                  // invalid triangle; probably colinear
                  continue;
               }

               newTriangles.add(triangle);
               DelaunayTriangulation.this.addTriangle(triangle);
               DelaunayTriangulation.this.notifyListeners("Triangle added: " + triangle, sendIterativeListenerNotifications, triangle);
            }
         } else {
            DelaunayTriangulation.this.notifyListeners("No unique edges; something probably went wrong.", sendIterativeListenerNotifications);
         }

         if (!newTriangles.isEmpty()) {
            DelaunayTriangulation.this.notifyListeners("Total Triangles added: " + newTriangles.size() + "; verifying Delaunay properties.", sendIterativeListenerNotifications, newTriangles.toArray());
            DelaunayTriangulation.this.verifyDelaunay(newTriangles, sendIterativeListenerNotifications);
         } else {
            DelaunayTriangulation.this.notifyListeners("No new Triangles; something probably went wrong.", sendIterativeListenerNotifications);
         }
      }

      DelaunayTriangulation.this.notifyListeners("Addition of new Vertex complete.", sendIterativeListenerNotifications, vertex);
      DelaunayTriangulation.this.isBusy = false;
   }

   /**
    * Checks if the two given triangles form a valid delaunay triangulation and if not, flips their common edge.
    *
    * @param t1
    * @param t2
    * @return the new triangles or null if no changes were made
    */
   private Pair<Triangle2d, Triangle2d> fixDelaunay(final Triangle2d t1, final Triangle2d t2) {
      final LineSegment edge = t1.getCommonEdge(t2);
      final Pair<LineSegment, LineSegment> corner1 = t1.getOppositeEdges(edge);
      final Pair<LineSegment, LineSegment> corner2 = t2.getOppositeEdges(edge);

      final double angle = Math.toDegrees(Math.abs(corner1.left.getAngle(corner1.right)) + Math.abs(corner2.left.getAngle(corner2.right)));

      if (angle > 180.0) {
         final Tuple2d commonVertex1 = corner1.left.getCommonVertex(corner1.right);
         final Tuple2d commonVertex2 = corner2.left.getCommonVertex(corner2.right);
         final Triangle2d newT1 = new Triangle2d(edge.min, commonVertex1, commonVertex2, false);
         final Triangle2d newT2 = new Triangle2d(edge.max, commonVertex1, commonVertex2, false);
         if(!this.vertices.contains(edge.min)) System.out.println("3. vertex not in input: " + edge.min);
         if(!this.vertices.contains(edge.max)) System.out.println("4. vertex not in input: " + edge.max);
         if(!this.vertices.contains(commonVertex1)) System.out.println("5. vertex not in input: " + commonVertex1);
         if(!this.vertices.contains(commonVertex2)) System.out.println("6. vertex not in input: " + commonVertex2);

         return Pair.getInstance(newT1, newT2);
      }

      return null;
   }

   private Set<Triangle2d> getNeighbors(final Triangle2d triangle) {
      final Set<Triangle2d> neighbors = new HashSet<>();

      for (final LineSegment segment : triangle.getLineSegments()) {
         if (this.edgesToTrianglesMap.containsKey(segment)) {
            neighbors.addAll(this.edgesToTrianglesMap.get(segment));
         }
      }

      // TODO: do this better
      neighbors.remove(triangle);

      return neighbors;
   }

   private void notifyListeners(final String message, final boolean doNotify, final Object... payload) {
      if (!this.listeners.isEmpty() && doNotify) {
         this.isWaiting = true;
         this.continueCtr = this.listeners.size();

         for (final IterativeListener listener : this.listeners) {
            listener.step(this, message, Arrays.asList(payload));
         }

         this.waitForContinue();
      }
   }

   private void removeTriangle(final Triangle2d triangle) {
      this.triangles.remove(triangle);

      Set<Triangle2d> set;

      for (final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);

         if (set != null) {
            set.remove(triangle);
         }
      }
   }

   private void verifyDelaunay(final Set<Triangle2d> toCheck, final boolean notifyListeners) {
      final Set<Triangle2d> toCheckNext = new HashSet<>();

      Set<Triangle2d> neighbors;
      Pair<Triangle2d, Triangle2d> newTriangles;
      boolean hasFlipped = false;

      this.notifyListeners("Checking neighbors for invalid triangulation.\nThis occurs if the sum of the two internal angles opposite the shared" + " edge exceed 180 degrees. With our recursive iterative algorithm, it should rarely occur.",
            notifyListeners, toCheck.toArray());

      for (final Triangle2d triangle : toCheck) {
         neighbors = this.getNeighbors(triangle);

         for (final Triangle2d neighbor : neighbors) {
            if (hasFlipped) {
               this.notifyListeners("Adding existing triangles to next verify loop: " + neighbor, notifyListeners, neighbor);
               toCheckNext.add(neighbor);
            } else if ((newTriangles = this.fixDelaunay(triangle, neighbor)) != null) {
               this.notifyListeners("Removing invalid delaunay triangulation and flipping shared edge.", notifyListeners, triangle, neighbor);
               this.removeTriangle(triangle);
               this.removeTriangle(neighbor);
               this.notifyListeners("Removed triangles: " + triangle + ", " + neighbor, notifyListeners, triangle, neighbor);

               this.addTriangle(newTriangles.left);
               this.addTriangle(newTriangles.right);
               this.notifyListeners("Flipped additions: " + newTriangles.left + ", " + newTriangles.right, notifyListeners, newTriangles.left, newTriangles.right);

               toCheckNext.add(newTriangles.left);
               toCheckNext.add(newTriangles.right);

               hasFlipped = true;
            }
         }
      }

      if (!toCheckNext.isEmpty()) {
         this.notifyListeners("Verifying delaunay with remainder of new triangles list along with all flipped triangles.", notifyListeners, toCheckNext.toArray());
         this.verifyDelaunay(toCheckNext, notifyListeners);
      } else {
         this.notifyListeners("Verification complete.", notifyListeners);
      }
   }

   private void waitForContinue() {
      while (this.isWaiting) {
         try {
            Thread.sleep(100);
         } catch (final InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   private static void getUniqueEdges(final List<LineSegment> input, final List<LineSegment> output) {
      output.clear();

      for (final LineSegment segment : input) {
         if (!output.remove(segment)) {
            output.add(segment);
         }
      }
   }
}
