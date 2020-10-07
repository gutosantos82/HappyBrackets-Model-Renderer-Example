package the_mind_at_work.sketches;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.core.control.BooleanControl;
import net.happybrackets.core.control.ControlScope;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.TriggerControl;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.*;
import org.json.JSONObject;
import the_mind_at_work.renderers.LightAndSoundFlockingRenderer;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;
import java.util.*;


public class SimpleFlockingExample implements HBAction, HBReset {

    RendererController rc = RendererController.getInstance();

    @Override
    public void doReset() {
        rc.turnOffLEDs();
        rc.disableSerial();
    }

    @Override
    public void action(HB hb) {
        hb.reset();
        rc.reset();
        rc.setHB(hb);

        final int PORT = 9001;   // this is silence

        rc.setRendererClass(LightAndSoundFlockingRenderer.class);

        // Add your simulator to this list test the code locally.
        rc.addRenderer(Renderer.Type.SPEAKER, "hb-b827eb999a03",120,200, 0,"Speaker-Left", 0);
        rc.addRenderer(Renderer.Type.SPEAKER,"hb-b827eb999a03",460,200, 0,"Speaker-Right", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,90, 0,"Light-1", 0);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,310, 0,"Light-2", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,310, 0,"Light-3", 2);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,90, 0,"Light-4", 3);
        rc.addRenderer(Renderer.Type.SPEAKER, "augustos-mbp.ad.unsw.edu.au",460,200, 0,"Speaker-Left", 0);
        rc.addRenderer(Renderer.Type.LIGHT, "augustos-mbp.ad.unsw.edu.au",460,200, 0,"Light-1", 0);

        FlockingModel myModel = new FlockingModel();  //HBSynchronisedModel2
        myModel.setup(this, hb, SynchronisedModel.ExecutionMode.LOCAL);
        myModel.setup2DSpaceSize(600, 400);
        myModel.setupFlock(10, 0);
        myModel.start();


        Clock clock = rc.addClockTickListener((offset, this_clock) -> {
            myModel.update();
            rc.renderers.forEach(r -> {
                LightAndSoundFlockingRenderer myR = (LightAndSoundFlockingRenderer) r;

                List<Integer> boidsAroundMe = myModel.getBoidsIdAroundXY((int)myR.x, (int)myR.y, 10);

                if(myR.type == Renderer.Type.LIGHT) {
                    if(boidsAroundMe.size() > 0) {
                        int boidId = boidsAroundMe.get(0);
                        rc.displayColor(myR, myR.boidsColour.get(boidId).red, myR.boidsColour.get(boidId).green, myR.boidsColour.get(boidId).blue);
                    } else {
                        rc.displayColor(myR, 1, 1, 1);
                    }
                }

                if(myR.type == Renderer.Type.SPEAKER) {
                    if (boidsAroundMe.size() == 0) {
                        int count = 0;
                        ((Gain) myR.out).setGain(0f);
                        for (WavePlayer wp : myR.speaker) {
                            wp.setFrequency(myR.defaultFreq[count]);
                            count++;
                        }
                    } else {
                        ((Gain) myR.out).setGain(0.1f);
                        for (WavePlayer wp : myR.speaker) {
                            wp.setFrequency(400 * (myR.id+2));
                        }
                    }
                }
            });
            rc.sendSerialcommand();
        });

        clock.setInterval(50);
        //clock.start();

        // type booleanControl to generate this code
        BooleanControl stopPlayControl = new BooleanControl(this, "Play", false) {
            @Override
            public void valueChanged(Boolean control_val) {// Write your DynamicControl code below this line
                /* To create this, just type clockTimer */
                if (control_val){
                    clock.start();/* End Clock Timer */
                }
                else {
                    clock.stop();
                    try {
                        Thread.sleep((long) 1000);
                        rc.turnOffLEDs();
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
