package the_mind_at_work.sketches;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import the_mind_at_work.renderers.GenericSampleAndClockRenderer;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;

public class MyFirstGranularTest implements HBAction {
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device

        RendererController.setRenderer(GenericSampleAndClockRenderer.class);

       //if the model is running on the Pi....
        Model myModel = new Whatever();
        myModel.setRunOnPi();
        myModel.setParams();


        RenderController.addClockTickListener() {
            myModel.update();


            RendererController.renderers.forEach(/something here!/) {

                GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer)r;

                //grab stuff from the model
                x = model.getFieldIntensityAt(r.getLocation());

                //set the renderers
                myR.setPitch(x);
            };
        };




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
