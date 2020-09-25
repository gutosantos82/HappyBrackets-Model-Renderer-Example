package examples.models_and_renderers;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.events.KillTrigger;
import net.beadsproject.beads.ugens.Envelope;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.core.instruments.WaveModule;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Boid;
import net.happybrackets.sychronisedmodel.FlockingModel;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import org.json.JSONObject;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;
import java.util.*;


public class LighsAndSoundExample implements HBAction, HBReset {

    final float INITIAL_FREQUENCY = 1000; // this is the frequency of the waveform we will make

    // define the different levels we will be using in our envelope
    final float MIN_VOLUME = 0f;   // this is silence
    final float MAX_VOLUME = 0.1f; // This is the high frequency of the waveform we will make

    // define the times it takes to reach the points in our envelope
    final float RAMP_UP_VOLUME_TIME = 50; // half a second (time is in milliseconds)
    final float HOLD_VOLUME_TIME = 10; // 3 seconds
    final float FADEOUT_TIME = 20; // 10 seconds

    // we will increment this number to get to next note is scale
    final int BASE_TONIC = 48; // This will be the tonic of our scale. This correlates to C2 in MIDI

    static final int PORT = 9001;   // this is silence

    @Override
    public void doReset() {
        RendererController.turnOffLEDs();
        RendererController.disableSerial();
    }

    @Override
    public void action(HB hb) {
        hb.reset();

        // create a wave player to generate a waveform using the FREQUENCY and a Square wave
        WaveModule player = new WaveModule();
        player.setFrequency(INITIAL_FREQUENCY);
        player.setBuffer(Buffer.SQUARE);
        player.setGain(0.0f);

        Envelope gainEnvelope = new Envelope(MIN_VOLUME);
        player.setGain(gainEnvelope);
        //player.connectTo(HB.getAudioOutput());

        WaveModule player2 = new WaveModule();
        player2.setFrequency(INITIAL_FREQUENCY);
        player2.setBuffer(Buffer.SQUARE);
        player2.setGain(0.0f);

        Envelope gainEnvelope2 = new Envelope(MIN_VOLUME);
        player2.setGain(gainEnvelope2);

        int key_note = Pitch.getRelativeMidiNote(BASE_TONIC, Pitch.major, 5);

        final float CLOCK_INTERVAL = 40;

        FlockingModel myModel = new FlockingModel();  //HBSynchronisedModel2
        myModel.setup(this, hb);
        myModel.setup2DSpaceSize(600, 400);
        myModel.setupFlock(10, 0);
        myModel.start();

        r = new Renderer(hb) {
            int count = 0;
            Random rand = new Random();

            // Generate random integers in range 0 to 999
            int red = rand.nextInt(255);
            int green = rand.nextInt(255);
            int blue = rand.nextInt(255);

            private List<WavePlayer> speaker1;
            private List<WavePlayer> speaker2;

            private float[] defaultFreq1;
            private float[] defaultFreq2;

            final int WAVEPLAYER_COUNT = 8;

            class colour {
                int red;
                int green;
                int blue;
                colour(int red, int green, int blue) {
                    this.red = red;
                    this.green = green;
                    this.blue = blue;
                }
            }

            List<colour> boidsColour;

            @Override
            public void setup() {
                speaker1 = new ArrayList<WavePlayer>();
                speaker2  = new ArrayList<WavePlayer>();
                defaultFreq1 = new float[WAVEPLAYER_COUNT];
                defaultFreq2 = new float[WAVEPLAYER_COUNT];
                System.out.println(" hello");
                if(speakers.size() > 0) {
                    //Speaker s = speakers.get(0);
                    for (Speaker s : speakers) {
                    System.out.println(" hi");
                    ((Gain) s.out).setGain(0.1f);
                        for (int i = 0; i < WAVEPLAYER_COUNT; i++) {
                            if(s.id == 0) {
                                defaultFreq1[i] = 100 * i;
                                WavePlayer wp = new WavePlayer(defaultFreq1[i], Buffer.SINE);
                                speaker1.add(wp);
                                s.out.addInput(wp);
                            }
                            if(s.id == 1) {
                                defaultFreq2[i] = 100 * i + 50;
                                WavePlayer wp = new WavePlayer(defaultFreq2[i], Buffer.SINE);
                                speaker2.add(wp);
                                s.out.addInput(wp);
                            }
                        }
                    }
                }

                boidsColour = new ArrayList<>();

                boidsColour.add(0,new colour(255,0,0));
                boidsColour.add(1,new colour(0,255,0));
                boidsColour.add(2,new colour(0,0,255));
                boidsColour.add(3,new colour(255,255,0));
                boidsColour.add(4,new colour(255,0,255));
                boidsColour.add(5,new colour(0,255,255));
                boidsColour.add(6,new colour(255,0,128));
                boidsColour.add(7,new colour(128,0,255));
                boidsColour.add(8,new colour(128,128,255));
                boidsColour.add(9,new colour(255,128,255));

            }

            @Override
            public void renderLight(Light light) {

                //double intensity = myModel.getIntensityAtXY( (int)light.x, (int)light.y);

                double intensity = myModel.getFieldInsensityAtXY( (int)light.x, (int)light.y, 50);

                List<Integer> boidsAroundMe = myModel.getBoidsIdAroundXY((int)light.x, (int)light.y, 10);
                if(boidsAroundMe.size() > 0) {
                    int boidId = boidsAroundMe.get(0);
                    displayColor(light, 12, boidsColour.get(boidId).red, boidsColour.get(boidId).green, boidsColour.get(boidId).blue);
                } else {
                    displayColor(light, 12, 0, 0, 0);
                }
/*
                if(light.id == 0) {
                    if(count > 255) count = 0;
                    //red = rand.nextInt(255);
                    //green = rand.nextInt(255);
                    //blue = rand.nextInt(255);
                    red = 255 - count;
                    green = 0;
                    blue = count;
                    DisplayColor(light, 8, red, green, blue);
                    count++;
                    //System.out.println(" DisplayColor(" + light.id+", 8, "+red+", "+green+", "+blue+"); ");
                }
                if(light.id == 3 || light.id == 1 || light.id == 2) {}
                    DisplayColor(light, 24, red, green, blue);

                //double intensity = model.getFieldInsensityAtXY((int)light.x, (int)light.y, 20);

                double intensity = model.getAverageRangeIntensityAtXY((int)light.x, (int)light.y, 20);

                //System.out.println("LED num: " + light.id + " x: " + light.x + " y: " + light.y + " intensity: " + intensity);

                if(intensity > 0) {
                    DisplayColor(light, 24, red, green, blue);
                    System.out.println("LED: " + light.id + " intensity: " + intensity + " frame: " + model.getFrameCount());
                }*/


/*
                    // create instance of Random class
                Thread thread = new Thread(){
                    public void run(){
*/





/*
                        double framerate = 100;


                        Thread thread = new Thread() {
                            public void run() {

                                try {
                                    Thread.sleep((long) framerate);
                                } catch (InterruptedException e) {
                                    System.out.println(e.getMessage());
                                }
                                DisplayColor(light, 0, 0, 0, 0);

                            }
                        };
                        thread.start();
                        */

/*
                    }
                };
                thread.start();
*/
//                }
            }

            @Override
            public void renderSpeaker(Speaker speaker) {

                //double intensity = myModel.getIntensityAtXY( (int)speaker.x, (int)speaker.y);

                double intensity = myModel.getFieldInsensityAtXY( (int)speaker.x, (int)speaker.y, 10);

                List<Integer> boidsAroundMe = myModel.getBoidsIdAroundXY((int)speaker.x, (int)speaker.y, 10);

                if(boidsAroundMe.size() == 0) {
                    if (speaker.id == 0) {
                        int count = 0;
                        for (WavePlayer wp : speaker1) {
                            wp.setFrequency(defaultFreq1[count]);
                            count++;
                        }
                    }
                    if (speaker.id == 1) {
                        int count = 0;
                        for (WavePlayer wp : speaker2) {
                            wp.setFrequency(defaultFreq2[count]);
                            count++;
                        }
                    }
                } else {
                    hb.setStatus(" Number of boids: "  + boidsAroundMe.size());
                    if (speaker.id == 0) {
                        int count = 0;
                        for (WavePlayer wp : speaker1) {
                            wp.setFrequency(800);
                            count++;
                        }
                    }
                    if (speaker.id == 1) {
                        int count = 0;
                        for (WavePlayer wp : speaker2) {
                            wp.setFrequency(1200);
                            count++;
                        }
                    }
                }

                if(speaker.id == 0 && false) {
                    player.connectTo(speaker.out);
                    player.setGain(gainEnvelope);

                    // convert our MIDI pitch to a frequency
                    player.setMidiFrequency(key_note);
                    // Now start changing the level of gainEnvelope
                    // first add a segment to progress to the higher volume
                    gainEnvelope.addSegment(MAX_VOLUME, RAMP_UP_VOLUME_TIME);

                    // now add a segment to make the gainEnvelope stay at that volume
                    // we do this by setting the start of the segment to the value as our MAX_VOLUME
                    gainEnvelope.addSegment(MAX_VOLUME, HOLD_VOLUME_TIME);

                    //Now make our gain fade out to MIN_VOLUME and then kill it
                    gainEnvelope.addSegment(MIN_VOLUME, FADEOUT_TIME, new KillTrigger(gainEnvelope));
                }
                if(speaker.id == 1 && false) {
                    player2.connectTo(speaker.out);
                    player2.setGain(gainEnvelope2);

                    // convert our MIDI pitch to a frequency
                    player2.setMidiFrequency(key_note+10);
                    // Now start changing the level of gainEnvelope
                    // first add a segment to progress to the higher volume
                    gainEnvelope2.addSegment(MAX_VOLUME, RAMP_UP_VOLUME_TIME);

                    // now add a segment to make the gainEnvelope stay at that volume
                    // we do this by setting the start of the segment to the value as our MAX_VOLUME
                    gainEnvelope2.addSegment(MAX_VOLUME, HOLD_VOLUME_TIME);

                    //Now make our gain fade out to MIN_VOLUME and then kill it
                    gainEnvelope2.addSegment(MIN_VOLUME, FADEOUT_TIME, new KillTrigger(gainEnvelope2));
                }
            }
        };

        RendererController.addRenderer(Renderer.Type.SPEAKER, "cocofofo-Lenovo-Y720-15IKB",9,6, 0,"Speaker-Left", 0);
        RendererController.addRenderer(Renderer.Type.SPEAKER, "augustos-mbp.ad.unsw.edu.au",10.5f,8, 0,"Speaker-Left", 0);
        RendererController.addRenderer(Renderer.Type.SPEAKER, "hb-b827ebbf17a8",10.5f,8, 0,"Speaker-Left", 0);
        RendererController.addRenderer(Renderer.Type.SPEAKER, "hb-b827ebbf17a8",10.5f,8, 0,"Speaker-Right", 1);
        RendererController.addRenderer(Renderer.Type.LIGHT,"hb-b827ebbf17a8",10.5f,8, 0,"Light-Right", 0);

        RendererController.addRenderer(Renderer.Type.LIGHT,"hb-b827ebb507fd",10.5f,8, 0,"Light-Right", 0);
        RendererController.addRenderer(Renderer.Type.SPEAKER, "hb-b827ebb507fd",10.5f,8, 0,"Speaker-Right", 0);
        RendererController.addRenderer(Renderer.Type.SPEAKER, "hb-b827ebb507fd",10.5f,8, 0,"Speaker-Right", 1);

        RendererController.addRenderer(Renderer.Type.LIGHT,"hb-b827ebaac945",9,6, 0,"Light-1", 0);
        RendererController.addRenderer(Renderer.Type.SPEAKER, "hb-b827eb302afa",10.5f,8, 0,"Speaker-Left", 0);

        RendererController.addRenderer(Renderer.Type.SPEAKER,"hb-b827eb999a03",120,200, 0,"Speaker-Left", 0);
        RendererController.addRenderer(Renderer.Type.SPEAKER,"hb-b827eb999a03",460,200, 0,"Speaker-Right", 1);
        RendererController.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,90, 0,"Light-1", 0);
        RendererController.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,310, 0,"Light-2", 1);
        RendererController.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,310, 0,"Light-3", 2);
        RendererController.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,90, 0,"Light-4", 3);

        Clock clock = hb.createClock(CLOCK_INTERVAL).addClockTickListener((offset, this_clock) -> {/* Write your code below this line */
            RendererController.executeRender();
        });

        //clock.start();

        // type booleanControl to generate this code
        BooleanControl stopPlayControl = new BooleanControl(this, "Play", false) {
            @Override
            public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                /* To create this, just type clockTimer */
                if (control_val){
                    RendererController.enableSerial();
                    clock.start();/* End Clock Timer */
                    RendererController.executeRender();
                }
                else {
                    clock.stop();
                    try {
                        Thread.sleep((long) 1000);
                        RendererController.turnOffLEDs();
                        RendererController.disableSerial();
                    } catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                    }
                    HB.getScheduler().reset();
                    // we can reset clocks also with a global message
                    HB.setScheduleTime(0);
                }
                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.GLOBAL);// End DynamicControl stopPlayControl code

        //stopPlayControl.setValue(true);

        // This control will cause device to play a beep and display it's time
        TriggerControl globalTrigger = new TriggerControl(this, "Play Now") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                stopPlayControl.setValue(true);
                // Write your DynamicControl code above this line
            }
        }.setControlScope(ControlScope.GLOBAL).setDisplayType(DynamicControl.DISPLAY_TYPE.DISPLAY_DISABLED);// End DynamicControl globalTrigger code

        new TriggerControl(this, "Play Delay") {
            @Override
            public void triggerEvent() {// Write your DynamicControl code below this line
                double schedulerTime =  (HB.getSchedulerTime());
                double trigger_time = schedulerTime + 1000;
                globalTrigger.send(trigger_time);
                // Write your DynamicControl code above this line
            }
        };// End DynamicControl triggerControl code

        /* type osclistener to create this code */
        OSCUDPListener oscudpListener = new OSCUDPListener(PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                /* type your code below this line */
                // first display the source of message and message name
                String display_val = socketAddress.toString() + ": " + oscMessage.getName();
                String just_val = "";

                for (int i = 0; i < oscMessage.getArgCount(); i++){
                    // add each arg to display message
                    display_val = display_val + " " + oscMessage.getArg(i);
                    just_val = just_val + " " + oscMessage.getArg(i);
                }

                if(oscMessage.getName().equals("/start")) {
                    int value = (int) oscMessage.getArg(0);
                    if(value == 0) {
                        //stopPlayControl.setValue(false);
                    } else {
                        //stopPlayControl.setValue(true);
                    }
                    System.out.println(myModel.getMe() + " start: " + display_val);
                }

                if(oscMessage.getName().equals("/frameState")) {
                    int value = (int) oscMessage.getArg(0);
                    myModel.setFrameState(value);
                    //System.out.println(myModel.getMe() + " frame: " + myModel.getFrameCount());
                }

                if(oscMessage.getName().equals("/fieldState")) {
                    JSONObject fieldState = new JSONObject(just_val);
                    myModel.importModelField(fieldState);
                }

                if(oscMessage.getName().equals("/modelState")) {
                    JSONObject modelState = new JSONObject(just_val);
                    myModel.importModelState(modelState);
                }

                /* type your code above this line */
            }
        };
        if (oscudpListener.getPort() < 0){ //port less than zero is an error
            String error_message =  oscudpListener.getLastError();
            System.out.println(myModel.getMe() + " says: Error opening port " + PORT + " " + error_message);
        } else {
            System.out.println(myModel.getMe() + " says: Success opening port " + PORT);
        }
        /** end oscListener code */
    }

    //<editor-fold defaultstate="collapsed" desc="Debug Start">
    /**
     * This function is used when running sketch in IntelliJ IDE for debugging or testing
     *
     * @param args standard args required
     */
    public static void main(String[] args) {

        try {
            HB.runDebug(MethodHandles.lookup().lookupClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //</editor-fold>

}
