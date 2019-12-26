/**
 * Created with IntelliJ IDEA.
 * User: Vishnu Prem
 * Date: 6/26/18
 * Time: 3:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class landmarks {
    import java.io.FileWriter;
    import java.io.PrintWriter;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.Scanner;

    /**
     * Created with IntelliJ IDEA.
     * User: Robot
     * Date: 30/04/18
     * Time: 16:32
     * To change this template use File | Settings | File Templates.
     */
    public class landmarks {

        String[] landmark_name = {"bed",    "couch",    "tv",   "potted plant", "oven", "refrigerator","microwave", "star", "chargepoint"};    //check spelling
        int[] landmark_safe_distance = {800, 800,        700,         500,      500,        800,        700,        500,    500};
        //int[][] coordinate =    {{20,13},   {40,41},    {39,2},     {5,36},    {94,26},    {94,9},          {91,4},    {60,41},    {60,5}};
        //int[][] coordinate =    {{20,13},   {56,41},    {39,2},     {68,41},    {94,26},    {94,9},     {91,4},     {29,46},    {60,5}};
        //int [][] coordinate;
        int [][] coordinate = {{1500,1300},{5200,3600},{3900,200},{2200,4100},{0,0},{0,0},{0,0},{3600,4600},{0,0}};

        boolean [] landmark_seen;
        boolean [] landmark_mapped;
        int[] num_of_times_seen;
        ArrayList[] observed_distances;
        ArrayList[] observed_thetas;


        int[] mean_distance;
        int[] mean_theta;
        double[] d_confidence;
        double[] t_confidence;

        int robot_x;
        int robot_y;
        int target_angle_robot;

        landmarks()
        {
            //coordinate = new int[9][2];

            //int[][] coordinate =    {{20,13},   {56,41},    {39,2},     {68,41},    {94,26},    {94,9},     {91,4},     {29,46},    {60,5}};

            landmark_mapped = new boolean[]{false, false, false,
                    false, false, false,
                    false, false, false};

            int count = 0;
            while(count<9){
                if(coordinate[count][0]!=0 || coordinate[count][1]!=0)
                    landmark_mapped[count]= true;
                count++;
            }

            landmark_seen = new boolean[]{false, false, false,
                    false, false, false,
                    false, false, false};
            num_of_times_seen = new int[9];

            observed_distances = new ArrayList[9];
            for(int i = 0; i<9; i++)
                observed_distances[i] = new ArrayList();

            observed_thetas =  new ArrayList[9];
            for(int i = 0; i<9; i++)
                observed_thetas[i] = new ArrayList();

            mean_distance = new int[9];
            mean_theta =  new int[9];
            d_confidence = new double[9];
            t_confidence = new double[9];
        }

        public void update_landmarks(objects obj, int robot_angle)
        {

            int count = 0;
            int landmark_index;

            while(count < obj.num_of_objects)
            {
                landmark_index = -1;

                for(int i = 0; i < 9 ; i++){
                    if( obj.labels[count].equals(landmark_name[i]))
                    {
                        landmark_index = i;
                        //System.out.println(landmark_name[i]);
                    }
                }

                if(landmark_index == -1)
                {
                    count++;
                    continue;
                }

                if(landmark_seen[landmark_index]== false)
                {
                    landmark_seen[landmark_index] = true;
                    num_of_times_seen[landmark_index] = 1;
                }
                else
                {
                    num_of_times_seen[landmark_index]++;

                }

                observed_distances[landmark_index].add(obj.centroid_depth[count]);
                //observed_distances[landmark_index].add(obj.avg_depth[count]);


                if(obj.normalized_centroids[count][1] >= 0.40 && obj.normalized_centroids[count][1] <= 0.60)
                    observed_thetas[landmark_index].add(robot_angle);

                count++;
            }

        }

        public void compute_landmark_coordinates(){
            int count = 0;
            System.out.println("");

            save_to_csv("Landmark label, distance, local angle, x, y,"+ new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
                    "registered_landmark_location.csv");

            while(count < 9){

                if(!landmark_seen[count]|| d_confidence[count] == 0 || t_confidence[count]==0)
                {
                    landmark_mapped[count] = false;
                    count++;
                    continue;

                }

                //coordinate[count][0] =(int) ((mean_distance[count]/100)*Math.cos(Math.toRadians(mean_theta[count])));
                //coordinate[count][1] =(int) ((mean_distance[count]/100)*Math.sin(Math.toRadians(mean_theta[count])));

                coordinate[count][0] =(int) ((mean_distance[count])*Math.cos(Math.toRadians(mean_theta[count])));
                coordinate[count][1] =(int) ((mean_distance[count])*Math.sin(Math.toRadians(mean_theta[count])));

                int rx = 0;
                int ry = 0;

                coordinate[count][0] += rx;
                coordinate[count][1] += ry;

                save_to_csv(landmark_name[count] + "," + mean_distance[count]  + "," + mean_theta[count] + "," + coordinate[count][0] + "," +
                        coordinate[count][1], "registered_landmark_location.csv");

                System.out.printf("\nCoordinate of %s: (%s, %s)",landmark_name[count],coordinate[count][0],coordinate[count][1]);
                count++;
            }

            System.out.printf("\nLandmarks location registered: ");
            System.out.println(java.util.Arrays.toString(landmark_mapped));
        }

        public void compute_robot_coordinate(){

            int A_index = high_distance_confidence_landmarks(1);
            int B_index = high_distance_confidence_landmarks(2);
            int case1_x0,case1_y0,case2_x0,case2_y0;
            System.out.println(A_index);
            System.out.println(B_index);
            double x1 = coordinate[A_index][0];
            double y1 = coordinate[A_index][1];

            double x2 = coordinate[B_index][0];
            double y2 = coordinate[B_index][1];

            //double a = mean_distance[A_index]/100 ;
            //double b = mean_distance[B_index]/100;
            double a = mean_distance[A_index] ;
            double b = mean_distance[B_index];

            System.out.printf("\n\nx1 = %S y1 = %s x2 = %s y2 = %s a = %s b = %s", x1, y1, x2, y2, a, b);

            int counter = 1;
            while(true){

                double c = Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));

                double cos = (a*a + c*c - b*b)/(2*a*c);
                double sin = Math.sqrt(1-cos*cos);

                case1_x0 = (int)Math.round( x1 + (a/c)*(x2-x1)*cos + (a/c)*(y2-y1)*sin);
                case1_y0 = (int)Math.round(y1 + (a/c)*(y2-y1)*cos - (a/c)*(x2-x1)*sin);
                case2_x0 = (int)Math.round( x1 + (a/c)*(x2-x1)*cos - (a/c)*(y2-y1)*sin);
                case2_y0 = (int)Math.round(y1 + (a/c)*(y2-y1)*cos + (a/c)*(x2-x1)*sin);

                //System.out.printf("\nHighest confidence landmarks: %s and %s \nPredicted points: (%s, %s) and (%s, %s)",
                //       landmark_name[A_index], landmark_name[B_index], case1_x0, case1_y0, case2_x0, case2_y0);

                if(case1_x0 == 0 && case1_y0 == 0 && case2_x0 == 0 && case2_y0 == 0)
                {   if(counter%2 == 1 )
                    a++;
                else if(counter %2 == 0)
                    b++;
                }
                else
                    break;
                counter ++;

            }
            // System.out.printf("\n Counter = %s",counter);
            System.out.printf("\nHighest confidence landmarks: %s and %s \nPredicted points: (%s, %s) and (%s, %s)",
                    landmark_name[A_index], landmark_name[B_index], case1_x0, case1_y0, case2_x0, case2_y0);

            int case1_A_theta = get_angle_from_points(case1_x0,case1_y0,(int)x1,(int)y1);
            int case1_B_theta = get_angle_from_points(case1_x0,case1_y0,(int)x2,(int)y2);
            int case2_A_theta = get_angle_from_points(case2_x0,case2_y0,(int)x1,(int)y1);
            int case2_B_theta = get_angle_from_points(case2_x0,case2_y0,(int)x2,(int)y2);

            int measured_A_theta = get_angle_from_robot_angle(mean_theta[A_index]);
            int measured_B_theta = get_angle_from_robot_angle(mean_theta[B_index]);
            int measured_sweep = get_sweep_angle_from_angles(measured_A_theta,measured_B_theta);

            int case1_sweep = get_sweep_angle_from_angles(case1_A_theta,case1_B_theta);
            int case2_sweep = get_sweep_angle_from_angles(case2_A_theta,case2_B_theta);

            if(Math.abs(case1_sweep - measured_sweep) > Math.abs(case2_sweep - measured_sweep)){
                robot_x = case2_x0;
                robot_y = case2_y0;
            }
            else{
                robot_x = case1_x0;
                robot_y = case1_y0;
            }

            /*
           System.out.printf("\nBest landmarks: %s,%s \tdist: %s,%s\trobot coods: %s,%s\n",
                   landmark_name[A_index],
                   landmark_name[B_index],
                   a,b,
                   robot_x,
                   robot_y);
            */

            // Scanner input = new Scanner(System.in);
            //System.out.println("Enter actual x and y");
            int actualx = 4000;
            int actualy = 1600;

            //save_to_csv("time,landmark 1,landmark 2, x1,y1,x2,y2," +
            //        "distance 1,distance 2,angle 1,angle 2,computed robot x,computed robot y,actual x, actual y, error x,error y,"+
            //        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),"robot_localisation.csv");

            save_to_csv( new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())+ ","+ new Date().getTime() + "," +  landmark_name[A_index] + "," + landmark_name[B_index] + "," +
                    coordinate[A_index][0] + "," + coordinate[A_index][1]+ "," + coordinate[B_index][0]+ "," +coordinate[B_index][1]+ "," +
                    mean_distance[A_index] + "," + mean_distance[B_index] + "," +
                    mean_theta[A_index] + "," + mean_theta[B_index] + "," + robot_x + "," + robot_y
                    + "," + actualx + "," + actualy + "," + (actualx - robot_x) + "," + (actualy - robot_y), "robot_localisation.csv");

            System.out.printf("\nRobot coordinates: (%s, %s)",robot_x,robot_y);
        }



        public int get_target_index(String name){

            for(int i = 0; i<9 ;i++)
                if(landmark_name[i].equals(name))
                    return i;
            System.out.println("Error target not in landmarks list");
            return 0;
        }

        public int get_angle_offset(){

            int landmark_index = high_distance_confidence_landmarks(1);
            int angle = get_angle_from_points(robot_x, robot_y, coordinate[landmark_index][0], coordinate[landmark_index][1]);

            int robot_measured_angle = get_angle_from_robot_angle(mean_theta[landmark_index]);

            return robot_measured_angle - angle;
        }

        public int high_angle_confidence_landmarks(int choice){

            double first_val = 0,second_val = 0;
            int first_index = -1,second_index = -1;

            for(int i = 0; i<9 ; i++)
            {   if(t_confidence[i] > first_val)
            { second_val = first_val;
                second_index = first_index;
                first_val = t_confidence[i];
                first_index = i;
            }
            else if (t_confidence[i] > second_val)
            { second_val = t_confidence[i];
                second_index = i;
            }
            }

            if(choice == 1)
                return first_index;
            else                        //choice == 2
                return second_index;

        }

        public int high_distance_confidence_landmarks(int choice){

            double first_val = 0,second_val = 0;
            int first_index = -1,second_index = -1;

            for(int i = 0; i<9 ; i++)
            {   if(d_confidence[i] > first_val && t_confidence[i] > 0)
            { second_val = first_val;
                second_index = first_index;
                first_val = d_confidence[i];
                first_index = i;
            }
            else if (d_confidence[i] > second_val && t_confidence[i] > 0)
            { second_val = d_confidence[i];
                second_index = i;
            }
            }

            if(choice == 1)
                return first_index;
            else                        //choice == 2
                return second_index;

        }

        public int get_angle_from_points(int x_robot,int y_robot,int x_landmark, int y_landmark){

            double slope = (y_landmark - y_robot)*1.0/ (double)(x_landmark - x_robot);

            int angle = (int)Math.toDegrees(Math.atan(slope));

            if(x_landmark >= x_robot){
                if(angle < 0)
                    return 360 + angle;    //fourth quadrant
                else
                    return angle;          //first quadrant
            }
            else{
                if(angle < 0)              //second quadrant
                    return 180 + angle;
                else
                    return 180 + angle;       //third quadrant
            }
        }

        public int get_angle_from_robot_angle(int robot_theta){
            if(robot_theta>= -90)
                return robot_theta + 90;
            else                               //theta between -90 and -180
                return robot_theta + 450;
        }

        public int get_sweep_angle_from_angles(int big_landmark_theta,int small_landmark_theta)
        {
            int current_angle = big_landmark_theta;
            int sweep_angle = 0;
            while(current_angle != small_landmark_theta){
                sweep_angle++;

                if(current_angle == 360 && small_landmark_theta == 0)
                    return sweep_angle;
                else if(current_angle > 360)
                    current_angle = 1;
                else
                    current_angle++;
            }
            return sweep_angle;
        }

        public double get_slope_from_robot_angle(int index){

            int theta;
            if(mean_theta[index]> -90)
                theta =  mean_theta[index]+90;
            else                               //theta between -90 and -180
                theta = mean_theta[index]+450;

            return Math.tan(Math.toRadians(theta));

        }

        public void show_details()
        {
            System.out.printf("\n Landmarks seen: ",Arrays.toString(landmark_seen));

            for(int i = 0; i< 9 ; i++){
                if(landmark_seen[i]){
                    System.out.printf("\nDetails for %s: \t\t\t\t\t mean distance: %s mean theta: %s conf-d: %.2f conf-t: %.2f",
                            landmark_name[i],
                            //Arrays.toString(observed_distances[i].toArray()),
                            //Arrays.toString(observed_thetas[i].toArray()),
                            mean_distance[i],
                            mean_theta[i],
                            d_confidence[i],
                            t_confidence[i]
                    );
                }
            }
        }

        public void set_landmarks_mapped(){

            int count = 0;

            while(count < 9){

                if(landmark_seen[count])
                    landmark_mapped[count] = true;
                count++;
            }

        }

        public void find_mean_and_confidence(){

            for(int i = 0; i<9; i++){
                int tot_dist = 0;
                int tot_theta = 0;
                if(landmark_seen[i] && landmark_mapped[i]){

                    System.out.printf("\n Distances for %s",landmark_name[i]);
                    for(Object x: observed_distances[i]){
                        tot_dist += (Integer)x;
                        if(((Integer)x) == 0){
                            num_of_times_seen[i]--;
                        }
                        System.out.printf("\t %s",(Integer)x);
                    }
                    System.out.println("");

                    mean_distance[i] = tot_dist / num_of_times_seen[i];
                    int count = 0;
                    for(Object x: observed_thetas[i]){
                        tot_theta += (Integer)x;
                        count++;
                    }
                    if(count !=0)
                        mean_theta[i] = tot_theta / count;

                    int tot_temp=0;
                    for(Object x: observed_distances[i])
                        tot_temp += Math.pow(mean_distance[i]-(Integer)x, 2);
                    d_confidence[i] = ((double)num_of_times_seen[i])/ Math.sqrt(((double)tot_temp)/(double)num_of_times_seen[i]);
                    if(Double.isInfinite(d_confidence[i]))
                        d_confidence[i] = 0;

                    tot_temp = 0;
                    for(Object x: observed_thetas[i])
                        tot_temp += Math.pow(mean_theta[i]-(Integer)x, 2);

                    if(count!=0 )
                        t_confidence[i] = count / Math.sqrt(tot_temp/count);
                    if(Double.isInfinite(t_confidence[i]))
                        t_confidence[i] = 0;
                }
            }
        }

        public int get_safe_distance(String name){
            int count = 0;
            while(count<9){
                if(name.equals(landmark_name[count]))
                    return landmark_safe_distance[count];
                count++;
            }
            return 500;
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
    }

}
