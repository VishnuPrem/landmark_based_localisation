/**
 * Created with IntelliJ IDEA.
 * User: Vishnu Prem
 * Date: 6/26/18
 * Time: 3:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class objects {
    import java.awt.image.BufferedImage;

    /**
     * Created with IntelliJ IDEA.
     * User: Robot
     * Date: 11/04/18
     * Time: 13:15
     * To change this template use File | Settings | File Templates.
     */
    public class objects {
        int num_of_objects;
        String[] labels;
        int[][] boxes;
        int[][] centroids;
        int[] min_depth;
        double[][] normalized_centroids;
        int[] centroid_depth;
        int[] avg_depth;


        public objects(){
            num_of_objects = 0;
            labels = new String[0];
        }



        public void update_all(String s){
            //System.out.println(s);
            String[] outputs = s.split("\\|");

            update_num_of_objects(outputs[0]);
            labels = new String[0];
            if (num_of_objects == 0)
                return;

            update_labels(outputs[1]);
            update_boxes(outputs[2]);
            find_centroids();

            //System.out.println(outputs.length);
            //System.out.printf("outputs1:%s and %s and %s",outputs[0],outputs[1],outputs[2]);
        }


        public void update_num_of_objects(String s){
            num_of_objects = Integer.parseInt(s);
            //System.out.printf("Received:%s",num_of_objects);
        }


        public void update_labels(String s){
            labels = s.split("\\+");
            //System.out.println(java.util.Arrays.toString(labels));
        }


        public void update_boxes(String s){
            String[] temp_boxes = s.split("\\+");
            int num = temp_boxes.length;
            boxes = new int[num_of_objects][4];

            //System.out.println(num_of_objects);
            for(int i = 0, j = 0 ; i < num_of_objects; i++,j += 4){
                boxes[i][0]= (Integer.parseInt(temp_boxes[j]))*2;               //since depth image resolution = 640x480
                boxes[i][1]= (Integer.parseInt(temp_boxes[j+1]))*2;             //       rgb image resolution = 320x240
                boxes[i][2]= (Integer.parseInt(temp_boxes[j+2]))*2;
                boxes[i][3]= (Integer.parseInt(temp_boxes[j+3]))*2;

                if(boxes[i][2] > 479)
                    boxes[i][2] = 479;
                if(boxes[i][3] > 639)
                    boxes[i][3] = 639;

                //System.out.println(java.util.Arrays.toString(boxes[i]));
            }

        }


        public void find_normalised_centroids(){
            normalized_centroids = new double[num_of_objects][2];

            for(int i = 0; i<num_of_objects;i++){
                normalized_centroids[i][0] = centroids[i][0]/480.0;
                normalized_centroids[i][1] = centroids[i][1]/640.0;

            }

        }


        public void find_centroids(){
            centroids = new int[num_of_objects][2];
            for(int i = 0;i<num_of_objects; i++){
                centroids[i][0] = ((boxes[i][0] + boxes[i][2])/2);    //centroid y, height
                centroids[i][1] = ((boxes[i][1] + boxes[i][3])/2);    //centroid x, width
                //System.out.printf("cent: %s,%s",centroids[i][0],centroids[i][1]);

            }
            find_normalised_centroids();
        }

    }

}
