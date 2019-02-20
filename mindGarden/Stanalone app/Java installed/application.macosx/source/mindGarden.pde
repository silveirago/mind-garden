/**
 * makes a FractalTree using Array(Lists)
 * adds one Level per MouseClick
 * at Level 6 Leaves will fall down
 * @author Lukas Klassen
 * translated version of CC_15_FractalTreeArray by Daniel Shiffmann
 */

import java.util.*;
import themidibus.*; //Import the library
MidiBus myBus; // The MidiBus
/**
 * global reference to Trees, Leaves and the amount of Levels
 */
ArrayList<ArrayList<Branch>> trees = new ArrayList<ArrayList<Branch>>();
ArrayList<PVector> leaves = new ArrayList<PVector>();
int numOfTrees = 4;
int count = 0;

int brachThickness = 2;
int minThickness = 1;
int branchLen = 10;
int minAngle = 20;
int maxAngle = 45;
int minLen, maxLen;
List<Integer> leafColor = new ArrayList<Integer>(); // colors of the leaves
List<Integer> branchColor = new ArrayList<Integer>(); // colors of the leaves

// Leaves
PShape greenLeaf;

// Background

PGraphics bg; 

//color treeColor = color(41, 25, 3);
color treeColor = color(104, 46, 14);
color bgColor = color(255, 178, 187); // pink
//color bgColor = color(237, 239, 252); // brown
color lightColor = color(255, 213, 147);
//color lightColor = color(244, 245, 247);
color darkColor = color(70, 83, 117);
//color darkColor = color(50, 58, 66);

// MIDI
String[] outputs;
int channel = 0;
float xoff = 0;

/**
 * sets the Tree up
 */
void setup() {
  //size(820, 462);
  background(#e2edf1);
  prepareExitHandler();
  fullScreen();
  noCursor();
  smooth();
  delay(2000);
  //MIDI

  // List all available Midi devices on STDOUT. This will show each device's index and name.
  //MidiBus.list();   
  //myBus = new MidiBus(this, 0, 1); // Create a new MidiBus using the device index to select the Midi input and output devices respectively.
  //myBus = new MidiBus(this, -1, "IAC Bus 1");
  myBus = new MidiBus(this, -1, 1); // sets MIDI port
  //String busName = myBus.getBusName();
  myBus.setBusName("Pendulum Sequencer Bus");
  //println(myBus.getBusName());  
  outputs = MidiBus.availableOutputs(); // stores the MIDI ports

  // TREE
  float treeOffset = width / numOfTrees;
  minLen = height/8;
  maxLen = height/2;

  for (int i=0; i<numOfTrees; i++) {
    //leafColoreate root-Branch
    //float tempPos = random(width/2 - (width/3),  width/2 + (width/3));
    branchLen = (int)(random(minLen, maxLen) * 0.6);
    float tempPos = ((treeOffset * (i+1)) - treeOffset/2) + random(-30, 30);
    //float tempPos = width/2;
    PVector a = new PVector(tempPos, height);
    PVector b = new PVector(tempPos, height - branchLen);
    Branch root = new Branch(a, b); 

    trees.add(new ArrayList<Branch>());
    trees.get(i).add(root);
  }


  branchColor.add(color(random(200, 255), random(10), random(100, 255)));

  //bg = createGraphics(width, height);
  //generateSky(bg);
  //tint(80);
  //image(bg, 0, 0);
}

/**
 * Displays the Tree
 */
void draw() {
  //background(bg);
  //background(#f4fbff);
  background(#e2edf1);
  //blendMode(ADD);

  growTrees();

  for (int t=0; t<trees.size(); t++) {
    tint(255, 30*t);
    //forEach Branch of the Tree: Draw it
    for (int b = 0; b < trees.get(t).size(); b++) {   
      //tree.get(i).jitter();    
      trees.get(t).get(b).show();
    }
  }

  //saveFrame("line-######.png");
}


/**
 * adds another Layer every time the Mouse is pressed
 */

void growTrees() {

  for (int t=0; t<trees.size(); t++) {
    for (int b = trees.get(t).size() -1; b >= 0; b--) {

      if (trees.get(t).get(b).isLerpFinished() && !trees.get(t).get(b).stopGrowing()) {

        Branch current = trees.get(t).get(b);
        //if the current Branch has no children: add them
        if (!current.finished) {
          //thickness.add(0, brachThickness);
          if (random(1) > 0.1) { 
            trees.get(t).add(current.branch(radians(random(minAngle, maxAngle))) );
          }
          if (random(1) > 0.1) {
            trees.get(t).add(current.branch(radians(random(-minAngle, -maxAngle))) );
          }
          if (random(1) > 0.5) {
            trees.get(t).add(current.branch(radians(random(-10, 10))) );
          }
        }

        //now that Branch has children
        current.finished = true;
      }
    }
  }

  //new Level added
  count ++;
}

void mousePressed() {
}


void generateSky(PGraphics pg)
{
  pg.beginDraw();

  pg.background(bgColor);

  for (int y = 0; y < height; y += 2)
  {
    for (int x = 0; x < width; x += 2)
    {
      //draw clouds
      float n = noise(x/200., y/50.);     
      pg.noStroke();
      pg.fill(darkColor, n*map(y, 0, 2*height/3., 255, 0)); 
      pg.ellipse(x, y, 3, 3);
    }

    //draw the light on the bottom
    strokeWeight(3);
    pg.stroke(lightColor, map(y, 2*height/3, height, 0, 255));
    pg.line(0, y, width, y);
  }

  pg.endDraw();
}

void keyPressed() {
  if (key == 's') {
    saveFrame("line-######.png");
  }
  if (key == 'c') {
    for (int t=0; t<trees.size(); t++) {
      for (int b = 0; b < trees.get(t).size(); b++) {
        trees.get(t).get(b).changeColor();
      }
    }
  }
}

// STOP
// must add "prepareExitHandler();" in setup() for Processing sketches 
private void prepareExitHandler () {
  Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
    public void run () {
      //System.out.println("SHUTDOWN HOOK");
      myBus = new MidiBus(this, -1, 1); // sets MIDI port
      for (int ch=0; ch<16; ch++) {  // sets all midi notes off when app shuts down
        for (int n=0; n<128; n++) {
          myBus.sendNoteOn(ch, n, 0);
        }
      }
      try {
        stop();
      } 
      catch (Exception ex) {
        ex.printStackTrace(); // not much else to do at this point
      }
    }
  }
  ));
}  
