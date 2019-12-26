/**
 * Created with IntelliJ IDEA.
 * User: Vishnu Prem
 * Date: 4/11/18
 * Time: 11:23 PM
 * To change this template use File | Settings | File Templates.
 */
import javax.imageio.ImageIO;
import javax.net.ssl.SSLEngine;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class UDPServer {

    DatagramSocket ds;
    DatagramSocket ds2;
    ByteArrayInputStream bais;
    byte[] buffer;
    byte[] buffer2;
    InetAddress ia;
    DatagramPacket packet;
    DatagramPacket packet2;

    UDPServer()throws Exception{

        ds = new DatagramSocket(9999);
        buffer = new byte[12866];
        ia = InetAddress.getByName("10.99.201.15");

        ds2 = new DatagramSocket();

    }

    public BufferedImage receive_img() throws Exception{

        DatagramPacket dp = new DatagramPacket(buffer,buffer.length);
        ds.receive(dp);

        buffer = dp.getData();
        bais = new ByteArrayInputStream(buffer);
        BufferedImage img = ImageIO.read(bais);

        return img;

    }

    public void send_all_detection_output(int num,String labels,String boxes)throws  Exception{

        String detection_output = num+"|"+labels+"|"+boxes;
        buffer2 = detection_output.getBytes();
        packet2 = new DatagramPacket(buffer2,buffer2.length,ia,8888);
        ds2.send(packet2);
        System.out.printf("\rData sent through socket: %s",detection_output);
    }

    public void send_num_of_objects(String S) throws Exception{
        buffer2 = S.getBytes();
        packet2 = new DatagramPacket(buffer2,buffer2.length,ia,8888);
        ds2.send(packet2);
        System.out.printf("\nSent: %s",S);
    }


    public void send_packed_label(String S) throws Exception{
       buffer2 = S.getBytes();
       packet2 = new DatagramPacket(buffer2,buffer2.length,ia,8888);
       ds2.send(packet2);
        //System.out.printf("\nSent: %s",S);
    }

    public void send_packed_boxes(String S) throws Exception{
        buffer2 = S.getBytes();
        packet2 = new DatagramPacket(buffer2,buffer2.length,ia,8888);
        ds2.send(packet2);
        //System.out.printf("\nSent: %s",S);

    }

    public void send_acknowledgement()throws  Exception{
        int val = 1;
        buffer2 = String.valueOf(val).getBytes();
        packet2 = new DatagramPacket(buffer2,buffer2.length,ia,8888);
        ds2.send(packet2);
        //System.out.println("sent ack");

    }

    public  void receive_int()throws Exception
    {
        DatagramSocket ds = new DatagramSocket(9999);

        byte[] b1 = new byte[1024];

        DatagramPacket dp = new DatagramPacket(b1,b1.length);

        while(true){

            System.out.println("\n\nSERVER READY");
            ds.receive(dp);
            String str = new String(dp.getData(),0,dp.getLength());
            System.out.println("str "+str);
            int num = Integer.parseInt(str.trim());

            System.out.println("num "+ num);
            int result = num*num;

            byte[] b2 = String.valueOf(result).getBytes();
            ia = InetAddress.getByName("10.99.201.218");
            DatagramPacket dp1 = new DatagramPacket(b2,b2.length,ia,dp.getPort());
            ds.send(dp1);
        }
    }
}
