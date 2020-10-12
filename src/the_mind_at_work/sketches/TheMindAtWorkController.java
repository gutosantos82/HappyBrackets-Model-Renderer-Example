package the_mind_at_work.sketches;

import de.sciss.net.OSCMessage;
import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.OSCUDPListener;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import the_mind_at_work.renderers.GenericSampleAndClockRenderer;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.net.SocketAddress;

public class TheMindAtWorkController implements HBAction, HBReset {

    final int PORT = 4000;
    RendererController rc = RendererController.getInstance();
    HB hb;

    @Override
    public void action(HB hb) {
        this.hb = hb;
        System.out.println("___The_Mind_At_Work___");
        hb.reset(); //Clears any running code on the device
        rc.reset();
        rc.getInternalClock().setInterval(50);
        //adding some samples
        GenericSampleAndClockRenderer.samples.clear();
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_B_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_D_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_E_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_G_harm.wav");
        //set up the RC
        rc.setRendererClass(GenericSampleAndClockRenderer.class);
        //set up the configuration of the system
        rc.addRenderer(Renderer.Type.SPEAKER, "hb-b827eb999a03",0,0, 0,"Speaker-Left", 0);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",0.1f,0, 0,"Light-1", 0);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",0.2f,0, 0,"Light-2", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",0.3f,0, 0,"Light-3", 2);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",0.4f,0, 0,"Light-4", 3);
        rc.addRenderer(Renderer.Type.SPEAKER,"hb-b827eb999a03",0.5f,0, 0,"Speaker-Right", 1);
        //init to movement 1
        m1(null);
        //this is just to flush out serial stuff?
        rc.addClockTickListener((offset, this_clock) -> {
            rc.sendSerialcommand();
        });
        //generic OSC listener - for any message, e.g., "/sqk" it tries to call the function, e.g., "sqk".
        //this could be set up as a built-in feature
        new OSCUDPListener(PORT) {
            @Override
            public void OSCReceived(OSCMessage oscMessage, SocketAddress socketAddress, long l) {
                try {
                    String methodName = oscMessage.getName().substring(1);
                    Method m = TheMindAtWorkController.class.getMethod(methodName, OSCMessage.class);
                    m.invoke(TheMindAtWorkController.this, oscMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
    }

    //osc messages
    public void m1(OSCMessage oscMessage) {
        hb.setStatus("Movement 1");
        rc.renderers.forEach(renderer -> {
            GenericSampleAndClockRenderer r = (GenericSampleAndClockRenderer)renderer;
            r.clockInterval(0);
            r.clockLockPosition(0);
            r.clockDelay(0);
            r.useGranular(true);
            r.pitch(1);
            r.rate(1);
            r.random(0.01f);
            r.loopType(SamplePlayer.LoopType.NO_LOOP_FORWARDS);
            r.clearLFO();
            r.gain(0);
            r.brightness(0);
            r.grainInterval(40);
            r.grainOverlap(1.2f);
//            r.setLFORingMod();
//            r.lfoFreq(1f);
//            r.lfoDepth(0.5f);
//            r.lfoWave(Buffer.NOISE);
        });
    }

    public void m2(OSCMessage oscMessage) {
        hb.setStatus("Movement 2");
    }

    public void m3(OSCMessage oscMessage) {
        hb.setStatus("Movement 3");
    }

    public void m4(OSCMessage oscMessage) {
        hb.setStatus("Movement 4");
    }


    public void sqk(OSCMessage oscMessage) {
        //grab args
        int arg = 0;
        float x = (int)oscMessage.getArg(arg++) / 127f;
        float y = (int)oscMessage.getArg(arg++) / 127f;
        float size = (int)oscMessage.getArg(arg++) / 127f;
        float sparkle = (int)oscMessage.getArg(arg++) / 127f;
        float bright = (int)oscMessage.getArg(arg++) / 127f;
        int sound = (int)oscMessage.getArg(arg++);
        int timeSinceEventStartMS = (int)oscMessage.getArg(arg++);
        rc.renderers.forEach(renderer -> {
            GenericSampleAndClockRenderer r = (GenericSampleAndClockRenderer)renderer;
            //determine if this renderer is active for this event
            if(size > 0 && meanSquare(x,y,r.x, r.y) < size*size) {
                //yes we are in the blob, do we need to trigger the sound?
                if(        timeSinceEventStartMS <= 0
                        || r.currentSample != sound
                        || r.timeSinceLastTriggerMS() > (timeSinceEventStartMS + 100)
                ) {
                    r.setSample(sound);
                    r.triggerSampleWithOffset(timeSinceEventStartMS);
                }
                r.setRGB(255,0,0);
                r.brightness(1);
                r.gain(1);
            }
        });
    }

    public void beat(OSCMessage oscMessage) {
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
