/**
 * Created with IntelliJ IDEA.
 * User: Vishnu Prem
 * Date: 3/25/18
 * Time: 8:52 PM
 * To change this template use File | Settings | File Templates.
 */


import com.google.protobuf.TextFormat;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Tensor;
import org.tensorflow.types.UInt8;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static object_detection.protos.StringIntLabelMapOuterClass.StringIntLabelMap;
import static object_detection.protos.StringIntLabelMapOuterClass.StringIntLabelMapItem;

//detect from specific model


public class DetectFromModel1 {

    private static String modelpath;             //paths
    private static String labelpath;

    private static String[] labels;              //loaded from paths
    private static SavedModelBundle model;

    private String[] labels_detected;
    private int[][] boxes;               //output from model
    private int[][] centroids;
    public int num_of_objects;


    DetectFromModel1(String m_path, String l_path){                                                                //Constructor

        modelpath = m_path;
        labelpath= l_path;

        System.out.printf("\nLoading model: %s \t labels: %s",m_path,l_path);
        try{
            labels = loadLabels(labelpath);
        }
        catch(Exception e){}

        model = SavedModelBundle.load(modelpath, "serve");


    }

    public int[][] getCentroid()
    {
        return centroids;
    }

    public int[][] getBoxes(){
        return boxes;
    }

    public String[] getLabel(){
        return  labels_detected;
    }



    public void detect(BufferedImage img)throws IOException{                                   //runs detection model and obtains outputs

        List<Tensor<?>> outputs = null;
        Tensor<UInt8> input = makeImageTensor(img);

        //System.out.println("\t...detecting objects");
        outputs = model
                .session()
                .runner()
                .feed("image_tensor", input)
                .fetch("detection_scores")
                .fetch("detection_classes")
                .fetch("detection_boxes")
                .run();

        try (Tensor<Float> scoresT = outputs.get(0).expect(Float.class);
             Tensor<Float> classesT = outputs.get(1).expect(Float.class);
             Tensor<Float> boxesT = outputs.get(2).expect(Float.class)) {

            int maxObjects = (int) scoresT.shape()[1];
            float[] scores = scoresT.copyTo(new float[1][maxObjects])[0];
            float[] classes = classesT.copyTo(new float[1][maxObjects])[0];
            float[][] normalized_boxes = boxesT.copyTo(new float[1][maxObjects][4])[0];
            int objectsAboveThresh = 0;


            for (int i = 0; i < scores.length; ++i) {
                if (scores[i] > 0.5) {
                objectsAboveThresh++;
                }
            }
            num_of_objects = objectsAboveThresh;
            findBoxesOfAll(normalized_boxes,img);
            findCentroidOfAll();
            findLabelsOfAll(classes);

            for (int i = 0; i < scores.length; ++i) {
                if (scores[i] > 0.5) {
                    //System.out.printf("\t\tlabel:%s\tcentroid:%s",labels_detected[i],java.util.Arrays.toString(centroids[i]));

                }
            }

        }

    }

    private void findLabelsOfAll( float[] classes) {
        labels_detected = new String[num_of_objects];
        for(int i=0; i<num_of_objects; i++){
            labels_detected[i] = labels[(int) classes[i]];
        }
    }

    private void findBoxesOfAll(float[][] normalized_boxes, BufferedImage img){
        boxes = new int[num_of_objects][4];
        int ht = img.getHeight();
        int wd = img.getWidth();
        for (int i = 0; i<num_of_objects; i++){
            boxes[i][0]=(int)(ht*normalized_boxes[i][0]);
            boxes[i][2]=(int)(ht*normalized_boxes[i][2]);
            boxes[i][1]=(int)(wd*normalized_boxes[i][1]);
            boxes[i][3]=(int)(wd*normalized_boxes[i][3]);
            //System.out.println(java.util.Arrays.toString(boxes[i]));

        }
    }

    private void findCentroidOfAll (){ //calculates centroid of all boxes

        centroids = new int[num_of_objects][2];
        for (int i = 0; i<num_of_objects; i++){
            centroids[i][0] = ((boxes[i][0] + boxes[i][2])/2);            //Xc=b0+b2 /2
            centroids[i][1] = ((boxes[i][1] + boxes[i][3])/2);            //Yc=b1+b3 /2
            //System.out.println(java.util.Arrays.toString(centroids[i]));

        }
    }


    private String[] loadLabels(String filename) throws Exception {   //loads .pbtxt file
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


    private void bgr2rgb(byte[] data) {
        for (int i = 0; i < data.length; i += 3) {
            byte tmp = data[i];
            data[i] = data[i + 2];
            data[i + 2] = tmp;
        }
    }

    private Tensor<UInt8> makeImageTensor(BufferedImage img) throws IOException { //makes image into tensor

        if (img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            throw new IOException(
                    String.format(
                            "Expected 3-byte BGR encoding in BufferedImage, found %d . This code could be made more robust",
                            img.getType()));
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
