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

public class MyFirstGranularTest implements HBAction, HBReset {

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
            myR.clockInterval(0);
            myR.clockDelay(0);
            if(myR.type == Renderer.Type.SPEAKER) {
                //speaker behaviours
                myR.useGranular(true);
            }
        });

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

    void sqk(OSCMessage oscMessage) {
        int arg = 0;
        int id = (int)oscMessage.getArg(arg++);
        int x = (int)oscMessage.getArg(arg++);
        int y = (int)oscMessage.getArg(arg++);
        int size = (int)oscMessage.getArg(arg++);
        int sparkle = (int)oscMessage.getArg(arg++);
        int brightness = (int)oscMessage.getArg(arg++);
        int sound = (int)oscMessage.getArg(arg++);
        rc.renderers.forEach(renderer -> {
            if(renderer.type == Renderer.Type.SPEAKER) {
                GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer) renderer;
                myR.pitch((brightness / 127f) + 1);
            }
        });
    }

    void beat(OSCMessage oscMessage) {
        rc.renderers.forEach(renderer -> {
            GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer) renderer;
            myR.triggerBeat();
        });
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
