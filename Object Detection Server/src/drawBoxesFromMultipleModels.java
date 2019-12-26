/**
 * Created with IntelliJ IDEA.
 * User: Robot
 * Date: 09/04/18
 * Time: 13:10
 * To change this template use File | Settings | File Templates.
 */


import org.bytedeco.javacv.CanvasFrame;

import java.awt.*;
import java.awt.image.BufferedImage;


public class drawBoxesFromMultipleModels {

    DetectFromModel1 trained_detector;
    DetectFromModel2 retrained_detector;
    CanvasFrame canvas;
    boolean use_both_detectors;

    String output_labels;
    String output_boxes;
    int total_num_of_objects;

    drawBoxesFromMultipleModels() {

        use_both_detectors = true;
        trained_detector = new DetectFromModel1("models/ssd_mobilenet_v1_coco_2017_11_17/saved_model","models/ssd_mobilenet_v1_coco_2017_11_17/mscoco_label_map.pbtxt");
        retrained_detector = new DetectFromModel2("models/chargepoint_and_star/saved_model","models/chargepoint_and_star/labelmap.pbtxt");

        canvas = new CanvasFrame("Web Cam");
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }






    public void getDetection( BufferedImage buff)throws Exception{   //runs image through detection model and prints centroids
        BufferedImage final_img;

        trained_detector.detect(buff);
        if(use_both_detectors == true)
             {retrained_detector.detect(buff); }

        BufferedImage img1 = constructRectangle(buff, trained_detector);
        if(use_both_detectors == true)
            { BufferedImage img2 = constructRectangle2(img1, retrained_detector);
              final_img = img2;
            }
        else
            final_img = img1;

        canvas.showImage(final_img);

        pack_output_of_detection(trained_detector,retrained_detector);
        //System.out.printf("\n%s\t%s",java.util.Arrays.toString(trained_detector.getLabel()),java.util.Arrays.toString(retrained_detector.getLabel()));

    }

    public BufferedImage constructRectangle(BufferedImage img, DetectFromModel1 detector){

        int[][] centroids = detector.getCentroid();
        int[][] boxes = detector.getBoxes();
        String[] labels = detector.getLabel();


        Graphics2D draw = img.createGraphics();
        draw.setColor(Color.green);
        draw.setStroke(new BasicStroke(3));

        for(int i=0; i<centroids.length;i++){                        //iterates for each box in single image
            /*
            int box_y1 = (int) (boxes[i][0]*imgHeight); //x,top left of box                   //for denormalization
            int box_x1 = (int) (boxes[i][1]*imgWidth);//y,top left of box
            int box_y2 = (int) (boxes[i][2]*imgHeight); //x,bottom right of box
            int box_x2 = (int) (boxes[i][3]*imgWidth);//y,bottom right of box
            */

            int box_h = boxes[i][2]-boxes[i][0];
            int box_w = boxes[i][3]-boxes[i][1];

            draw.drawRect(boxes[i][1], boxes[i][0], box_w, box_h);
            draw.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            draw.drawString(labels[i],boxes[i][1],boxes[i][0]-5);

        }
        return img;

    }

    public BufferedImage constructRectangle2(BufferedImage img, DetectFromModel2 detector){

        int[][] centroids = detector.getCentroid();
        int[][] boxes = detector.getBoxes();
        String[] labels = detector.getLabel();


        Graphics2D draw = img.createGraphics();
        draw.setColor(Color.green);
        draw.setStroke(new BasicStroke(3));

        for(int i=0; i<centroids.length;i++){                        //iterates for each box in single image
            /*
            int box_y1 = (int) (boxes[i][0]*imgHeight); //x,top left of box                   //for denormalization
            int box_x1 = (int) (boxes[i][1]*imgWidth);//y,top left of box
            int box_y2 = (int) (boxes[i][2]*imgHeight); //x,bottom right of box
            int box_x2 = (int) (boxes[i][3]*imgWidth);//y,bottom right of box
            */

            int box_h = boxes[i][2]-boxes[i][0];
            int box_w = boxes[i][3]-boxes[i][1];

            draw.drawRect(boxes[i][1], boxes[i][0], box_w, box_h);
            draw.setFont(new Font("TimesRoman", Font.PLAIN, 20));
            draw.drawString(labels[i],boxes[i][1],boxes[i][0]-5);

        }
        return img;

    }

    public void pack_output_of_detection(DetectFromModel1 detector1,DetectFromModel2 detector2){
        //packs "num of objects" "label1+label2+label3.." "boxes[0][0]+boxes[0][1]...(four integers for each label)"
        total_num_of_objects = detector1.num_of_objects + detector2.num_of_objects;
        //System.out.println(total_num_of_objects);
        output_labels = new String();
        output_boxes = new String();


        //if(total_num_of_objects == 0)
        //    return;

        String[] labels = detector1.getLabel();
        for(int i = 0; i < detector1.num_of_objects; i++)
        {
            output_labels = output_labels + labels[i] + "+";
        }

        labels = detector2.getLabel();
        for(int i = 0; i < detector2.num_of_objects; i++)
        {
            output_labels = output_labels + labels[i] + "+";
        }
        //System.out.printf("\nPacked labels:%s",output_labels);

        int[][] boxes = detector1.getBoxes();
        for(int i = 0; i < detector1.num_of_objects; i++)
        {
            output_boxes = output_boxes + boxes[i][0] + "+" + boxes[i][1] + "+" + boxes[i][2] + "+" + boxes[i][3] + "+";
        }

        boxes = detector2.getBoxes();
        for(int i = 0; i < detector2.num_of_objects; i++)
        {
            output_boxes = output_boxes + boxes[i][0] + "+" + boxes[i][1] + "+" + boxes[i][2] + "+" + boxes[i][3] + "+";
        }
        //System.out.println(output_boxes);
    }
}
