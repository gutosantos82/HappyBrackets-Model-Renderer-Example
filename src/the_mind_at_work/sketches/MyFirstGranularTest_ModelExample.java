package the_mind_at_work.sketches;

import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import the_mind_at_work.renderers.GenericSampleAndClockRenderer;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;


//TODO: need RC to take care of strip size in config
//TODO: need to be able to blend colours, need a basic colour data structure? Or not?
//TODO: copying a renderer to the device does not copy its nested classes

public class MyFirstGranularTest_ModelExample implements HBAction, HBReset {

    final int PORT = 4000;
    RendererController rc = RendererController.getInstance();

    @Override
    public void action(HB hb) {
        System.out.println("___The_Mind_At_Work___");
        hb.reset(); //Clears any running code on the device
        rc.reset();

        //adding some samples
        GenericSampleAndClockRenderer.samples.clear();  //note this does not clear SampleManager.
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_B_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_D_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_E_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_G_harm.wav");

        //set up the RC
        rc.setRendererClass(GenericSampleAndClockRenderer.class);

        //set up the configuration of the system
        rc.addRenderer(Renderer.Type.SPEAKER, "hb-b827eb999a03",120,200, 0,"Speaker-Left", 0);
        rc.addRenderer(Renderer.Type.SPEAKER,"hb-b827eb999a03",460,200, 0,"Speaker-Right", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,90, 0,"Light-1", 0);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,310, 0,"Light-2", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,310, 0,"Light-3", 2);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,90, 0,"Light-4", 3);

        rc.getInternalClock().setInterval(50);

        //set up
        rc.renderers.forEach(r -> {
            GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer)r;
            //here we start mapping
            myR.clockInterval(0);
            if(myR.type == Renderer.Type.SPEAKER) {
                //speaker behaviours
                myR.useGranular(true);
            }
        });

//        FlockingModel myModel = new FlockingModel();  //HBSynchronisedModel2
//        myModel.setup(this, hb, SynchronisedModel.ExecutionMode.LOCAL);
//        myModel.setup2DSpaceSize(600, 400);
//        myModel.setupFlock(10, 0);
//        myModel.start();
//
//        rc.addClockTickListener((offset, this_clock) -> {
//            myModel.update();
//            //hb.setStatus("tick: " + this_clock.getNumberTicks());
//            rc.renderers.forEach(r -> {
//                GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer)r;
//
//                List<Integer> boidsAroundMe = myModel.getBoidsIdAroundXY((int)myR.x, (int)myR.y, 10);
//
//                if(myR.type == Renderer.Type.SPEAKER) {
//                    //speaker behaviours
//                    if(boidsAroundMe.size() > 0) {
//                        myR.pitch(2);
//                    } else {
//                        myR.pitch(1);
//                    }
//                } else if(myR.type == Renderer.Type.LIGHT) {
//                    //light behaviours
//                        //rc.displayColor(myR, hb.rng.nextInt(256), hb.rng.nextInt(256), hb.rng.nextInt(256));
//                        //rc.displayColor(myR, (int)myR.rgbD[0],(int)myR.rgbD[1],(int)myR.rgbD[2]);
//                     }
//
//            });
//        });

        rc.addClockTickListener((offset, this_clock) -> {
            rc.sendSerialcommand();
        });

        rc.getInternalClock().start();


        /* type osclistener to create this code */
        OSCUDPListener oscudpListener = new OSCUDPListener(PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                if(oscMessage.getName().equals("/sqk")) {
                    int id = (int) oscMessage.getArg(0);
                    int x = (int) oscMessage.getArg(1);
                    int y = (int) oscMessage.getArg(2);
                    int size = (int) oscMessage.getArg(3);
                    int sparkle = (int) oscMessage.getArg(4);
                    int brightness = (int) oscMessage.getArg(5);
                    int sound = (int) oscMessage.getArg(6);

                    rc.renderers.forEach(renderer -> {
//                        if(Math.abs(renderer.x - x) < 10 && Math.abs(renderer.y - y) < 10) {
//                        }
                        if(renderer.type == Renderer.Type.SPEAKER) {
                            GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer) renderer;
                            myR.pitch((brightness / 127f) + 1);
                        }
                    });


                }
                if(oscMessage.getName().equals("/beat")) {
                    int beatCount = (int)oscMessage.getArg(0);
                    rc.renderers.forEach(renderer -> {
                        GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer) renderer;
                        myR.triggerBeat(beatCount);
                    });
                }
                /* type your code above this line */
            }
        };
        if (oscudpListener.getPort() < 0){ //port less than zero is an error
            String error_message =  oscudpListener.getLastError();
        } else {
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

    @Override
    public void doReset()
    {
        rc.turnOffLEDs();
        rc.reset();
    }
    //</editor-fold>
}
