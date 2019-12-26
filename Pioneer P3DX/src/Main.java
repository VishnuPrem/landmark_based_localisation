/**
 * Created with IntelliJ IDEA.
 * User: Vishnu Prem
 * Date: 6/26/18
 * Time: 3:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    import com.mobilerobots.Aria.ArPose;
    import com.sun.org.apache.xpath.internal.SourceTree;
    import robot.Robot;
    import sun.management.snmp.util.SnmpCachedData;
    import utils.Delay;

    import javax.imageio.ImageIO;
    import java.awt.*;
    import java.awt.image.BufferedImage;
    import java.io.*;
    import java.net.DatagramPacket;
    import java.net.DatagramSocket;
    import java.net.InetAddress;
    import java.text.SimpleDateFormat;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.Scanner;

    /**
     * Created by Theo Theodoridis.
     * Class    : Main
     * Version  : v1.0
     * Date     : © Copyright 05-04-2018
     * User     : ttheod
     * email    : ttheod@gmail.com
     * Comments :
     **/

    public class Main
    {
        private static Robot robot;
        private static int memory = 0;
        static boolean rotation_first_run_flag = true;      //first iteration of localizing?
        static int start_angle = 0;

        static boolean map_landmarks = true;
        static boolean localize_landmarks = true;
        static boolean turn_after_localise = false;
        static boolean target_reached = false;
        private static Date time_target_seen;

        static boolean save_star_pos = false;
        static double star_kin_X = 0.0,star_kin_Y = 0.0,star_kin_Th = 0.0;

        /**
         * Method     : Main::Main()
         * Purpose    : Default Main class constructor.
         * Parameters : args : The program's arguments.
         * Returns    : Nothing.
         * Notes      : None.
         **/
        public Main(String args[])
        {
            robot = new Robot();
            robot.init(args, robot);
//        robot.arRobot.moveTo(new ArPose(0, 0, 0));
            update.start();
        }

        /**
         * Thread     : Run::update()
         * Purpose    : To run the update thread.
         * Parameters : None.
         * Returns    : Nothing.
         * Notes      : None.
         **/
        Thread update = new Thread()
        {
            public void run()
            {
                while(true)
                {
                    // your code...
                    Delay.ms(1);
                }
            }
        };

        /**
         * Method     : Run::main()
         * Purpose    : Default main method which runs the Run class.
         * Parameters : - args : Initialization parameters.
         * Returns    : Nothing.
         * Notes      : None.
         **/
        public static void main(String args[])throws Exception
        {
            new Main(args);

            String mode = "";


            //String[] target_name_list = new String[]{"bed", "potted plant", "star", "couch", "star", "potted plant"};
            String[] target_name_list = new String[]{"star","tv","potted plant", "bed", "couch"};
            //String[] target_name_list = new String[]{"star", "chargepoint", "tv"};

            String target_name;
            int target_number = 0;

            UDPClient client = new UDPClient();
            objects detected_objects = new objects();
            landmarks landmark = new landmarks();

            boolean print_flag = true;
            /*
            Scanner input = new Scanner(System.in);
            System.out.println("1. Reload status 2. Reload landarks location only  ");
            int choice = input.nextInt();
            */
            int choice = 2;
            if(choice == 1){
                target_number = load_status(landmark);
                time_target_seen = new Date();
                rotation_first_run_flag = true;

            }
            else if(choice ==2){
                load_only_landmarks_from_status(landmark);
                time_target_seen = new Date();
            }

            save_to_csv("time,X,Y,theta,mode,next target," + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),"robot_kinematics.csv");
            while(true)
            {
                target_name = target_name_list[target_number];

                robot.sensor.vision.start();
                {
                    if(print_flag)
                    {

                        System.out.printf
                                (
                                        "\rOdometry: X = %.1f, Y = %.1f, Th = %.1f, " +
                                                //"Sensors: s(0) = %.1f, s(1) = %.1f s(2) = %.1f s(3) = %.1f, s(4) = %.1f s(5) = %.1f s(6) = %.1f s(7) = %.1f, " +
                                                "Camera: RGB: %d, Depth: %d,",
                                        robot.kinematics.getX(), robot.kinematics.getY(), robot.kinematics.getTh(),
                                        //robot.sensor.getSonarRange(0),robot.sensor.getSonarRange(1),robot.sensor.getSonarRange(2), robot.sensor.getSonarRange(3),robot.sensor.getSonarRange(4),robot.sensor.getSonarRange(5),robot.sensor.getSonarRange(6), robot.sensor.getSonarRange(7),
                                        robot.sensor.vision.getRgbPixel(10, 10).getRed(), robot.sensor.vision .getDepthPixel(639, 479)
                                        // detected_objects.num_of_objects, Arrays.toString(detected_objects.labels)
                                );
                        robot.sensor.vision.setRgbPixel(10, 10, Color.RED);
                        robot.sensor.vision.setDepthPixel(100, 10, Color.GREEN);

                        print_flag = false;
                    }


                    object_detection(client,detected_objects);

                    if(map_landmarks){

                        perform_mapping(landmark,detected_objects);
                        mode = "register landmark";
                    }
                    else if(localize_landmarks)
                    {
                        perform_localization(landmark,detected_objects,target_name);

                        if(find_target_index(detected_objects,target_name)!= -1)
                        {
                            localize_landmarks = false;
                            target_reached = false;
                            robot.control.stop();
                            System.out.printf("\n%s seen while localizing",target_name);
                            time_target_seen = new Date();
                        }
                        mode = "localise robot";
                    }

                    else if(turn_after_localise){

                        turn_to_computed_target_after_localization(landmark);
                        if(find_target_index(detected_objects,target_name)!= -1)
                        {
                            turn_after_localise = false;
                            target_reached = false;
                            robot.control.stop();
                            System.out.printf("\n%s seen after localizing",target_name);
                            time_target_seen = new Date();
                        }
                        mode = "turn after localise";
                    }

                    else if(target_reached == false)
                    {

                        target_reached = approach_target(detected_objects, find_target_index(detected_objects,target_name) ,
                                landmark.get_safe_distance(target_name));

                        Date current = new Date();
                        int time_elapsed = (int)(current.getTime() - time_target_seen.getTime())/1000;
                        if(time_elapsed > 0)
                            System.out.printf("Time to localise: %s\t", time_elapsed);
                        if(time_elapsed > 25){
                            localize_landmarks = true;
                            rotation_first_run_flag = true;
                            robot.control.stop();
                        }
                        mode =  "navigate to target";
                    }
                    /*
                  if(target_reached)
                  {

                      localize_landmarks = true;
                      target_reached = false;
                      rotation_first_run_flag = true;

                      target_number ++;
                      if(target_number >= target_name_list.length)
                          target_number = 0;

                      if(target_name.equals("star")){

                          if(!save_star_pos){
                              star_kin_X = robot.kinematics.getX();
                              star_kin_Y = robot.kinematics.getY();
                              star_kin_Th = robot.kinematics.getTh();
                              save_star_pos = true;
                              System.out.printf("\nStar position saved x:%s y:%s th:%s", star_kin_X, star_kin_Y, star_kin_Th);
                          }
                          else
                          {   robot.arRobot.moveTo(new ArPose(star_kin_X, star_kin_Y, star_kin_Th));
                              System.out.println("\nCircuit Complete!");
                          }
                      }

                      System.out.printf("\nNext target: %s\n",target_name_list[target_number]);
                      memory = 0;
                  }  */


                    //System.out.printf("Number of objects:%s; Labels:%s", detected_objects.num_of_objects, Arrays.toString(detected_objects.labels));
                }

                save_status(target_number,landmark);
                save_to_csv(new Date().getTime() + "," + robot.kinematics.getX() + "," + robot.kinematics.getY()
                        + "," + robot.kinematics.getTh() + "," + mode + "," + target_name, "robot_kinematics.csv");



                robot.sensor.vision.end();
                Delay.ms(1);
            }
        }

        private static void save_to_csv(String data,String file_name){
            try{
                PrintWriter fileio = new PrintWriter(new FileWriter(file_name,true));
                fileio.print(data+"\n");
                fileio.close();
            }
            catch(Exception E){
                System.out.println("\n File not found");
            }
        }

        private static void save_status(int target_number, landmarks landmark) throws Exception{

            String status;

            if(map_landmarks)                  //0
                status = "1";
            else
                status = "0";

            if(localize_landmarks)             //1
                status = status+"|1";
            else
                status = status+"|0";

            if(turn_after_localise)            //2
                status = status+"|1";
            else
                status = status+"|0";

            if(target_reached)                 //3
                status = status+"|1";
            else
                status = status+"|0";

            status = status + "|" + target_number + "|";  //4

            int count = 0;
            while(count < 9){                              //5

                status = status + landmark.coordinate[count][0]+ "+" + landmark.coordinate[count][1] + "+";
                count++;
            }

            status = status + "|" + robot.kinematics.getX() + "|" + robot.kinematics.getY() + "|" + robot.kinematics.getTh();
            //6                        //7                              //8
            status = status + "|" + star_kin_X + "|" + star_kin_Y  +"|" + star_kin_Th;
            //9                   //10          //11

            PrintWriter fileio = new PrintWriter("status.txt");
            fileio.print(status);
            fileio.close();


            //System.out.println(status);
        }

        public static int load_status(landmarks landmark)throws Exception{

            Scanner scanner = new Scanner( new File("status.txt") );
            String text = scanner.useDelimiter("\\A").next();
            scanner.close();

            System.out.println(text);

            String[] status = text.split("\\|");

            if(status[0].equals("1"))
                map_landmarks = true;
            else
                map_landmarks = false;

            if(status[1].equals("1"))
                localize_landmarks = true;
            else
                localize_landmarks = false;

            if(status[2].equals("1"))
                turn_after_localise = true;
            else
                turn_after_localise = false;

            if(status[3].equals("1"))
                target_reached = true;
            else
                target_reached = false;

            String[] coods = status[5].split("\\+");

            for(int i = 0; i < 9; i++){
                landmark.coordinate[i][0] = Integer.valueOf(coods[i*2]);
                landmark.coordinate[i][1] = Integer.valueOf(coods[(i*2)+1]);
                if(landmark.coordinate[i][0]!=0 || landmark.coordinate[i][1]!=0)
                    landmark.landmark_mapped[i] = true;
            }

            double robot_kin_X = Double.parseDouble(status[6]);
            double robot_kin_Y = Double.parseDouble(status[7]);
            double robot_kin_Th = Double.parseDouble(status[8]);

            //System.out.printf("x %s y %s z %s",robot_kin_X,robot_kin_Y,robot_kin_Th);

            star_kin_X = Double.parseDouble(status[9]);
            star_kin_Y = Double.parseDouble(status[10]);
            star_kin_Th = Double.parseDouble(status[11]);

            if(star_kin_X!=0 || star_kin_Y!=0 || star_kin_Th!=0)
                save_star_pos = true;

            robot.arRobot.moveTo(new ArPose(robot_kin_X, robot_kin_Y, robot_kin_Th));
            //robot.arRobot.moveTo(new ArPose(0,0,0));

            return Integer.valueOf(status[4]);

        }

        private static void load_only_landmarks_from_status(landmarks landmark)throws Exception{

            Scanner scanner = new Scanner( new File("status.txt") );
            String text = scanner.useDelimiter("\\A").next();
            scanner.close();

            System.out.println(text);

            String[] status = text.split("\\|");


            map_landmarks = false;

            String[] coods = status[5].split("\\+");

            for(int i = 0; i < 9; i++){
                landmark.coordinate[i][0] = Integer.valueOf(coods[i*2]);
                landmark.coordinate[i][1] = Integer.valueOf(coods[(i*2)+1]);
                if(landmark.coordinate[i][0]!=0 || landmark.coordinate[i][1]!=0)
                    landmark.landmark_mapped[i] = true;
            }

        }

        private static void perform_mapping (landmarks landmark, objects obj)
        {
            int current_angle = (int)robot.kinematics.getTh();
            if(rotation_first_run_flag)
            {
                System.out.println("\nMapping started");
                start_angle = current_angle;
                rotation_first_run_flag = false;
                robot.control.turnSpot(-10);

            }
            else if(rotation_complete(current_angle,start_angle))  //last iteration
            {
                System.out.println("\nMapping finished");
                map_landmarks = false;
                rotation_first_run_flag = true;
                localize_landmarks = true;             //MAKE TRUE
                robot.control.stop();

                landmark.set_landmarks_mapped();
                landmark.find_mean_and_confidence();
                landmark.show_details();

                landmark.compute_landmark_coordinates();


                return;
            }

            landmark.update_landmarks(obj,current_angle);
        }

        private static void perform_localization(landmarks landmark, objects obj,String target_name)
        {
            int current_angle = (int)robot.kinematics.getTh();
            if(rotation_first_run_flag)                                         //first iteration
            {
                System.out.println("\nLocalizing started");
                start_angle = current_angle;
                rotation_first_run_flag = false;
                robot.control.turnSpot(-11);
            }
            else if(rotation_complete(current_angle,start_angle))  //last iteration
            {
                System.out.println("\nLocalization finished");
                localize_landmarks = false;
                rotation_first_run_flag = true;
                turn_after_localise = true;
                robot.control.stop();


                landmark.find_mean_and_confidence();
                landmark.show_details();

                landmark.compute_robot_coordinate();

                int angle_offset = landmark.get_angle_offset();
                int target_index = landmark.get_target_index(target_name);
                int target_angle_actual = landmark.get_angle_from_points(landmark.robot_x,landmark.robot_y, landmark.coordinate[target_index][0], landmark.coordinate[target_index][1]);
                landmark.target_angle_robot = find_target_angle_robot(angle_offset + target_angle_actual);

                System.out.printf("\nGlobal angle to %s: %s ", target_name, target_angle_actual);
                //System.out.printf("\nOffset angle = %s Actual angle = %s Robot angle = %s ",angle_offset, target_angle_actual, landmark.target_angle_robot);

                //landmark.turn_to_computed_target_location(target_name);
                return;
            }

            landmark.update_landmarks(obj,current_angle);

        }

        private static int find_target_angle_robot(int angle){

            if(angle > 359)
                angle = angle % 360;
            else if (angle < 0)
                angle = 360 + angle;
            return angle;

        }

        private  static void turn_to_computed_target_after_localization(landmarks landmark){


            int robot_angle = landmark.get_angle_from_robot_angle(Math.round((float)robot.kinematics.getTh()));

            if(Math.abs(landmark.target_angle_robot - robot_angle) < 5){
                robot.control.stop();
                turn_after_localise = false;
                target_reached = false;
                System.out.println("\nTurned to target");
                time_target_seen = new Date();

            }
            else if(landmark.target_angle_robot > 90 && landmark.target_angle_robot < 270)
                robot.control.turnSpot(-15);
            else
                robot.control.turnSpot(15);

        }


        private static boolean rotation_complete(int current_angle, int start_angle){

            //System.out.printf("\nstart: %s current: %s",start_angle,current_angle);
            if(start_angle < -175 && current_angle > 175)
                return true;
            else if(start_angle > current_angle && start_angle - current_angle < 10)
                return true;
            else
                return false;
        }

        private static void object_detection(UDPClient client, objects detected_objects) throws Exception{
            BufferedImage img = robot.sensor.vision.getRgbImage();
            client.send_img(img);
            detected_objects.update_all(client.receive_detection_output());

            find_centroid_depth(detected_objects);
            find_min_depth(detected_objects);

            //find_avg_depth(detected_objects);


        }

        private static int find_target_index(objects obj, String target){

            for(int i = 0; i<obj.num_of_objects ; i++){
                if(obj.labels[i].equals(target)){
                    //System.out.println("Star found");
                    return i;
                }
            }
            //System.out.println("Star not found");
            return -1;
        }

        private static  boolean approach_target(objects obj, int index, int min_distance){
            /*
            if(index!=-1)
            {System.out.println(obj.normalized_centroids[index][1]);
                System.out.println(obj.min_depth[index]);
            }
            */
            if( index != -1)
                if(obj.min_depth[index] < min_distance)
                {   System.out.printf("\rTARGET REACHED!");
                    //Toolkit.getDefaultToolkit().beep();
                    robot.control.stop();
                    return true;
                }
            if(is_obstacle_front_left(300) && is_obstacle_front_right(300) && is_obstacle_left_side(300) && is_obstacle_right_side(300))
            {
                robot.control.move(-20);
                System.out.println("Reverse");
            }
            else if( is_obstacle_front_left(300) == true && is_obstacle_right_side(300) == false)             //turn right if obstacle on left
            {    robot.control.turnSpot(20);
                if(memory != 0)
                    memory = 2;
                System.out.printf("\rRight       avoid object");
            }
            else  if(is_obstacle_front_right(300) == true && is_obstacle_left_side(300) == false)       //turn left  if obstacle on right
            {    robot.control.turnSpot(-20);
                if(memory != 0)
                    memory = 1;
                System.out.printf("\rLeft        avoid object");
            }
            else if ( index != -1)                       //if target is seen
            {
                time_target_seen = new Date();

                if(obj.normalized_centroids[index][1] < 0.40 && is_obstacle_right_side(300) == false && is_obstacle_front_right(400) == false)                      //if target on right
                {   robot.control.turnSpot(10);
                    memory = 3;            //to indicate target was found once
                    System.out.printf("\rRight       towards target");
                }
                else if(obj.normalized_centroids[index][1] > 0.60 && is_obstacle_left_side(300) == false && is_obstacle_front_left(400) == false)                 //if target on left
                {    robot.control.turnSpot(-10);
                    memory = 3;
                    System.out.printf("\rLeft        towards target");
                }
                else if(obj.min_depth[index] < min_distance)                                                         // if target too close
                {
                    System.out.printf("\rTARGET REACHED!");
                    //Toolkit.getDefaultToolkit().beep();
                    robot.control.stop();
                    return true;
                }
                else if( obj.normalized_centroids[index][1] >= 0.45 && obj.normalized_centroids[index][1] <= 0.55 )
                {
                    robot.control.move(50);
                    System.out.printf("\rStraight    towards target\tdistance: %s\n",obj.min_depth[index]);
                    memory = 3;
                }

                else                                                                                           //if target seen straight ahead
                {   robot.control.move(50);
                    System.out.printf("\rStraight    avoiding side when target seen");

                }
            }
            else if(memory == 1 && is_obstacle_right_side(300) == false)
            {

                robot.control.turnSpot(10);
                System.out.printf("\rRight       from memory");
            }
            else if(memory == 2 && is_obstacle_left_side(300) == false)
            {
                robot.control.turnSpot(-10);
                System.out.printf("\rLeft        from memory");
            }
            else if (memory == 1 && is_obstacle_right_side(300) == true)
            {
                time_target_seen = new Date();
                robot.control.move(50);
                System.out.printf("\rStraight    avoid right side when target not seen");
            }
            else if (memory == 2 && is_obstacle_left_side(300) == true)
            {
                time_target_seen = new Date();
                robot.control.move(50);
                System.out.printf("\rStraight    avoid left side when target not seen");
            }
            else                                                                                               //if no target seen and no obstacle
            {
                robot.control.move(50);
                System.out.printf("\rStraight    freely");
            }
            //System.out.println("\t\t\t\t\t"+turned_side);

            return false;
        }


        /*
        private static void  avoid_obstacle( int st){


            if( robot.sensor.getSonarRange(1) < st || robot.sensor.getSonarRange(2) < st || robot.sensor.getSonarRange(3) < st)             //turn right
                robot.control.turnSpot(40);
            else  if(robot.sensor.getSonarRange(4) < st || robot.sensor.getSonarRange(5) < st || robot.sensor.getSonarRange(6) < st )       //turn left
                robot.control.turnSpot(-40);
            else
                robot.control.move(80);
        }

        private static boolean  avoid_obstacle_during_approach_target(){


            if( is_obstacle_front(300) == 1)             //turn right
                robot.control.turnSharp(40);
            else  if(is_obstacle_front(300) == 2 )       //turn left
                robot.control.turnSharp(-40);
            else
                 return false;

            return true;
        }
        */

        /*
        public static int is_obstacle_front(int st){

            if( robot.sensor.getSonarRange(1) < st-100 || robot.sensor.getSonarRange(2) < st-50 || robot.sensor.getSonarRange(3) < st)             //obstacle on left
                return 1;
            else  if(robot.sensor.getSonarRange(4) < st || robot.sensor.getSonarRange(5) < st-50 || robot.sensor.getSonarRange(6) < st-100 )       //obstacle on right
                return 2;
            else
               return 0;
        }
        */

        public static boolean is_obstacle_front_left(int st){
            if( robot.sensor.getSonarRange(1) < st-100 || robot.sensor.getSonarRange(2) < st-50 || robot.sensor.getSonarRange(3) < st)             //obstacle on left
                return true;
            else
                return false;
        }

        public static boolean is_obstacle_front_right(int st){
            if(robot.sensor.getSonarRange(4) < st || robot.sensor.getSonarRange(5) < st-50 || robot.sensor.getSonarRange(6) < st-100 )       //obstacle on right
                return true;
            else
                return false;
        }

        /*
        public static int is_obstacle_side(int st){

            if( robot.sensor.getSonarRange(0) < st || robot.sensor.getSonarRange(1) < st)             //obstacle on left
                return 2;
            else  if(robot.sensor.getSonarRange(6) < st || robot.sensor.getSonarRange(7) < st )       //obstacle on right
                return 1;
            else
                return 0;
        }
        */

        public static boolean is_obstacle_right_side(int st){
            if( robot.sensor.getSonarRange(6) < st || robot.sensor.getSonarRange(7) < st)             //obstacle on left
                return true;
            else
                return false;
        }
        public static boolean is_obstacle_left_side(int st){
            if( robot.sensor.getSonarRange(0) < st || robot.sensor.getSonarRange(1) < st)             //obstacle on left
                return true;
            else
                return false;
        }

        /*
        private static void save(BufferedImage image) {

            try{
            File outputfile = new File("D:\\Projects\\Pioneer 2\\images\\saved.jpg");
            ImageIO.write(image,"jpg",outputfile);
            }catch(IOException e){
                System.out.println(e);}
        }

        private static void centroid_depth_color(objects obj){

            int count = 0;
            while(count<obj.num_of_objects){
                int y = obj.centroids[count][0];
                int x = obj.centroids[count][1];
                for (int i = x; i<x+10 && i<640 ; i++)
                  for(int j= y; j<y+10 && j<480; j++)
                      robot.sensor.vision.setDepthPixel(i,j,Color.green);
               count++;
            }
        }
        */

        private static void find_min_depth(objects obj){

            obj.min_depth = new int[obj.num_of_objects];
            int count = 0;
            int i=0,j=0,x1,y1,x2,y2,depth_min;

            while(count < obj.num_of_objects){

                y1 = obj.boxes[count][0];
                x1 = obj.boxes[count][1];
                y2 = obj.boxes[count][2];
                x2 = obj.boxes[count][3];

                //x_min = x1;
                //y_min = y1;
                depth_min = 10000;

                for (i = x1; i<x2 ; i++)
                {       for(j= y1; j<y2; j++)
                {   //if(i==x1 || i==x2-1 || j==y1 || j==y2-1)
                    //  robot.sensor.vision.setDepthPixel(i,j,Color.red);
                    if(robot.sensor.vision.getDepthPixel(i,j)<depth_min && robot.sensor.vision.getDepthPixel(i,j)!=0)
                    {
                        //x_min = i;
                        //y_min = j;
                        depth_min = robot.sensor.vision.getDepthPixel(i,j);
                    }
                }
                }
                if(obj.centroid_depth[count] - depth_min > 1000)
                    obj.min_depth[count] = obj.centroid_depth[count];
                else
                    obj.min_depth[count] = depth_min;

                //System.out.printf("\nDepth: %s at %s,%s", depth_min,x_min,y_min);
                //System.out.printf("\n %s at distance %s",obj.labels[count],obj.min_depth[count]);
                count++;

                //System.out.printf(java.util.Arrays.toString(obj.min_depth));
            }
        }

        private static void find_avg_depth(objects obj){

            obj.avg_depth = new int[obj.num_of_objects];
            int count = 0;
            int i=0,j=0,x1,y1,x2,y2,depth_min;

            while(count < obj.num_of_objects){

                y1 = obj.boxes[count][0];
                x1 = obj.boxes[count][1];
                y2 = obj.boxes[count][2];
                x2 = obj.boxes[count][3];

                long tot_depth = 0;
                int counter = 0;
                for (i = x1; i<x2 ; i++)
                    for(j= y1; j<y2; j++)
                    { tot_depth += robot.sensor.vision.getDepthPixel(i,j);
                        counter++;
                    }

                obj.min_depth[count] =(int) tot_depth/counter;

                //System.out.printf("\nDepth: %s at %s,%s", depth_min,x_min,y_min);
                //System.out.printf("\n %s at distance %s",obj.labels[count],obj.min_depth[count]);
                count++;

                //System.out.printf("%s"obj.min_depth[count]);
            }


        }

        private static void find_centroid_depth(objects obj){

            int count = 0;
            obj.centroid_depth = new int[obj.num_of_objects];

            while(count < obj.num_of_objects){

                obj.centroid_depth[count] = robot.sensor.vision.getDepthPixel(obj.centroids[count][1],obj.centroids[count][0]);
                count++;

            }

        }


    }
}
