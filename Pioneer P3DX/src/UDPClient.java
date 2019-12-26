/**
 * Created with IntelliJ IDEA.
 * User: Vishnu Prem
 * Date: 6/26/18
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class UDPClient {
    /**
     * Created with IntelliJ IDEA.
     * User: Vishnu Prem
     * Date: 4/11/18
     * Time: 11:27 PM
     * To change this template use File | Settings | File Templates.
     */
    import javax.imageio.ImageIO;
    import java.awt.image.BufferedImage;
    import java.io.ByteArrayOutputStream;
    import java.io.File;
    import java.net.DatagramPacket;
    import java.net.DatagramSocket;
    import java.net.InetAddress;
    import java.util.Scanner;

    public class UDPClient  {

        DatagramSocket ds;
        DatagramSocket ds2;
        ByteArrayOutputStream baos;
        byte[] buffer;
        byte[] buffer2;
        InetAddress ia;
        DatagramPacket packet;
        DatagramPacket packet2;

        UDPClient()throws  Exception{

            ds = new DatagramSocket();
            baos = new ByteArrayOutputStream();
            //ia = InetAddress.getByName("146.87.112.243");       //VIRTALIS PC
            //ia = InetAddress.getByName("10.99.220.183");     //laptop
            ia = InetAddress.getByName("localhost");     //ROBOT-PC

            ds2 = new DatagramSocket(8888);

        }

        public void send_img(BufferedImage img) throws Exception{
            baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            baos.flush();

            buffer = baos.toByteArray();

            //System.out.printf("\nSize %s",buffer.length);

            packet = new DatagramPacket(buffer, buffer.length, ia, 9999);
            ds.send(packet);

        }

        public String receive_detection_output() throws Exception{
            buffer2 = new byte[1024];
            packet2 = new DatagramPacket(buffer2,buffer2.length);
            ds2.receive(packet2);
            String str = new String(packet2.getData(),0,packet2.getLength());
            return str;
        }


        public String receive_num_output() throws Exception{
            buffer2 = new byte[1024];
            packet2 = new DatagramPacket(buffer2,buffer2.length);
            ds2.receive(packet2);
            String str = new String(packet2.getData(),0,packet2.getLength());
            return str;
        }

        public String receive_label_output() throws Exception{
            buffer2 = new byte[1024];
            packet2 = new DatagramPacket(buffer2,buffer2.length);
            ds2.receive(packet2);
            String str = new String(packet2.getData(),0,packet2.getLength());
            return str;
        }

        public String receive_boxes_output() throws Exception{
            buffer2 = new byte[1024];
            packet2 = new DatagramPacket(buffer2,buffer2.length);
            ds2.receive(packet2);
            String str = new String(packet2.getData(),0,packet2.getLength());
            return str;
        }


        public void get_acknowledgement() throws Exception{

            buffer2 = new byte[1024];
            packet2 = new DatagramPacket(buffer2,buffer2.length);
            ds2.receive(packet2);
            String str = new String(packet2.getData(),0,packet2.getLength());
            //System.out.println("result is "+ str);
            //System.out.println("Acknowledged");

        }


        public void load()throws Exception{

            BufferedImage img = ImageIO.read(new File("saved.jpg"));
            send_img(img);

        }

        public void send_int()throws Exception{

            DatagramSocket ds = new DatagramSocket();
            Scanner input = new Scanner(System.in);

            int i = 1;
            while(i!=0)
            {
                System.out.println("Enter num: ");
                i = input.nextInt();

                byte[] b = String.valueOf(i).getBytes();

                InetAddress ia = InetAddress.getByName("146.87.112.243");
                DatagramPacket dp = new DatagramPacket(b,b.length,ia,9999);
                ds.send(dp);


                //Send complete

                byte[] b1 = new byte[1024];
                DatagramPacket dp1 = new DatagramPacket(b1,b1.length);
                ds.receive(dp1);

                String str = new String(dp1.getData(),0,dp1.getLength());
                System.out.println("result is "+ str);
            }
        }
    }
}
