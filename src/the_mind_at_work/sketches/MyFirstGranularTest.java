package the_mind_at_work.sketches;

import de.sciss.net.OSCListener;
import de.sciss.net.OSCMessage;
import net.happybrackets.core.HBAction;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Flock;
import net.happybrackets.sychronisedmodel.FlockingModel;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import the_mind_at_work.renderers.GenericSampleAndClockRenderer;

import java.lang.invoke.MethodHandles;
import java.net.SocketAddress;

public class MyFirstGranularTest implements HBAction {
    RendererController rc = RendererController.getInstance();
    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device

        rc.setRendererClass(GenericSampleAndClockRenderer.class);

       //if the model is running on the Pi....
        FlockingModel myModel = new FlockingModel();
        myModel.setup(this, hb);
        myModel.setup2DSpaceSize(600, 400);


        rc.addClockTickListener((offset, this_clock) -> {
            myModel.update();


            rc.renderers.forEach(r -> {

                GenericSampleAndClockRenderer myR = (GenericSampleAndClockRenderer) r;

                //grab stuff from the model
                double x = myModel.getIntensityAtXY((int)myR.x, (int)myR.y);

                //set the renderers
                myR.pitch((float)x);
            });
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
    //</editor-fold>
}
