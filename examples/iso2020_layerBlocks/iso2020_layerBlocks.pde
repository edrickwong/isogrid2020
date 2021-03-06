import wblut.isogrid.*;
WB_IsoSystem6 iso;

void setup() {
  size(800, 800, P3D);
  smooth(8);
  //scale, I, J, K, center x, center y, colors, random seed,  PApplet
  iso=new WB_IsoSystem6(4, 53, 117, 64, width/2, height/2, new int[]{color(255, 0, 255), color(255, 255, 0), color(0, 255, 255)}, (int)random(1000000), this);


  iso.invertAll();
  //chance, off, on, block I, block J, block K
  iso.layerIBlocks(0.5,16,2,16,16,16);
  iso.layerJBlocks(0.5,8,2,8,8,16);
  iso.layerKBlocks(0.5,16,4,16,32,8);
  
  
}

void draw() {
  background(255);
  iso.centerGrid();
  stroke(0);
  strokeWeight(2);
  iso.drawOutlines();
  strokeWeight(1);
  iso.drawLines();
  noStroke();
  //iso.drawTriangles();
}
