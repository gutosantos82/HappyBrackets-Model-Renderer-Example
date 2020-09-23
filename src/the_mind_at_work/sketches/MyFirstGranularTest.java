package the_mind_at_work.sketches;

import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import the_mind_at_work.renderers.GenericSampleAndClockRenderer;

import java.lang.invoke.MethodHandles;

public class MyFirstGranularTest implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device

        GenericSampleAndClockRenderer r = new GenericSampleAndClockRenderer(hb);

        RendererController.setRenderer(GenericSampleAndClockRenderer.class);



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
