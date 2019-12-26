/* Copyright 2018 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/


/*
  <model> is the path to the SavedModel directory of the model to use.
          For example, the saved_model directory in tarballs from
          https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
  <label_map> is the path to a file containing information about the labels detected by the model.
          For example, one of the .pbtxt files from
          https://github.com/tensorflow/models/tree/master/research/object_detection/data
  <image> is the path to an image file.
          Sample images can be found from the COCO, Kitti, or Open Images dataset.
  See: https://github.com/tensorflow/models/blob/master/research/object_detection/g3doc/detection_model_zoo.md
*/

//VERSION 2
import java.awt.Graphics2D;
import com.google.protobuf.TextFormat;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static object_detection.protos.StringIntLabelMapOuterClass.StringIntLabelMap;
import static object_detection.protos.StringIntLabelMapOuterClass.StringIntLabelMapItem;

/**
 * Java inference for the Object Detection API at:
 * https://github.com/tensorflow/models/blob/master/research/object_detection/
 */

//VERSION 2: READS SINGLE IMAGE AND DETECTS OBJECT AND CENTROID

public class DetectObjectsfromImage {
  public static void main(String[] args) throws Exception {

     String modelpath ="models/ssd_mobilenet_v1_coco_2017_11_17/saved_model";
     String labelpath= "labels/mscoco_label_map.pbtxt";
     String imagepath= "saved.jpg";
     System.out.println("Loading labels and model...");

    final String[] labels = loadLabels(labelpath);
    try (SavedModelBundle model = SavedModelBundle.load(modelpath, "serve")) {

    System.out.println("Model and labels loaded...");

    final String filename = imagepath;
    List<Tensor<?>> outputs = null;
    try (Tensor<UInt8> input = makeImageTensor(filename)) {
            System.out.println("Getting output...");
            outputs =
              model
                  .session()
                  .runner()
                  .feed("image_tensor", input)
                  .fetch("detection_scores")
                  .fetch("detection_classes")
                  .fetch("detection_boxes")
                  .run();
    }
    System.out.println("output obtained...");
    try (Tensor<Float> scoresT = outputs.get(0).expect(Float.class);
          Tensor<Float> classesT = outputs.get(1).expect(Float.class);
          Tensor<Float> boxesT = outputs.get(2).expect(Float.class)) {
          // All these tensors have:
          // - 1 as the first dimension
          // - maxObjects as the second dimension
          // While boxesT will have 4 as the third dimension (2 sets of (x, y) coordinates).
          // This can be verified by looking at scoresT.shape() etc.
          int maxObjects = (int) scoresT.shape()[1];
          float[] scores = scoresT.copyTo(new float[1][maxObjects])[0];
          float[] classes = classesT.copyTo(new float[1][maxObjects])[0];
          float[][] boxes = boxesT.copyTo(new float[1][maxObjects][4])[0];
          int objectsAboveThresh = 0;
          // Print all objects whose score is at least 0.5.
          System.out.printf("* %s\n", filename);
          boolean foundSomething = false;
          for (int i = 0; i < scores.length; ++i) {
            if (scores[i] < 0.5) {
              continue;
            }
            objectsAboveThresh++;
            foundSomething = true;
            System.out.printf("\tFound %-20s (score: %.4f)\t", labels[(int) classes[i]], scores[i]);
            //System.out.println(java.util.Arrays.toString(boxes[i]));
            findCentroidOfOne(boxes[i]);

          }
          //findCentroidOfAll(boxes,objectsAboveThresh);
          if (!foundSomething) {
            System.out.println("No objects detected with a high enough score.");
          }
        }

    }
  }



    private static void findCentroidOfOne ( float [] boxes){
        int length = boxes.length;
        float [] centroids = new float[2];                      //NORMALISED

        for (int i = 0; i<length; i++){
            centroids[0] = (boxes[0] + boxes[2])/2;            //Xc=b0+b2 /2
            centroids[1] = (boxes[1] + boxes[3])/2;            //Yc=b1+b3 /2
        }

        System.out.println(java.util.Arrays.toString(centroids));

  }


  private static void findCentroidOfAll ( float [][] boxes, int num){

     float[][] centroids = new float[num][2];                      //NORMALISED

      for (int i = 0; i<num; i++){
          centroids[i][0] = (boxes[i][0] + boxes[i][2])/2;            //Xc=b0+b2 /2
          centroids[i][1] = (boxes[i][1] + boxes[i][3])/2;            //Yc=b1+b3 /2
          System.out.println(java.util.Arrays.toString(centroids[i]));

      }
  }


  private static String[] loadLabels(String filename) throws Exception {
    String text = new String(Files.readAllBytes(Paths.get(filename)), StandardCharsets.UTF_8);
    StringIntLabelMap.Builder builder = StringIntLabelMap.newBuilder();
    TextFormat.merge(text, builder);
    StringIntLabelMap proto = builder.build();
    int maxId = 0;
    for (StringIntLabelMapItem item : proto.getItemList()) {
      if (item.getId() > maxId) {
        maxId = item.getId();
      }
    }
    String[] ret = new String[maxId + 1];
    for (StringIntLabelMapItem item : proto.getItemList()) {
      ret[item.getId()] = item.getDisplayName();
    }
    return ret;
  }

  private static void bgr2rgb(byte[] data) {
    for (int i = 0; i < data.length; i += 3) {
      byte tmp = data[i];
      data[i] = data[i + 2];
      data[i + 2] = tmp;
    }
  }

  private static Tensor<UInt8> makeImageTensor(String filename) throws IOException {
    BufferedImage img = ImageIO.read(new File(filename));
    if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
      throw new IOException(
          String.format(
              "Expected 3-byte BGR encoding in BufferedImage, found %d (file: %s). This code could be made more robust",
              img.getType(), filename));
    }
    byte[] data = ((DataBufferByte) img.getData().getDataBuffer()).getData();
    // ImageIO.read seems to produce BGR-encoded images, but the model expects RGB.
    bgr2rgb(data);
    final long BATCH_SIZE = 1;
    final long CHANNELS = 3;
    long[] shape = new long[] {BATCH_SIZE, img.getHeight(), img.getWidth(), CHANNELS};
    return Tensor.create(UInt8.class, shape, ByteBuffer.wrap(data));
  }

}
