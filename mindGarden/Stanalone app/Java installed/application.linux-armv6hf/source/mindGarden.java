import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.*; 
import themidibus.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class mindGarden extends PApplet {

/**
 * makes a FractalTree using Array(Lists)
 * adds one Level per MouseClick
 * at Level 6 Leaves will fall down
 * @author Lukas Klassen
 * translated version of CC_15_FractalTreeArray by Daniel Shiffmann
 */


 //Import the library
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
int treeColor = color(104, 46, 14);
int bgColor = color(255, 178, 187); // pink
//color bgColor = color(237, 239, 252); // brown
int lightColor = color(255, 213, 147);
//color lightColor = color(244, 245, 247);
int darkColor = color(70, 83, 117);
//color darkColor = color(50, 58, 66);

// MIDI
String[] outputs;
int channel = 0;
float xoff = 0;

/**
 * sets the Tree up
 */
public void setup() {
  //size(820, 462);
  background(0xffe2edf1);
  prepareExitHandler();
  
  noCursor();
  
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
    branchLen = (int)(random(minLen, maxLen) * 0.6f);
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
public void draw() {
  //background(bg);
  //background(#f4fbff);
  background(0xffe2edf1);
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

public void growTrees() {

  for (int t=0; t<trees.size(); t++) {
    for (int b = trees.get(t).size() -1; b >= 0; b--) {

      if (trees.get(t).get(b).isLerpFinished() && !trees.get(t).get(b).stopGrowing()) {

        Branch current = trees.get(t).get(b);
        //if the current Branch has no children: add them
        if (!current.finished) {
          //thickness.add(0, brachThickness);
          if (random(1) > 0.1f) { 
            trees.get(t).add(current.branch(radians(random(minAngle, maxAngle))) );
          }
          if (random(1) > 0.1f) {
            trees.get(t).add(current.branch(radians(random(-minAngle, -maxAngle))) );
          }
          if (random(1) > 0.5f) {
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

public void mousePressed() {
}


public void generateSky(PGraphics pg)
{
  pg.beginDraw();

  pg.background(bgColor);

  for (int y = 0; y < height; y += 2)
  {
    for (int x = 0; x < width; x += 2)
    {
      //draw clouds
      float n = noise(x/200.f, y/50.f);     
      pg.noStroke();
      pg.fill(darkColor, n*map(y, 0, 2*height/3.f, 255, 0)); 
      pg.ellipse(x, y, 3, 3);
    }

    //draw the light on the bottom
    strokeWeight(3);
    pg.stroke(lightColor, map(y, 2*height/3, height, 0, 255));
    pg.line(0, y, width, y);
  }

  pg.endDraw();
}

public void keyPressed() {
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
/**
 * representing a Branch of the FractalTree
 * @author Lukas Klassen
 */
class Branch {

  /**
   * Start and end Point of the Branch
   */
  public PVector begin;
  public PVector end;
  PVector lastPos;
  PVector currentPos;
  int len;
  public boolean finished = false;
  int branchColor;
  int lastBranchColor;
  float branchAlpha = 0;
  float lastBranchAlpha = 0;
  int newColor;
  int colorType = 3;
  int minBranchLen = 15;
  float randomLerpVal;

  float leafRadius;
  int leafColor = color(random(200, 255), 0, random(100, 150), 200); // pinkish
  float randomVal = random(1);


  int[][] colors = {
    {0xff5678ff, 0xffff8a5b, 0xffb7c8ff, 0xffff89eb, 0xffff7792, 0xffffa8d5, 0xff8b1eff, 0xffbaffff, 0xffff327a, 0xff5678ff}, // 0 : pinksish
    {0xffFF9500, 0xffFF0000, 0xffFF00F3, 0xffAA00FF, 0xff002EFF}, // 1: psy
    {0xff3eb1cb, 0xff007fa3, 0xff086b8a, 0xff054458, 0xff001e3b}, // 2: blue
    {0xffeef5fa, 0xffcbe1ea, 0xff98c0ce, 0xff719cad, 0xff496370}, // 3: Ice
    { 0xff94562b, 0xffe18c22, 0xffff6700, 0xffd65555, 0xffffdd11}, // 4: Fall
    {0xfff9a011, 0xffdb8528, 0xffbd7e39, 0xff8c6a45, 0xff695540}, // 5: Fall 2
    {0xff1bdbc2, 0xff9270d4, 0xffee29e6, 0xff3115e1, 0xffb713d6}, // 6: purple ish
    {0xffe5a7af, 0xffeebaa7, 0xfff2cea0, 0xfff9e29a, 0xfffff68f}, // 7: sutle flame
  };

  // MIDI
  String[] noteName = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"}; 
  int velocity = 127;
  //int channel = 0;
  int noteIndex = 0;
  float pitch = 0;
  int octave = 12 * 3;
  int whichScale = 13;

  /**
   * create Branch with beginning and end-Point 
   * @param beging Startpoint
   * @param end Endpoint
   */
  Branch(PVector begin, PVector end) {
    this.begin = begin;
    this.end = end;
    currentPos = new PVector();
    currentPos = begin.copy();
    lastPos = this.begin;

    PVector dir = PVector.sub(end, begin);
    len = (int)dir.mag();
    strokeWeight(map(dir.mag(), height/4, 3, 20, 1));
    branchColor = colors[colorType][(int)random(colors[colorType].length)];
    newColor = branchColor;
    lastBranchColor = branchColor;

    randomLerpVal = random(0.005f, 0.01f);

    //LEAF
    leafRadius = random(8, 12);

    // MIDI
    velocity = (int)random(30, 90);
    float noiseVal = noise(xoff);
    //pitch = scales[whichScale][(int)random(scales[whichScale].length)];
    pitch = scales[whichScale][(int)(noiseVal*(scales[whichScale].length))];
    myBus.sendNoteOn(channel, (int)pitch + octave, velocity);
    channel++;
    channel%=5;
    xoff++;
    //if (xoff > 0.9) {
    //  xoff = 0.9;
    //}
    println(noiseVal);
    //println( + " " + scales[whichScale].length + " " + (xoff * scales[whichScale].length));
  }

  /**
   * jitters the Branch (randomly change the end-Point a bit)
   */
  public void jitter() {
    end.x += random(-0.1f, 0.1f);
    end.y += random(-0.1f, 0.1f);
  }

  /**
   * displays the Branch
   */
  public void show() {    
    strokeWeight(map(len, height/4, 3, 20, 1));
    //branchAlpha = lerp(lastBranchAlpha, 255, 0.1);
    //lastBranchAlpha = branchAlpha;    
    //stroke(branchColor, branchAlpha);
    branchColor = lerpColor(lastBranchColor, newColor, 0.1f);
    lastBranchColor = branchColor;
    stroke(branchColor);
    // position
    currentPos.lerp(end, randomLerpVal);
    line(begin.x, begin.y, currentPos.x, currentPos.y);

    if (stopGrowing() && randomVal > 0.2f) {
      drawLeaf();
    }
  }

  public void drawLeaf() {
    noStroke();
    fill(leafColor);
    ellipse(currentPos.x, currentPos.y, leafRadius, leafRadius);
  }

  /**
   * generates a new Branch for the right-side
   */
  public Branch branch(float angle_) {
    PVector dir = PVector.sub(end, begin);        
    //strokeWeight(map(len, height/4, 3, 20, 1));
    dir.rotate(angle_);
    dir.mult(random(0.6f, 0.8f));
    len = (int)dir.mag();
    strokeWeight(map(dir.mag(), height/4, 3, 20, 1));
    PVector newEnd = PVector.add(end, dir);
    Branch b = new Branch(end, newEnd);
    return b;
  }

  public PVector getPos() {
    return end;
  }

  public int getLength() {
    return len;
  }

  public void changeColor() {
    newColor = colors[colorType][(int)random(colors[colorType].length)];
  }

  public boolean isLerpFinished() {
    float dist = abs(PVector.dist(end, currentPos));
    if (dist < 0.2f) {
      return true;
    } else {
      return false;
    }
  }

  public boolean stopGrowing() {
    if (len < minBranchLen) {
      return true;
    } else {
      return false;
    }
  }
}
float scales[][] = {
 {0, 1, 2, 3, 4, 5,  6,  7,  8,  9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23}, // 0: chromatic
 {0, 2, 4, 5, 7, 9, 11, 12, 14, 16, 17, 19, 21, 23, 24, 26, 28, 29, 31, 33, 35, 36, 38, 40}, // 1: ionian
 {0, 2, 3, 5, 7, 9, 10, 12, 14, 15, 17, 19, 21, 22, 24, 26, 27, 29, 31, 33, 34, 36, 38, 39}, // 2: dorian
 {0, 1, 3, 5, 7, 8, 10, 12, 13, 15, 17, 19, 20, 22, 24, 25, 27, 29, 31, 32, 34, 36, 37, 39}, // 3: phrygian
 {0, 2, 4, 6, 7, 9, 11, 12, 14, 16, 18, 19, 21, 23, 24, 26, 28, 30, 31, 33, 35, 36, 38, 40}, // 4: lydian
 {0, 2, 4, 5, 7, 9, 10, 12, 14, 16, 17, 19, 21, 22, 24, 26, 28, 29, 31, 33, 34, 36, 38, 40}, // 5: mixolydian
 {0, 2, 3, 5, 7, 8, 10, 12, 14, 15, 17, 19, 20, 22, 24, 26, 27, 29, 31, 32, 34, 36, 38, 39}, // 6: aeolian
 {0, 1, 3, 5, 6, 8, 10, 12, 13, 15, 17, 18, 20, 22, 24, 25, 27, 29, 30, 32, 34, 36, 37, 39}, // 7: locrian
 {0, 2, 4, 6, 8, 10,12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36, 38, 40, 42, 44, 46}, // 8: wholetone
 {0, 3, 7,10,14, 17,21, 24, 27, 31, 34, 38, 41, 45, 48, 51, 55, 58, 62, 65, 69, 72, 77, 80}, // 9: m7 9 11 13
 {0, 3, 6, 9,12, 15,18, 21, 24, 27, 30, 33, 36, 39, 42, 45, 48, 51, 54, 57, 60, 63, 66, 69}, //10: dim7
 {0, 2, 3, 5, 6, 8,  9, 11, 12, 14, 15, 17, 18, 20, 21, 23, 24, 26, 27, 29, 30, 32, 33, 35}, //11: octatonic 2-1
 {0, 1, 3, 4, 6, 7,  9, 10, 12, 13, 15, 16, 18, 19, 21, 22, 24, 25, 27, 28, 30, 31, 33, 34}, //12: octatonic 1-2
 {0, 2, 4, 7, 9,12, 14, 16, 19, 21, 24, 26, 28, 31, 33, 36, 38, 40, 43, 45, 48, 50, 52, 55}, //13: major pentatonic
 {0, 3, 5, 7,10,12, 15, 17, 19, 22, 24, 27, 29, 31, 34, 36, 39, 41, 43, 46, 48, 51, 53, 55}, //14: minor pentatonic 
};
//String[] scaleNames = {"chromatic", "ionian", "dorian", "phrygian", "lydian", "mixolydian", 
//"aeolian", "locrian", "wholetone", "m7 9 11 13", "dim7", "octatonic 2-1", 
//"octatonic 1-2", "major", "pentatonic", "minor pentatonic"};

/*

0, 0 1 2 3 4 5 6  7  8  9  10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50;
1, 0 2 4 5 7 9 11 12 14 16 17 19 21 23 24 26 28 29 31 33 35 36 38 40 41 43 45 47 48 50 52 53 55 57 59 60;
2, 0 2 3 5 7 9 10 12 14 15 17 19 21 22 24 26 27 29 31 33 34 36 38 39 41 43 45 46 48 50 51 53 55 57 58 60 62;
3, 0 1 3 5 7 8 10 12 13 15 17 19 20 22 24 25 27 29 31 32 34 36 37 39 41 43 44 46 48 49 51 53 55 56 58 60 61 63;
4, 0 2 4 6 7 9 11 12 14 16 18 19 21 23 24 26 28 30 31 33 35 36 38 40 42 43 45 47 48 50 52 54 55 57 59 60 62 64;
5, 0 2 4 5 7 9 10 12 14 16 17 19 21 22 24 26 28 29 31 33 34 36 38 40 41 43 45 46 48 50 52 53 55 57 58 60 62 64;
6, 0 2 3 5 7 8 10 12 14 15 17 19 20 22 24 26 27 29 31 32 34 36 38 39 41 43 44 46 48 50 51 53 55 56 58 60 62 63;
7, 0 1 3 5 6 8 10 12 13 15 17 18 20 22 24 25 27 29 30 32 34 36 37 39 41 42 44 46 48 49 51 53 54 56 58 60 61 63;
8,  0 2 4  6  8 10 12 14 16 18 20 22 24 26 28 30 32 34 36 38 40 42 44 46 48 50 52 54 56 58 60 62 64 66 68 70 72 74;
9,  0 3 7 10 14 17 21 24 27 31 34 38 41 45 48 51 55 58 62;
10, 0 3 6 9  12 15 18 21 24 27 30 33 36 39 42 45 48 51 54 57 60 63 66 69 72 75 78 81 84 87 90;
11, 0 2 3 5 6 8 9 11 12 14 15 17 18 20 21 23 24 26 27 29 30 32 33 35 36 38 39 41 42 44 45 47 48 50 51 53 54 56 57 59 60 62 63 65 66 68 69 71 72;
12, 0 1 3 4 6  7  9 10 12 13 15 16 18 19 21 22 24 25 27 28 30 31 33 34 36 37 39 40 42 43 45 46 48 49 51 52 54 55 57 58 60 61 63 64;
13, 0 2 4 7 9  12 14 16 19 21 24 26 28 31 33 36 38 40 43 45 48 50 52 55 57 60;
14, 0 3 5 7 10 12 15 17 19 22 24 27 29 31 34 36 39 41 43 46 48 51 53 55 58 60;
15, 0 12 24 36 48;

*/
  public void settings() {  fullScreen();  smooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "mindGarden" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
