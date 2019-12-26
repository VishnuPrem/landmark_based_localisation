# Landmark based localisation using RGBD sensor

A mobile robot was programmed to navigate and localise in an indoor envoronment using RGBD sensor data, an Object recognition model and sonar sensors. 

<p align="center"> 
<img src="/img/img1.jpg" height = "200" />
</p> 
The software was implemented on a Pioneer P3DX mobile robot platform equiped with an ASUS Xtion Pro RGBD sensor shown above

<p align="center"> 
<img src="/img/img2.jpg" width = "400"/>
</p> 
The platform was tested in the Living Lab at the University of Salford

## Object Detection

<p align="center"> 
<img src="/img/img3.jpg" width = "400"/>
</p> 
 
To identify the landmarks in the room an object detection model was used (SSD MobileNet). The model forms a bounding box around the items identified. By computing the centroid of the bounding box, the position and type of landmark could be identified

<p align="center"> 
<img src="/img/img4.jpg" width = "400"/>
</p> 

The model was retrained to identify 2 new classes, a star and a charge point symbol. Several images were taken of the the two classes in various environments and lighting conditions to form a good training set.

By using the object detection model, once the position of an identified landmark was found, this information could be used to determine the distance of the obstacle from the robot by finding the value of the corresponding pixel in the depth image obtained from the depth camera. The working is shown below.

<p align="center"> 
<img src="/img/gif1.gif" height = "200"/>
</p>

## Obstacle Avoidance

<p align="center">
<a href="https://www.youtube.com/watch?v=gFwmu38nINA/">
         <img alt="Obstacle avoidance" src="/img/img5.jpg" width="400" >
</a>
</p>
 
 ## Localisation
 
 
<p align="center"> 
<img src="/img/img6.jpg" width = "400"/>
</p>
For localising, two landmarks having known location in the room need to be identified and the distance from the robot calculated

<p align="center"> 
<img src="/img/img7.jpg" width = "400"/>
</p> 

The candidate locations of the robot are computed geometrically

<p align="center"> 
<img src="/img/img8.jpg" width = "400"/>
</p> 
Based on the amount of rotation between the two landmarks, the actual location of the robot is computed

## Navigation
<p align="center">
<a href="https://www.youtube.com/watch?v=EVmXJM0tmb4">
         <img alt="Navigation" src="/img/img9.jpg" width="400" >
</a>
</p>

