package the_mind_at_work.sketches;

import net.happybrackets.core.HBAction;
import net.happybrackets.core.HBReset;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import the_mind_at_work.renderers.SimpleLightRenderer;

import java.lang.invoke.MethodHandles;

public class SimpleLightExample implements HBAction, HBReset {

    RendererController rc = RendererController.getInstance();

    @Override
    public void doReset() {
        rc.turnOffLEDs();
        rc.disableSerial();
    }

    @Override
    public void action(HB hb) {
        hb.reset();
        rc.reset(); // Clear the RendererController renderer list
        rc.setHB(hb);

        rc.setRendererClass(SimpleLightRenderer.class);

        // Add your simulator to this list test the code locally.
        rc.addRenderer(Renderer.Type.SPEAKER, "hb-b827eb999a03",120,200, 0,"Speaker-Left", 0);
        rc.addRenderer(Renderer.Type.SPEAKER,"hb-b827eb999a03",460,200, 0,"Speaker-Right", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,90, 0,"Light-1", 0);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",120,310, 0,"Light-2", 1);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,310, 0,"Light-3", 2);
        rc.addRenderer(Renderer.Type.LIGHT,"hb-b827eb999a03",460,90, 0,"Light-4", 3);
        rc.addRenderer(Renderer.Type.SPEAKER, "augustos-mbp.ad.unsw.edu.au",10.5f,8, 0,"Speaker-Left", 0);
        rc.addRenderer(Renderer.Type.LIGHT, "augustos-mbp.ad.unsw.edu.au",10.5f,8, 0,"Light-1", 0);

        Clock clock = rc.addClockTickListener((offset, this_clock) -> {
            rc.renderers.forEach(r -> {
                SimpleLightRenderer myR = (SimpleLightRenderer) r;
                if(myR.type == Renderer.Type.LIGHT) {
                    if(myR.rgb[0] < 255 && !myR.step1Finished) {
                        myR.changeBrigthness(2);
                    } else {
                        myR.step1Finished = true;
                    }
                    if(myR.step1Finished && !myR.step2Finished) {
                        myR.changeHue(2);
                        if(myR.rgb[0] == 255 && myR.rgb[1] < 4 && myR.rgb[2] < 4) {
                            myR.step2Finished = true;
                        }
                    }
                    if(myR.step1Finished && myR.step2Finished) {
                        myR.changeBrigthness(-2);
                    }

                    // System.out.println(myR.id + " - red: " + myR.rgb[0] + " green: " + myR.rgb[1] + " blue: " + myR.rgb[2]);

                    // After calculating the new color. Push it to the serial 'queue'
                    rc.pushLightColor(myR, 18);
                }
            });

            // The developer must 'commit' the serial command to the serial port manually
            // The pushLightColor does not send the command to the serial as it is more efficient to send one command with all 4 LEDs instructions.
            rc.sendSerialcommand();
        });

        clock.setInterval(50);
        clock.start();

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

