package the_mind_at_work.sketches;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.SampleManager;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import the_mind_at_work.renderers.GenericSampleAndClockRenderer;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;

public class TheMindAtWorkController implements HBAction, HBReset {

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
        init();

        //generic OSC listener - for any message, e.g., "/sqk" it tries to call the function, e.g., "sqk".
        new OSCUDPListener(PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                try {
                    this.getClass().getMethod(oscMessage.getName().substring(1), OSCMessage.class).invoke(this, oscMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    //osc messages

    void init() {
        movement1(null);
    }

    void movement1(OSCMessage oscMessage) {
        rc.renderers.forEach(r -> {
            GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer)r;
            myR.clockInterval(0);
            myR.clockLockPosition(0);
            myR.clockDelay(0);
            myR.useGranular(true);
        });
    }


    void sqk(OSCMessage oscMessage) {
        //grab args
        int arg = 0;
        int x = (int)oscMessage.getArg(arg++);
        int y = (int)oscMessage.getArg(arg++);
        int size = (int)oscMessage.getArg(arg++);
        int sparkle = (int)oscMessage.getArg(arg++);
        int hue = (int)oscMessage.getArg(arg++);
        int sound = (int)oscMessage.getArg(arg++);
        int timeSinceEventStartMS = (int)oscMessage.getArg(arg++);

        rc.renderers.forEach(renderer -> {
            GenericSampleAndClockRenderer r = (GenericSampleAndClockRenderer)renderer;


            //determine if this renderer is active for this event
            //TODO - tricky



            if(size > 0 && meanSquare(x,y,r.x, r.y) < size*size) {
                //yes active
                if(timeSinceEventStartMS == 0) {
                    r.setSample(sound);
                    r.triggerSampleWithOffset(0);
                } else if(r.currentSample != sound) {
                    r.setSample(sound);
                    r.triggerSampleWithOffset(timeSinceEventStartMS);
                }
                r.brightness(1);
                r.gain(1);
            }

            //TODO need to find a way to end sound! Add a timeout?
        });
    }


    void beat(OSCMessage oscMessage) {
        int beatCount = (int)oscMessage.getArg(0);
        rc.renderers.forEach(renderer -> {
            GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer) renderer;
            myR.triggerBeat(beatCount);
        });
    }


    //utility functions

    float meanSquare(float x1, float y1, float x2, float y2) {
        return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
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
