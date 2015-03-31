package com.stephenwranger.compgeo.algorithms.trapezoids;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import com.stephenwranger.compgeo.algorithms.Algorithm;
import com.stephenwranger.graphics.collections.Pair;
import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.math.intersection.IntersectionUtils;
import com.stephenwranger.graphics.math.intersection.LineSegment;
import com.stephenwranger.graphics.math.intersection.Trapezoid;

public class TrapezoidalMapAlgorithm implements Algorithm<LineSegment, String[]> {
   private final Rectangle2D                                       bounds;
   private TrapezoidalMapNode                                      root;
   private String[][]                                              matrix    = null;
   private final LinkedHashMap<Tuple2d, Pair<String, Integer>>     xNodes    = new LinkedHashMap<Tuple2d, Pair<String, Integer>>();
   private final LinkedHashMap<LineSegment, Pair<String, Integer>> yNodes    = new LinkedHashMap<LineSegment, Pair<String, Integer>>();
   private final LinkedHashMap<Trapezoid, Pair<String, Integer>>   leafNodes = new LinkedHashMap<Trapezoid, Pair<String, Integer>>();
   private List<String[]>                                          output    = null;
   private int                                                     xNodeIndex = 0, yNodeIndex = 0, leafNodeIndex = 0;

   public TrapezoidalMapAlgorithm(final Rectangle2D bounds) {
      this.bounds = bounds;
   }

   @Override
   public boolean compute(final List<LineSegment> input, final List<String[]> output, final long timeout) {
      this.output = output;
      final long startTime = System.nanoTime();

      // initialize the root with the bounding box as a trapezoid
      this.root = new LeafNode(this.getTrapezoidalBounds(), new LeafNode[0]);
      List<LeafNode> intersected;
      LeafNode leaf;
      Trapezoid trapezoid;
      int ctr = 0;

      // loop over all input segments and insert them one at a time
      for (final LineSegment segment : input) {
         System.out.println("segment #" + ctr++);

         if ((System.nanoTime() - startTime) / 1000000l > timeout) {
            return false;
         }

         // traverse tree and adjacent trapezoids to get list of intersected leaf nodes
         intersected = this.getIntersectedNodes(segment);

         // if size is 1, the the segment is completely inside the node; split into 4 pieces
         if (intersected.size() == 1) {
            leaf = intersected.get(0);
            trapezoid = leaf.getTrapezoid();

            // segment is completely inside leaf node
            final LineSegment leftEdge = trapezoid.getLeft();
            final LineSegment rightEdge = trapezoid.getRight();
            final LineSegment topEdge = trapezoid.getTop();
            final LineSegment bottomEdge = trapezoid.getBottom();

            final LineSegment pMax = new LineSegment(segment.min, new Tuple2d(segment.min.x, segment.min.y + this.bounds.getHeight()));
            final LineSegment pMin = new LineSegment(segment.min, new Tuple2d(segment.min.x, segment.min.y - this.bounds.getHeight()));
            final LineSegment qMax = new LineSegment(segment.max, new Tuple2d(segment.max.x, segment.max.y + this.bounds.getHeight()));
            final LineSegment qMin = new LineSegment(segment.max, new Tuple2d(segment.max.x, segment.max.y - this.bounds.getHeight()));

            final Tuple2d topLeft = topEdge.intersect(pMax);
            final Tuple2d topRight = topEdge.intersect(qMax);
            final Tuple2d bottomLeft = bottomEdge.intersect(pMin);
            final Tuple2d bottomRight = bottomEdge.intersect(qMin);

            final Trapezoid u = new Trapezoid(leftEdge.min, leftEdge.max, topLeft, bottomLeft);
            final Trapezoid x = new Trapezoid(rightEdge.min, rightEdge.max, topRight, bottomRight);
            final Trapezoid y = new Trapezoid(segment.min, topLeft, topRight, segment.max);
            final Trapezoid z = new Trapezoid(segment.min, bottomLeft, bottomRight, segment.max);

            final LeafNode a = this.getLeafNode(u);
            final LeafNode d = this.getLeafNode(x);

            final LeafNode b = this.getLeafNode(y);
            b.addNeighbors(new LeafNode[] { a, d });

            final LeafNode c = this.getLeafNode(z);
            c.addNeighbors(new LeafNode[] { a, d });

            a.addNeighbors(leaf.getNeighborsLeft());
            a.addNeighbor(b);
            a.addNeighbor(c);

            d.addNeighbors(leaf.getNeighborsRight());
            d.addNeighbor(b);
            d.addNeighbor(c);

            final YNode s1 = this.getYNode(segment, b, c);
            final XNode p1 = this.getXNode(segment.min, "P", a, null);
            final XNode q1 = this.getXNode(segment.max, "Q", s1, d);

            p1.setRightBelow(q1);

            if (leaf == this.root) {
               this.root = p1;
            } else {
               for(final TrapezoidalMapNode p : leaf.getParentNodes()) {
                  p.replaceChild(leaf, p1);
               }
               leaf.removeAsNeighbor();
            }
         } else {
            // each endpoint is inside a different trapezoid and potentially bisects intermediaries
            final List<LeafNode> newNodes = new ArrayList<LeafNode>();
            LeafNode node;
            Trapezoid[] splitTrapezoids;

            // loop left-to-right and deal with each node
            for (int i = 0; i < intersected.size(); i++) {
               node = intersected.get(i);

               try {
                  splitTrapezoids = node.getTrapezoid().split(segment);
               } catch (final Exception e) {
                  System.out.println(node.getTrapezoid());
                  System.out.println(segment);
                  throw e;
               }

               if (i == 0) {
                  // split into three on left end
                  final LeafNode a = this.getLeafNode(splitTrapezoids[0]);
                  a.addNeighbors(node.getNeighborsLeft());

                  final LeafNode b = this.getLeafNode(splitTrapezoids[1]);
                  b.addNeighbors(node.getNeighborsRight());
                  b.addNeighbor(a);

                  final LeafNode c = this.getLeafNode(splitTrapezoids[2]);
                  c.addNeighbors(node.getNeighborsRight());
                  c.addNeighbor(a);

                  a.addNeighbor(b);
                  a.addNeighbor(c);

                  newNodes.add(a);
                  newNodes.add(b);
                  newNodes.add(c);

                  final YNode s = this.getYNode(segment, b, c);
                  final XNode p1 = this.getXNode(segment.min, "P", a, s);

                  for (final TrapezoidalMapNode p : node.getParentNodes()) {
                     p.replaceChild(node, p1);
                  }
               } else if (i == intersected.size() - 1 && splitTrapezoids.length > 2) {
                  // split into three on right end
                  final LeafNode c = this.getLeafNode(splitTrapezoids[2]);
                  c.addNeighbors(node.getNeighborsRight());

                  final LeafNode b = this.getLeafNode(splitTrapezoids[1]);
                  b.addNeighbors(node.getNeighborsLeft());
                  b.addNeighbor(c);

                  final LeafNode a = this.getLeafNode(splitTrapezoids[0]);
                  a.addNeighbors(node.getNeighborsLeft());
                  a.addNeighbor(c);

                  c.addNeighbor(b);
                  c.addNeighbor(a);

                  newNodes.add(a);
                  newNodes.add(b);
                  newNodes.add(c);

                  final YNode s = this.getYNode(segment, a, b);
                  final XNode q = this.getXNode(segment.max, "Q", s, c);

                  for (final TrapezoidalMapNode p : node.getParentNodes()) {
                     p.replaceChild(node, q);
                  }
               } else {
                  // bisect
                  final LeafNode top = this.getLeafNode(splitTrapezoids[0]);
                  final LeafNode bottom = this.getLeafNode(splitTrapezoids[1]);

                  top.addNeighbors(node.getNeighborsLeft());
                  top.addNeighbors(node.getNeighborsRight());

                  bottom.addNeighbors(node.getNeighborsLeft());
                  bottom.addNeighbors(node.getNeighborsRight());

                  newNodes.add(top);
                  newNodes.add(bottom);

                  final YNode s = this.getYNode(segment, top, bottom);

                  for (final TrapezoidalMapNode p : node.getParentNodes()) {
                     p.replaceChild(node, s);
                  }
               }

               node.removeAsNeighbor();
            }

            // check for any merges within set of new nodes
            final List<Pair<LeafNode, LeafNode>> toMerge = new ArrayList<>();
            LeafNode n, m;
            LineSegment left, right;

            do {
               // for any pair of nodes that contained an identical edge, merge them
               for (final Pair<LeafNode, LeafNode> pair : toMerge) {
                  left = pair.left.getTrapezoid().getLeft();
                  right = pair.right.getTrapezoid().getRight();
                  final Trapezoid merged = new Trapezoid(left.min, left.max, right.max, right.min);
                  n = this.getLeafNode(merged);
                  n.addNeighbors(pair.left.getNeighborsLeft());
                  n.addNeighbors(pair.right.getNeighborsRight());

                  newNodes.remove(pair.left);
                  newNodes.remove(pair.right);
                  newNodes.add(n);

                  pair.left.removeAsNeighbor();
                  pair.right.removeAsNeighbor();

                  if (pair.left == this.root || pair.right == this.root) {
                     this.root = n;
                  } else {
                     for (final TrapezoidalMapNode p : pair.left.getParentNodes()) {
                        p.replaceChild(pair.left, n);
                     }
                     for (final TrapezoidalMapNode p : pair.right.getParentNodes()) {
                        p.replaceChild(pair.right, n);
                     }
                  }
               }

               toMerge.clear();

               // loop through list of new leaf nodes and look for merge-able leaves
               for (int i = 0; i < newNodes.size() - 1; i++) {
                  n = newNodes.get(i);

                  for (int j = i + 1; j < newNodes.size(); j++) {
                     m = newNodes.get(j);
                     Pair<LeafNode, LeafNode> pair = null;

                     if (n.getTrapezoid().getRight().equals(m.getTrapezoid().getLeft())) {
                        pair = Pair.getInstance(n, m);
                     } else if (n.getTrapezoid().getLeft().equals(m.getTrapezoid().getRight())) {
                        pair = Pair.getInstance(m, n);
                     }

                     if (pair != null && !toMerge.contains(pair)) {
                        toMerge.add(pair);
                     }
                  }
               }
            } while (!toMerge.isEmpty());
         }

         // update output matrix (so even if it crashes we get something)
         this.updateMatrix();
      }

      // update output matrix
      this.updateMatrix();

      return true;
   }

   /**
    * Returns a new LeafNode for the given Trapezoid or, if one has already been created, the cached node.
    *
    * @param trapezoid
    * @return
    */
   private LeafNode getLeafNode(final Trapezoid trapezoid) {
      Pair<String, Integer> label = null;

      if (this.leafNodes.containsKey(trapezoid)) {
         label = this.leafNodes.get(trapezoid);
      } else {
         label = Pair.getInstance("T" + this.leafNodes.size(), this.leafNodeIndex);
         this.leafNodeIndex++;
         this.leafNodes.put(trapezoid, label);
      }

      final LeafNode node = new LeafNode(trapezoid, null);
      node.setLabel(label.left);
      node.setIndex(label.right);

      return node;
   }

   /**
    * Returns a new XNode for the given point and the given left and right nodes or, if one has already been created, the cached one.
    *
    * @param point
    * @param left
    * @param right
    * @return
    */
   private XNode getXNode(final Tuple2d point, final String labelPrefix, final TrapezoidalMapNode left, final TrapezoidalMapNode right) {
      Pair<String, Integer> label = null;

      if (this.xNodes.containsKey(point)) {
         label = this.xNodes.get(point);
      } else {
         label = Pair.getInstance(labelPrefix + this.xNodes.size(), this.xNodeIndex);
         this.xNodeIndex++;
         this.xNodes.put(point, label);
      }

      final XNode node = new XNode(point, left, right);
      node.setLabel(label.left);
      node.setIndex(label.right);

      return node;
   }

   /**
    * Returns a new YNode for the given segment and the given above and below nodes or, if one has already been created, the cached one.
    *
    * @param segment
    * @param above
    * @param below
    * @return
    */
   private YNode getYNode(final LineSegment segment, final TrapezoidalMapNode above, final TrapezoidalMapNode below) {
      Pair<String, Integer> label = null;

      if (this.yNodes.containsKey(segment)) {
         label = this.yNodes.get(segment);
      } else {
         label = Pair.getInstance("S" + this.yNodes.size(), this.yNodeIndex);
         this.yNodeIndex++;
         this.yNodes.put(segment, label);
      }

      final YNode node = new YNode(segment, above, below);
      node.setLabel(label.left);
      node.setIndex(label.right);

      return node;
   }

   private void updateNode(final TrapezoidalMapNode node) {
      if (node == null) {
         return;
      }

      int parentIndex = 0, childIndex = node.getIndex();

      if (node instanceof XNode) {
         childIndex += 0;
      } else if (node instanceof YNode) {
         childIndex += this.xNodes.size();
      } else {
         childIndex += this.xNodes.size() + this.yNodes.size();
      }

      this.matrix[childIndex + 1][0] = node.getLabel();
      this.matrix[0][childIndex + 1] = node.getLabel();

      for (final TrapezoidalMapNode parent : node.getParentNodes()) {
         parentIndex = parent.getIndex();

         if (parent instanceof XNode) {
            parentIndex += 0;
         } else if (parent instanceof YNode) {
            parentIndex += this.xNodes.size();
         } else {
            parentIndex += this.xNodes.size() + this.yNodes.size();
         }

         this.matrix[childIndex + 1][parentIndex + 1] = "1";
      }

      this.updateNode(node.getLeftAbove());
      this.updateNode(node.getRightBelow());
   }

   /**
    * Updates the trapezoidal map matrix.
    */
   private void updateMatrix() {
      this.output.clear();

      this.matrix = new String[this.xNodes.size() + this.yNodes.size() + this.leafNodes.size() + 2][this.xNodes.size() + this.yNodes.size() + this.leafNodes.size() + 2];
      String[] row;

      for (int i = 0; i < this.matrix.length; i++) {
         row = this.matrix[i];

         if (i == 0 || i == this.matrix.length - 1) {
            Arrays.fill(row, "");
         } else {
            Arrays.fill(row, 1, row.length - 1, "0");
            row[0] = "";
            row[row.length - 1] = "";
         }
      }

      this.updateNode(this.root);

      this.matrix[0][this.matrix.length - 1] = "Total";
      this.matrix[this.matrix.length - 1][0] = "Total";
      int totalRow = 0, totalCol = 0;

      for (int i = 1; i < this.matrix.length - 1; i++) {
         totalRow = 0;
         totalCol = 0;

         for (int j = 1; j < this.matrix.length - 1; j++) {
            totalRow += Integer.parseInt(this.matrix[i][j]);
            totalCol += Integer.parseInt(this.matrix[j][i]);
         }

         this.matrix[i][this.matrix.length - 1] = Integer.toString(totalRow);
         this.matrix[this.matrix.length - 1][i] = Integer.toString(totalCol);
      }

      final List<Integer> validRows = new ArrayList<Integer>();
      final List<Integer> validCols = new ArrayList<Integer>();

      for (int i = 1; i < this.matrix.length - 1; i++) {
         if (!this.matrix[0][i].equals("")) {
            validCols.add(i);
         }

         if (!this.matrix[i][0].equals("")) {
            validRows.add(i);
         }
      }

      int colIndex;

      for (int i = 0; i < this.matrix.length; i++) {
         if (i == 0 || i == this.matrix.length - 1 || validRows.contains(i)) {
            colIndex = 0;
            row = new String[validCols.size() + 2];

            for (int j = 0; j < this.matrix.length; j++) {
               if (j == 0 || j == this.matrix.length - 1 || validCols.contains(j)) {
                  row[colIndex] = this.matrix[i][j];
                  colIndex++;
               }
            }

            this.output.add(row);
         }
      }

      // this.output.addAll(Arrays.asList(this.matrix));
   }

   /**
    * Uses the Trapezoid Map Query to determine which nodes a given line segment has intersected.
    *
    * @param segment
    * @return
    */
   private List<LeafNode> getIntersectedNodes(final LineSegment segment) {
      final List<LeafNode> intersected = new ArrayList<LeafNode>();

      final LeafNode leftNode = this.root.queryMap(segment.min);
      final LeafNode rightNode = this.root.queryMap(segment.max);
      LeafNode temp = leftNode;

      if (leftNode != rightNode) {
         do {
            intersected.add(temp);
            temp = temp.getNeighbor(segment);
         } while (IntersectionUtils.isLessOrEqual(temp.getTrapezoid().getRight().x, rightNode.getTrapezoid().getLeft().x));

         intersected.add(temp);
      } else {
         intersected.add(leftNode);
      }

      return intersected;
   }

   /**
    * Returns the bounds of this algorithm as a Trapezoid.
    *
    * @return
    */
   private Trapezoid getTrapezoidalBounds() {
      final double x = this.bounds.getX();
      final double y = this.bounds.getY();
      final double w = this.bounds.getWidth();
      final double h = this.bounds.getHeight();

      final Tuple2d c0 = new Tuple2d(x, y);
      final Tuple2d c1 = new Tuple2d(x, y + h);
      final Tuple2d c2 = new Tuple2d(x + w, y + h);
      final Tuple2d c3 = new Tuple2d(x + w, y);

      return new Trapezoid(c0, c1, c2, c3);
   }
}
