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
  color branchColor;
  color lastBranchColor;
  float branchAlpha = 0;
  float lastBranchAlpha = 0;
  color newColor;
  int colorType = 3;
  int minBranchLen = 15;
  float randomLerpVal;

  float leafRadius;
  color leafColor = color(random(200, 255), 0, random(100, 150), 200); // pinkish
  float randomVal = random(1);


  color[][] colors = {
    {#5678ff, #ff8a5b, #b7c8ff, #ff89eb, #ff7792, #ffa8d5, #8b1eff, #baffff, #ff327a, #5678ff}, // 0 : pinksish
    {#FF9500, #FF0000, #FF00F3, #AA00FF, #002EFF}, // 1: psy
    {#3eb1cb, #007fa3, #086b8a, #054458, #001e3b}, // 2: blue
    {#eef5fa, #cbe1ea, #98c0ce, #719cad, #496370}, // 3: Ice
    { #94562b, #e18c22, #ff6700, #d65555, #ffdd11}, // 4: Fall
    {#f9a011, #db8528, #bd7e39, #8c6a45, #695540}, // 5: Fall 2
    {#1bdbc2, #9270d4, #ee29e6, #3115e1, #b713d6}, // 6: purple ish
    {#e5a7af, #eebaa7, #f2cea0, #f9e29a, #fff68f}, // 7: sutle flame
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

    randomLerpVal = random(0.005, 0.01);

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
  void jitter() {
    end.x += random(-0.1, 0.1);
    end.y += random(-0.1, 0.1);
  }

  /**
   * displays the Branch
   */
  void show() {    
    strokeWeight(map(len, height/4, 3, 20, 1));
    //branchAlpha = lerp(lastBranchAlpha, 255, 0.1);
    //lastBranchAlpha = branchAlpha;    
    //stroke(branchColor, branchAlpha);
    branchColor = lerpColor(lastBranchColor, newColor, 0.1);
    lastBranchColor = branchColor;
    stroke(branchColor);
    // position
    currentPos.lerp(end, randomLerpVal);
    line(begin.x, begin.y, currentPos.x, currentPos.y);

    if (stopGrowing() && randomVal > 0.2) {
      drawLeaf();
    }
  }

  void drawLeaf() {
    noStroke();
    fill(leafColor);
    ellipse(currentPos.x, currentPos.y, leafRadius, leafRadius);
  }

  /**
   * generates a new Branch for the right-side
   */
  Branch branch(float angle_) {
    PVector dir = PVector.sub(end, begin);        
    //strokeWeight(map(len, height/4, 3, 20, 1));
    dir.rotate(angle_);
    dir.mult(random(0.6, 0.8));
    len = (int)dir.mag();
    strokeWeight(map(dir.mag(), height/4, 3, 20, 1));
    PVector newEnd = PVector.add(end, dir);
    Branch b = new Branch(end, newEnd);
    return b;
  }

  PVector getPos() {
    return end;
  }

  int getLength() {
    return len;
  }

  void changeColor() {
    newColor = colors[colorType][(int)random(colors[colorType].length)];
  }

  boolean isLerpFinished() {
    float dist = abs(PVector.dist(end, currentPos));
    if (dist < 0.2) {
      return true;
    } else {
      return false;
    }
  }

  boolean stopGrowing() {
    if (len < minBranchLen) {
      return true;
    } else {
      return false;
    }
  }
}
