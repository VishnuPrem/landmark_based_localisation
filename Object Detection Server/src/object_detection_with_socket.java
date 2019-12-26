import java.awt.image.BufferedImage;

/**
 * Created with IntelliJ IDEA.
 * User: SIRC
 * Date: 16/04/18
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */
public class object_detection_with_socket {

    public static void main(String[] args) throws Exception{
        drawBoxesFromMultipleModels D = new drawBoxesFromMultipleModels();
        UDPServer server = new UDPServer();
        System.out.println("\n\nSERVER READY");

        //String s = new String();

        while(true){

            BufferedImage img = server.receive_img();
            //System.out.println("\nReceived Image");
            D.getDetection(img);
            server.send_all_detection_output(D.total_num_of_objects, D.output_labels, D.output_boxes);

        }

    }
}
