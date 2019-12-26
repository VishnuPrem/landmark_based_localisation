/**
 * Created with IntelliJ IDEA.
 * User: Robot
 * Date: 09/04/18
 * Time: 13:29
 * To change this template use File | Settings | File Templates.
 */


import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import java.awt.image.BufferedImage;


public class VideoCapture
{
    private CanvasFrame canvas = new CanvasFrame("Web Cam");

    public VideoCapture()
    {
        canvas.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
    }

    public void beginCapture()
    {
        Frame capturedframe = null;
        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        Java2DFrameConverter paintConverter = new Java2DFrameConverter();

        drawBoxesFromMultipleModels D = new drawBoxesFromMultipleModels();
        try
        {
            grabber.start();


            while(true)
            {
                capturedframe = grabber.grabFrame();
                BufferedImage buff = paintConverter.getBufferedImage(capturedframe);

                if(buff != null)
                {

                    canvas.showImage(buff);
                    D.getDetection(buff);
                }

            }
        }catch(Exception e)
        {
            System.out.println("Exception<MonoGrabber::run>: " + e);
        }
    }

    public static void main(String args[])
    {
        new VideoCapture().beginCapture();
    }
}
