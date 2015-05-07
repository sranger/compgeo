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
   private final List<Tuple2d> vertices = new ArrayList<Tuple2d>();
   private final Map<LineSegment, Set<Triangle2d>> edgesToTrianglesMap = new HashMap<LineSegment, Set<Triangle2d>>();
   private final Set<Triangle2d> triangles = new CopyOnWriteArraySet<Triangle2d>();
   private final Set<IterativeListener> listeners = new HashSet<IterativeListener>();

   private final Triangle2d boundingTriangle;

   private boolean isBusy = false;
   private boolean isWaiting = false;
   private int continueCtr = 0;

   public DelaunayTriangulation(final Triangle2d boundingTriangle) {
      this.boundingTriangle = boundingTriangle;

      final Tuple2d[] corners = this.boundingTriangle.getCorners();
      this.addVertexImpl(corners[0], false);
      this.addVertexImpl(corners[1], false);
      this.addVertexImpl(corners[2], false);
   }

   public void addVertex(final Tuple2d vertex, final boolean sendIterativeListenerNotifications) throws InvalidActivityException {
      if (this.isBusy) {
         throw new InvalidActivityException("Cannot add a new vertex while the triangulation is busy.");
      }
      final Tuple3d bary = this.boundingTriangle.getBarycentricCoordinate(vertex);

      if (IntersectionUtils.isLessOrEqual(bary.x, 0) || IntersectionUtils.isLessOrEqual(bary.y, 0) || IntersectionUtils.isGreaterThan(bary.x + bary.y + bary.z, 1)) {
         DelaunayTriangulation.this.notifyListeners("Vertex is on, or outside, the bounding triangle; skipping." + vertex + ", " + bary);
         return;
      }

      this.isBusy = true;
      new Thread() {
         @Override
         public void run() {
            synchronized (DelaunayTriangulation.this) {
               DelaunayTriangulation.this.addVertexImpl(vertex, sendIterativeListenerNotifications);
            }
         }
      }.start();
   }

   private void addVertexImpl(final Tuple2d vertex, final boolean sendIterativeListenerNotifications) {
      DelaunayTriangulation.this.vertices.add(vertex);

      if(sendIterativeListenerNotifications) {
         DelaunayTriangulation.this.notifyListeners("Adding New Vertex: " + vertex, vertex);
      }

      if (DelaunayTriangulation.this.vertices.size() == 3) {
         DelaunayTriangulation.this.addTriangle(new Triangle2d(DelaunayTriangulation.this.vertices.get(0), DelaunayTriangulation.this.vertices.get(1),
               DelaunayTriangulation.this.vertices.get(2)));
      } else if (DelaunayTriangulation.this.vertices.size() > 3) {
         final List<Triangle2d> needsReplacing = new ArrayList<Triangle2d>();
         Circle c = null;

         if(sendIterativeListenerNotifications) {
            DelaunayTriangulation.this.notifyListeners("Checking New Point against existing Triangle's Circumscribed Circles.");
         }
         for (final Triangle2d t : DelaunayTriangulation.this.triangles) {
            c = t.getCircumscribedCircle();

            if(sendIterativeListenerNotifications) {
               DelaunayTriangulation.this.notifyListeners("\tChecking circumcircle.", t, c);
            }

            if (c.contains(vertex)) {
               needsReplacing.add(t);
            }
         }

         if(sendIterativeListenerNotifications) {
            DelaunayTriangulation.this.notifyListeners("Found " + needsReplacing.size()
                  + " triangles that need updating; removing them from the triangulation.", needsReplacing.toArray());
         }

         final List<LineSegment> edges = new ArrayList<LineSegment>();
         final List<LineSegment> output = new ArrayList<LineSegment>();
         final Set<Triangle2d> newTriangles = new HashSet<Triangle2d>();

         for (final Triangle2d tri : needsReplacing) {
            edges.addAll(Arrays.asList(tri.getLineSegments()));
            DelaunayTriangulation.this.removeTriangle(tri);
         }

         if(sendIterativeListenerNotifications) {
            DelaunayTriangulation.this.notifyListeners("Removing any non-unique edges from removed triangles.");
         }

         if (!edges.isEmpty()) {
            DelaunayTriangulation.getUniqueEdges(edges, output);
            if(sendIterativeListenerNotifications) {
               edges.removeAll(output);
               DelaunayTriangulation.this.notifyListeners("Number of unique edges found: " + output.size() + ". Creating new Triangles from unique edge list and inserted vertex.", edges.toArray());
            }

            Triangle2d triangle;

            for (final LineSegment segment : output) {
               triangle = new Triangle2d(segment.min, segment.max, vertex);

               // final double left = (segment.max.y - segment.min.y) * (vertex.x - segment.max.x);
               // final double right = (vertex.y - segment.max.y) * (segment.max.x - segment.min.x);
               //
               // // triangle vertices are colinear and we're going to ignore that
               // if (IntersectionUtils.isEqual(left, right)) {
               // continue;
               // }

               newTriangles.add(triangle);
               DelaunayTriangulation.this.addTriangle(triangle);
               if(sendIterativeListenerNotifications) {
                  DelaunayTriangulation.this.notifyListeners("Triangle added: " + triangle, triangle);
               }
            }
         } else {
            if(sendIterativeListenerNotifications) {
               DelaunayTriangulation.this.notifyListeners("No unique edges; something probably went wrong.");
            }
         }

         if (!newTriangles.isEmpty()) {
            if(sendIterativeListenerNotifications) {
               DelaunayTriangulation.this.notifyListeners("Total Triangles added: " + newTriangles.size()
                     + "; verifying Delaunay properties.", newTriangles.toArray());
            }
            DelaunayTriangulation.this.verifyDelaunay(newTriangles);
         } else {
            if(sendIterativeListenerNotifications) {
               DelaunayTriangulation.this.notifyListeners("No new Triangles; something probably went wrong.");
            }
         }
      }

      if(sendIterativeListenerNotifications) {
         DelaunayTriangulation.this.notifyListeners("Addition of new Vertex complete.", vertex);
      }
      DelaunayTriangulation.this.isBusy = false;
   }

   public Iterable<Triangle2d> getTriangles() {
      return Collections.unmodifiableCollection(this.triangles);
   }

   public synchronized void clear() {
      this.triangles.clear();
      this.vertices.clear();
      this.edgesToTrianglesMap.clear();

      final Tuple2d[] corners = this.boundingTriangle.getCorners();
      this.addVertexImpl(corners[0], false);
      this.addVertexImpl(corners[1], false);
      this.addVertexImpl(corners[2], false);
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

   private void addTriangle(final Triangle2d triangle) {
      final Tuple2d[] corners = triangle.getCorners();
      final double left = (corners[1].y - corners[0].y) * (corners[2].x - corners[1].x);
      final double right = (corners[2].y - corners[1].y) * (corners[1].x - corners[0].x);

      // triangle vertices are colinear and we're going to ignore that
      if (IntersectionUtils.isEqual(left, right)) {
         return;
      }

      this.triangles.add(triangle);

      Set<Triangle2d> set;

      for (final LineSegment segment : triangle.getLineSegments()) {
         set = this.edgesToTrianglesMap.get(segment);

         if (set == null) {
            set = new HashSet<Triangle2d>();
            this.edgesToTrianglesMap.put(segment, set);
         }

         set.add(triangle);
      }
   }

   private void verifyDelaunay(final Set<Triangle2d> toCheck) {
      final Set<Triangle2d> toCheckNext = new HashSet<Triangle2d>();

      Set<Triangle2d> neighbors;
      Pair<Triangle2d, Triangle2d> newTriangles;
      boolean hasFlipped = false;

      for (final Triangle2d triangle : toCheck) {
         neighbors = this.getNeighbors(triangle);
         this.notifyListeners("Checking neighbors of " + triangle
               + " for invalid triangulation.\nThis occurs if the sum of the two internal angles opposite the shared"
               + " edge exceed 180 degrees. With our recursive iterative algorithm, it should rarely occur.", neighbors.toArray(), triangle);

         for (final Triangle2d neighbor : neighbors) {
            if (hasFlipped) {
               this.notifyListeners("Adding existing triangles to next verify loop: " + neighbor, neighbor);
               toCheckNext.add(neighbor);
            } else if ((newTriangles = this.fixDelaunay(triangle, neighbor)) != null) {
               this.notifyListeners("Removing invalid delaunay triangulation and flipping shared edge.", triangle, neighbor);
               this.removeTriangle(triangle);
               this.removeTriangle(neighbor);
               this.notifyListeners("Removed triangles: " + triangle + ", " + neighbor, triangle, neighbor);

               this.addTriangle(newTriangles.left);
               this.addTriangle(newTriangles.right);
               this.notifyListeners("Flipped additions: " + newTriangles.left + ", " + newTriangles.right, newTriangles.left, newTriangles.right);

               toCheckNext.add(newTriangles.left);
               toCheckNext.add(newTriangles.right);

               hasFlipped = true;
            }
         }
      }

      if (!toCheckNext.isEmpty()) {
         this.notifyListeners("Verifying delaunay with remainder of new triangles list along with all flipped triangles.", toCheckNext.toArray());
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
   private Pair<Triangle2d, Triangle2d> fixDelaunay(final Triangle2d t1, final Triangle2d t2) {
      final LineSegment edge = t1.getCommonEdge(t2);
      final Pair<LineSegment, LineSegment> corner1 = t1.getOppositeEdges(edge);
      final Pair<LineSegment, LineSegment> corner2 = t2.getOppositeEdges(edge);

      final double angle = Math.toDegrees(Math.abs(corner1.left.getAngle(corner1.right))
            + Math.abs(corner2.left.getAngle(corner2.right)));

      if (angle > 180.0) {
         final Tuple2d commonVertex1 = corner1.left.getCommonVertex(corner1.right);
         final Tuple2d commonVertex2 = corner2.left.getCommonVertex(corner2.right);
         final Triangle2d newT1 = new Triangle2d(edge.min, commonVertex1, commonVertex2);
         final Triangle2d newT2 = new Triangle2d(edge.max, commonVertex1, commonVertex2);

         return Pair.getInstance(newT1, newT2);
      }

      return null;
   }

   private Set<Triangle2d> getNeighbors(final Triangle2d triangle) {
      final Set<Triangle2d> neighbors = new HashSet<Triangle2d>();

      for (final LineSegment segment : triangle.getLineSegments()) {
         neighbors.addAll(this.edgesToTrianglesMap.get(segment));
      }

      // TODO: do this better
      neighbors.remove(triangle);

      return neighbors;
   }

   private void waitForContinue() {
      while(this.isWaiting) {
         try {
            Thread.sleep(100);
         } catch (final InterruptedException e) {
            e.printStackTrace();
         }
      }
   }

   @Override
   public void continueIteration() {
      this.continueCtr--;

      if(this.continueCtr == 0) {
         this.isWaiting = false;
      }
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
      if(!this.listeners.isEmpty()) {
         this.isWaiting = true;
         this.continueCtr = this.listeners.size();

         for (final IterativeListener listener : this.listeners) {
            listener.step(this, message, Arrays.asList(payload));
         }

         this.waitForContinue();
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

   @Override
   public boolean isBusy() {
      return this.isBusy;
   }

   public Collection<Triangle2d> getImmutableTriangles() {
      return Collections.unmodifiableSet(this.triangles);
   }

   public Collection<Tuple2d> getImmutableVertices() {
      return Collections.unmodifiableList(this.vertices);
   }
}
