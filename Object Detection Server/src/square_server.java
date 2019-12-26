/**
 * Created with IntelliJ IDEA.
 * User: SIRC
 * Date: 12/04/18
 * Time: 17:47
 * To change this template use File | Settings | File Templates.
 */
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class square_server {
    public static void main(String[] args)throws Exception {

        DatagramSocket ds = new DatagramSocket(9999);
        byte[] b1 = new byte[1024];
        InetAddress ia = InetAddress.getByName("10.99.200.148");

        while(true){


        DatagramPacket dp = new DatagramPacket(b1,b1.length);
        ds.receive(dp);

        String str = new String(dp.getData(),0,dp.getLength());
        System.out.println("str "+str);
        int num = Integer.parseInt(str.trim());

        int[] result = {num,num,num,num};

        byte[] b2 = java.util.Arrays.toString(result).getBytes();
        System.out.println(java.util.Arrays.toString(result));

        DatagramPacket dp1 = new DatagramPacket(b2,b2.length,ia,dp.getPort());
        ds.send(dp1);
    }              }
}


