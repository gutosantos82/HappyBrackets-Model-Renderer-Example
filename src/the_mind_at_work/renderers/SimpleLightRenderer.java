package the_mind_at_work.renderers;

import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;

public class SimpleLightRenderer extends Renderer {
    public boolean step1Finished = false;
    public boolean step2Finished = false;
    RendererController rc = RendererController.getInstance();

    public SimpleLightRenderer() {
    }

    @Override
    public void setupLight() {
        rc.displayColor(this, 1,0,0);
        colorMode(ColorMode.RGB, 255);
    }
}
