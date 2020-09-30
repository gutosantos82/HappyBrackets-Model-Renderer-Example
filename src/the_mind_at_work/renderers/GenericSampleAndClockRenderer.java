package the_mind_at_work.renderers;

import net.beadsproject.beads.core.UGen;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Gain;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.device.HB;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This render allows you to:
 * load up a database of sounds
 * choose a sound to play
 * choose whether that sound is granular or straight
 *
 *
 */

public class GenericSampleAndClockRenderer extends Renderer {

    //audio objects
    GranularSamplePlayer gsp;
    SamplePlayer sp;
                              //TODO Renderer should have a standard way to identify an "output" UGen.

    //audio controls
    Glide gain;
    Glide pitch;

    //other timing params
    int clockIntervalLock = 1;
    double clockLockPosition = 0;
    float clockDelayMS = 0;

    List<Sample> samples = new ArrayList<>();


    public void setupAudio() {      //this should override a setupAudio function in Renderer
                                    //also have a setupLight function too. Both are optional.

        //construct audio elements
        pitch = new Glide(1);

        addSample("data/audio/Nylon_Guitar/Clean_A_harm.wav");
        gsp.setKillOnEnd(false);
        gsp.setInterpolationType(SamplePlayer.InterpolationType.LINEAR);
        gsp.setLoopType(SamplePlayer.LoopType.NO_LOOP_FORWARDS);
        gsp.getGrainSizeUGen().setValue(100);
        gsp.getRandomnessUGen().setValue(0.1f);
        gsp.setPitch(pitch);

        sp.setKillOnEnd(false);
        sp.setInterpolationType(SamplePlayer.InterpolationType.LINEAR);
        sp.setLoopType(SamplePlayer.LoopType.NO_LOOP_FORWARDS);
        sp.setPitch(pitch);

        //out = new Gain(1, gain);

        useGranular(true);

        /*
        RendererController.addClockTickListener((offset, this_clock) -> {       //assumes clock is running at 20ms intervals for now

            if (clockIntervalLock > 0 && this_clock.getNumberTicks() % clockIntervalLock == 0) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep((long)clockDelayMS);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        gsp.setPosition(clockLockPosition);
                        sp.setPosition(clockLockPosition);
                        lightLoopTrigger();
                    }
                }).start();

            }
            lightUpdate();

        });
        */
    }

    void lightLoopTrigger() {
        /*
        if(modLevel.getCurrentValue() < 2) {
            rgb[0] = rgb[1] = rgb[2] = 0;
        }
        */
    }

    void lightUpdate() {
        /*
        if(modLevel.getCurrentValue() < 2) {        //TODO get the crossfades working
            rgb[0] *= 0.09f;                             //TODO between light modes
            rgb[1] *= 0.09f;
            rgb[3] *= 0.09f;
        } else {
            //light sparkles

        }

        //code to set the light??
        */
    }

    public void addSample(String samplename) {
        Sample sample = SampleManager.sample(samplename);
        if(sample != null) {
            samples.add(sample);
            System.out.println("Sample index: " + (samples.size() - 1) + ": " + samplename);
        } else {
            System.out.println("ERROR: there was a problem loading sample: " + samplename);
        }
    }

    public void useGranular(boolean yes) {
        //TODO could potentially do a crossfade
        out.clearInputConnections();
        if(yes) {
            out.addInput(gsp);
        } else {
            out.addInput(sp);
        }
    }

    public void setSample(int index) {
        if(samples.size() > index) {
            gsp.setSample(samples.get(index));
            sp.setSample(samples.get(index));
        }
    }

    public void rate(float rate) {
        gsp.getRateUGen().setValue(rate);
        sp.getRateUGen().setValue(rate);        //this should actually end up setting the pitch Glide object
    }

    public void grainOverlap(float overlap) {
        float interval = gsp.getGrainIntervalUGen().getValue();
        gsp.getGrainSizeUGen().setValue(interval * overlap);
    }

    public void grainInterval(float interval) {
        gsp.getGrainIntervalUGen().setValue(interval);
    }

    public void gain(float gain) {
        this.gain.setValue(gain);
    }

    public void random(float random) {
        gsp.getRandomnessUGen().setValue(random);
    }

    public void pitch(float pitch) {
        this.pitch.setValue(pitch);
    }

    public void loopStart(float start) {
        gsp.getLoopStartUGen().setValue(start);
    }

    public void loopEnd(float end) {
        gsp.getLoopEndUGen().setValue(end);
    }

    public void clockInterval(int interval) {
        clockIntervalLock = interval;
    }

    public void clockDelay(float delayMS) {
        clockDelayMS = delayMS;
    }

    public void sample(String sample) {
        Sample s = SampleManager.sample(sample);
        gsp.setSample(s);
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
