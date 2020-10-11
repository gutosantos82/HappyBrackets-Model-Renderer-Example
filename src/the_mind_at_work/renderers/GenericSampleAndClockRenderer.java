package the_mind_at_work.renderers;

import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.Glide;
import net.beadsproject.beads.ugens.GranularSamplePlayer;
import net.beadsproject.beads.ugens.SamplePlayer;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.sychronisedmodel.Renderer;
import net.happybrackets.sychronisedmodel.RendererController;
import java.util.ArrayList;
import java.util.List;

/**
 * This render allows you to:
 * load up a database of sounds
 * choose a sound to play
 * choose whether that sound is granular or straight
 */

public class GenericSampleAndClockRenderer extends Renderer {

    //list of sounds
    static List<Sample> samples = new ArrayList<>();

    //the renderer controller
    RendererController rc = RendererController.getInstance();

    //audio objects
    GranularSamplePlayer gsp;
    SamplePlayer sp;

    //audio controls
    Glide gain;
    Glide pitch;

    //other timing params
    int clockIntervalLock = 1;
    double clockLockPosition = 0;
    float clockDelayMS = 0;

    //light data
    public double[] rgbD = new double[]{0,0,0};

    @Override
    public void setupLight() {
        rc.addClockTickListener((offset, this_clock) -> {       //assumes clock is running at 20ms intervals for now
            if (clockIntervalLock > 0 && this_clock.getNumberTicks() % clockIntervalLock == 0) {

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep((long)clockDelayMS);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        lightLoopTrigger();
//                    }
//                }).start();
            }
            lightUpdate();
        });
    }

    @Override
    public void setupAudio() {
        //construct audio elements
        pitch = new Glide(1);
        gsp = new GranularSamplePlayer(1);
        sp = new SamplePlayer(1);
        setSample(0);
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
        useGranular(true);
        //set up a clock
        rc.addClockTickListener((offset, this_clock) -> {       //assumes clock is running at 20ms intervals for now
            if (clockIntervalLock > 0 && this_clock.getNumberTicks() % clockIntervalLock == 0) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            Thread.sleep((long)clockDelayMS);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
                        gsp.setPosition(clockLockPosition);
                        sp.setPosition(clockLockPosition);
//                    }
//                }).start();

            }
        });
    }

    public void lightLoopTrigger() {
        rgbD[0] = rgbD[1] = rgbD[2] = 255;
    }

    public void lightUpdate() {
        rgbD[0] *= 0.8f;
        rgbD[1] *= 0.8f;
        rgbD[2] *= 0.8f;
        rc.displayColor(this, (int)rgbD[0],(int)rgbD[1],(int)rgbD[2]);
    }

    public void triggerBeat() {
        lightLoopTrigger();
        if(gsp != null) {
            gsp.setPosition(clockLockPosition);
            sp.setPosition(clockLockPosition);
        }
    }

    public static void addSample(String samplename) {
        Sample sample = SampleManager.sample(samplename);
        if(sample != null) {
            samples.add(sample);
            System.out.println("Sample index: " + (samples.size() - 1) + ": " + samplename);
        } else {
            System.out.println("ERROR: there was a problem loading sample: " + samplename);
        }
    }

    public void useGranular(boolean yes) {
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
        sp.getRateUGen().setValue(rate);
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

}
