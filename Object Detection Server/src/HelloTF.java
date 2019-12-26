import org.tensorflow.Graph;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;

public class HelloTF {

    static double robot_x,robot_y;

    public static void main(String[] args) {
       findpoint();
        //System.out.println(Math.toDegrees(Math.atan(1)));
        //System.out.println(get_angle_from_points(0, 0, 0, -5));
        //System.out.println(get_sweep_angle_from_angles(90, 80));
    }

    public static void findpoint() {



        int case1_x0,case1_y0,case2_x0,case2_y0;

        double x1 = 1500;
        double y1 = 1300;


        double x2 = 2200;
        double y2 = 4100;


        double a = 2108 ;
        double b = 1118;
        int mean_thetaA = 117;
        int mean_thetaB =33;

        int counter=1;
        while(true){

            double c = Math.sqrt(Math.pow((x1-x2),2) + Math.pow((y1-y2),2));

            double cos = (a*a + c*c - b*b)/(2*a*c);
            double sin = Math.sqrt(1-cos*cos);

            case1_x0 = (int)Math.round( x1 + (a/c)*(x2-x1)*cos + (a/c)*(y2-y1)*sin);
            case1_y0 = (int)Math.round(y1 + (a/c)*(y2-y1)*cos - (a/c)*(x2-x1)*sin);
            case2_x0 = (int)Math.round( x1 + (a/c)*(x2-x1)*cos - (a/c)*(y2-y1)*sin);
            case2_y0 = (int)Math.round(y1 + (a/c)*(y2-y1)*cos + (a/c)*(x2-x1)*sin);

            if(case1_x0 == 0 && case1_y0 == 0 && case2_x0 == 0 && case2_y0 == 0)
            {   if(counter%2 == 1 )
                a++;
                else if(counter %2 == 0)
                b++;
            }
            else
                break;

            counter++;
        }
        System.out.printf("points: %s, %s\t%s, %s\n",case1_x0,case1_y0,case2_x0,case2_y0);

        int case1_A_theta = get_angle_from_points(case1_x0,case1_y0,(int)x1,(int)y1);
        int case1_B_theta = get_angle_from_points(case1_x0,case1_y0,(int)x2,(int)y2);
        int case2_A_theta = get_angle_from_points(case2_x0,case2_y0,(int)x1,(int)y1);
        int case2_B_theta = get_angle_from_points(case2_x0,case2_y0,(int)x2,(int)y2);

        System.out.printf("case 1: %s,%s case 2: %s,%s\n",case1_A_theta,case1_B_theta,case2_A_theta,case2_B_theta);

        int measured_A_theta = get_angle_from_robot_angle(mean_thetaA);
        int measured_B_theta = get_angle_from_robot_angle(mean_thetaB);

        System.out.printf("robot sees landmarks at %s and %s\n", measured_A_theta, measured_B_theta);

        int measured_sweep = get_sweep_angle_from_angles(measured_A_theta,measured_B_theta);

        //System.out.println(measured_sweep);
        int case1_sweep = get_sweep_angle_from_angles(case1_A_theta,case1_B_theta);
        int case2_sweep = get_sweep_angle_from_angles(case2_A_theta,case2_B_theta);

        System.out.println(a);
        if(Math.abs(case1_sweep - measured_sweep) > Math.abs(case2_sweep - measured_sweep)){
            robot_x = case2_x0;
            robot_y = case2_y0;
        }
        else{
            robot_x = case1_x0;
            robot_y = case1_y0;
        }


        System.out.printf("dist: %s,%s\trobot coods: %s,%s",
                a,b,
                robot_x,
                robot_y);


    }


    public static int get_angle_from_points(int x_robbot,int y_robot,int x_landmark, int y_landmark){

        double slope = (y_landmark-y_robot)*1.0/ (double)(x_landmark-x_robbot);

        int angle = (int)Math.toDegrees(Math.atan(slope));

        //System.out.printf("Slope %s\n",slope);
        //System.out.printf("Angle: %s\n",angle);

        if(x_landmark >= x_robbot){
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

    public static int get_angle_from_robot_angle(int robot_theta){
        if(robot_theta> -90)
            return robot_theta + 90;
        else                               //theta between -90 and -180
            return robot_theta + 450;
    }

    public static int get_sweep_angle_from_angles(int big_landmark_theta,int small_landmark_theta)
    {
        int current_angle = big_landmark_theta;
        int sweep_angle = 0;
        while(current_angle != small_landmark_theta){
            sweep_angle++;

            if(current_angle == 360 && small_landmark_theta == 0)
                return sweep_angle;
            else if(current_angle == 360)
                current_angle = 1;
            else
                current_angle++;
        }
        return sweep_angle;
    }



    public static void solve_linear_eqn(){


        double a = 1;
        double b = -1;
        double e = 0;

        double c = 0;
        double d = 1;
        double f = 5;

        double det = a*d - b*c;
        double x = (d*e - b*f)/det;
        double y = (a*f - c*e)/det;

        System.out.printf("x=%s \ty=%s", x,y);


    }
}
