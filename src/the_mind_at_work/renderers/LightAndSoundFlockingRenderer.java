package the_mind_at_work.renderers;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.WavePlayer;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;

import java.util.ArrayList;
import java.util.List;

public class LightAndSoundFlockingRenderer extends Renderer {
    public boolean step1Finished = false;
    public boolean step2Finished = false;
    RendererController rc = RendererController.getInstance();
    public List<WavePlayer> speaker;
    public float[] defaultFreq;

    public class colour {

        public int red;
        public int green;
        public int blue;

        colour(int red, int green, int blue) {
            this.red = red;
            this.green = green;
            this.blue = blue;
        }
    }

    public List<colour> boidsColour;

    private final int WAVEPLAYER_COUNT = 8;


    public LightAndSoundFlockingRenderer() {
        boidsColour = new ArrayList<>();
        boidsColour.add(0,new colour(255,0,0));
        boidsColour.add(1,new colour(0,255,0));
        boidsColour.add(2,new colour(0,0,255));
        boidsColour.add(3,new colour(255,255,0));
        boidsColour.add(4,new colour(255,0,255));
        boidsColour.add(5,new colour(0,255,255));
        boidsColour.add(6,new colour(255,0,128));
        boidsColour.add(7,new colour(128,0,255));
        boidsColour.add(8,new colour(128,128,255));
        boidsColour.add(9,new colour(255,128,255));
    }

    @Override
    public void setupLight() {
        rc.displayColor(this, 1,0,0);
        colorMode(ColorMode.RGB, 255);
    }

    @Override
    public void setupAudio() {
        speaker = new ArrayList<WavePlayer>();
        defaultFreq = new float[WAVEPLAYER_COUNT];
        ((Gain) out).setGain(0.1f);
        for (int i = 0; i < WAVEPLAYER_COUNT; i++) {
            defaultFreq[i] = 100 * i + (id+1)*50;
            WavePlayer wp = new WavePlayer(defaultFreq[i], Buffer.SINE);
            speaker.add(wp);
            out.addInput(wp);
        }
    }
}
