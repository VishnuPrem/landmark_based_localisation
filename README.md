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

<p align="center"> 
<img src="/img/img3.jpg" width = "400"/>
</p> 
 
To identify the landmarks in the room an object detection model was used (SSD MobileNet). The model forms a bounding box around the items identified. 

<p align="center"> 
<img src="/img/img4.jpg" width = "400"/>
</p> 

The model was retrained to identify 2 new classes, a star and a charge point symbol. Several images were taken of the the two classes in various environments and lighting conditions to form a good training set.

[![Obstacle avoidance](/img/img5.jpg)](https://www.youtube.com/watch?v=gFwmu38nINA)
