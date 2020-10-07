package the_mind_at_work.sketches;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import the_mind_at_work.renderers.GenericSampleAndClockRenderer;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;


//TODO: need RC to take care of strip size in config
//TODO: need to be able to blend colours, need a basic colour data structure? Or not?
//TODO: copying a renderer to the device does not copy its nested classes

public class MyFirstGranularTest implements HBAction, HBReset {
    RendererController rc = RendererController.getInstance();
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device
        rc.reset();

        //adding some samples
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_B_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_D_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_E_harm.wav");
        GenericSampleAndClockRenderer.addSample("data/audio/Nylon_Guitar/Clean_G_harm.wav");

        //set up the RC
        rc.setRendererClass(GenericSampleAndClockRenderer.class);

        //set up the configuration of the system
//        rc.addRenderer(Renderer.Type.SPEAKER, "hb-b827eb999a03",120,200, 0,"Speaker-Left", 0);
//        rc.addRenderer(Renderer.Type.SPEAKER,"hb-b827eb999a03",460,200, 0,"Speaker-Right", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,90, 0,"Light-1", 0);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,310, 0,"Light-2", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,310, 0,"Light-3", 2);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,90, 0,"Light-4", 3);


        rc.addClockTickListener((offset, this_clock) -> {

            hb.setStatus("tick: " + this_clock.getNumberTicks());
            rc.renderers.forEach(r -> {
                GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer)r;


                //here we start mapping

                myR.clockInterval(20);

                if(myR.type == Renderer.Type.SPEAKER) {
                    //speaker behaviours
                    myR.pitch(hb.rng.nextFloat() *0.2f + 1);
                    myR.useGranular(false);
                } else if(myR.type == Renderer.Type.LIGHT) {
                    //light behaviours
                    rc.displayColor(myR, hb.rng.nextInt(256), hb.rng.nextInt(256), hb.rng.nextInt(256));

                     }



            });
        });
        rc.getInternalClock().setInterval(50);
        rc.sendSerialcommand();

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
    public void doReset() {
        rc.reset();
    }
    //</editor-fold>
}
