import wblut.isogrid.*;
WB_IsoSystem6 iso;

void setup() {
  size(800, 800, P3D);
  smooth(8);
  //scale, I, J, K, center x, center y, colors, random seed, PApplet
  iso=new WB_IsoSystem6(4, 85, 47, 103, width/2, height/2, new int[]{color(255, 0, 255), color(255, 255, 0), color(0, 255, 255)}, (int)random(1000000), this);
  iso.subdivide(0.50, 16, 32, 16);
  iso.sliceJAll(8, 4);
  iso.wallAll();
  
  //chance, block J, block K
  iso.openIBlocks(0.5,16,16);  
  //chance, block I, block K
  iso.openJBlocks(0.5,8,16);  
  //chance, block I, block J
  iso.openKBlocks(0.5,16,8);  
}

void draw() {
  background(255);
  iso.centerGrid();
  stroke(0);
  strokeWeight(2);
  iso.drawOutlines();
  strokeWeight(1);
  iso.drawLines();
}
