import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.stephenwranger.graphics.math.Tuple2d;
import com.stephenwranger.graphics.utils.models.PlyModelLoader;
import com.stephenwranger.graphics.utils.models.VertexListLoader;


public class SampleVl2File {
   public static void main(final String[] args) {
      final File fileVl2 = new File(System.getProperty("user.home") + "/Desktop/sample.vl2");
      final File filePly = new File(System.getProperty("user.home") + "/Desktop/sample.ply");
      final Set<Tuple2d> sampleData = new HashSet<Tuple2d>();
      final int count = (args.length >= 1) ? Integer.parseInt(args[0]) : 100;
      final int x = (args.length >= 2) ? Integer.parseInt(args[1]) : 20;
      final int y = (args.length >= 3) ? Integer.parseInt(args[2]) : 20;
      final int width = (args.length >= 4) ? Integer.parseInt(args[3]) : 750;
      final int height = (args.length >= 5) ? Integer.parseInt(args[4]) : 450;
      final Random random = new Random();

      for (int i = 0; i < count; i++) {
         sampleData.add(new Tuple2d(x + random.nextDouble() * width, y + random.nextDouble() * height));
      }

      try {
         VertexListLoader.writeVertexList2d(fileVl2, sampleData);

         System.out.println("File " + fileVl2 + " written successfully!");
      } catch (final IOException e) {
         e.printStackTrace();
      }

      try {
         PlyModelLoader.writePlyModel2d(filePly, sampleData, null);

         System.out.println("File " + filePly + " written successfully!");
      } catch (final IOException e) {
         e.printStackTrace();
      }
   }
}
