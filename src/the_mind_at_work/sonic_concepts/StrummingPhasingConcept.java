package the_mind_at_work.sonic_concepts;

import net.beadsproject.beads.data.Buffer;
import net.beadsproject.beads.data.Pitch;
import net.beadsproject.beads.data.Sample;
import net.beadsproject.beads.data.SampleManager;
import net.beadsproject.beads.ugens.*;
import net.happybrackets.core.HBAction;
import net.happybrackets.core.control.DynamicControl;
import net.happybrackets.core.control.FloatControl;
import net.happybrackets.core.scheduling.Clock;
import net.happybrackets.core.scheduling.Delay;
import net.happybrackets.device.HB;

import java.lang.invoke.MethodHandles;

public class StrummingPhasingConcept implements HBAction {


    String[] notes = {"A", "B", "D", "E", "G"};
    int baseOctaveC = 42;
    float[] freqs = {Pitch.mtof(baseOctaveC + 9), Pitch.mtof(baseOctaveC + 11), Pitch.mtof(baseOctaveC + 14), Pitch.mtof(baseOctaveC + 16) + Pitch.mtof(baseOctaveC + 19)};
    int chosenNote = 0;

    Sample noiseSample;

    SamplePlayer pluck;
    WavePlayer modulator;
    Glide modLevel;

    Clock clock;
    float timeDelay = 0;
    float timeInterval = 20;
    float startTime = 0;

    float r = 0, g = 0, b = 0;

    @Override
    public void action(HB hb) {
        hb.reset(); //Clears any running code on the device

        //load samples
        SampleManager.group("guitar_notes", "data/audio/Nylon_Guitar");
        noiseSample = SampleManager.sample("data/audio/simple_car_noise.wav");

        //set up audio chain
        pluck = new SamplePlayer(SampleManager.fromGroup("guitar_notes", chosenNote));
        pluck.setKillOnEnd(false);

        modulator = new WavePlayer(freqs[chosenNote], Buffer.SINE);
        modLevel = new Glide(0f);
        hb.sound(new Function(pluck, modulator, modLevel) {
            @Override
            public float calculate() {
                if(x[2] <= 1f) {
                    float ratioA = x[2];
                    float ratioB = 1 - ratioA;
                    return x[0] * (ratioB + x[1] * ratioA);
                } else if(x[2] <= 2f) {
                    float ratioA = x[2] - 1f;
                    float ratioB = 1 - ratioA;
                    return x[0] * x[1] * ratioB + x[1] * ratioA;
                } else {
                    float ratioA = x[2] - 2f;
                    float ratioB = 1 - ratioA;
                    return x[1] * ratioB + x[0] * x[1] * ratioA;
                }
            }
        });

        //set up the clock
        clock = hb.createClock(timeInterval);
        clock.addClockTickListener(new Clock.ClockTickListener() {
            @Override
            public void clockTick(double v, Clock clock) {
                //sound
                if(clock.getNumberTicks() % 100 == 0) {
                    if (timeDelay > 0) {
                        hb.doAtTime(hb.getSynchTime() + timeDelay, new Delay.DelayListener() {
                            @Override
                            public void delayComplete(double v, Object o) {
                                pluck.setPosition(startTime);
                                lightLoopTrigger();
                            }
                        });
                    } else {
                        pluck.setPosition(startTime);
                    }
                }
                //light
                lightUpdate();
            }
        });
        clock.start();

        // Type floatSliderControl to generate this code
        FloatControl floatControl = new FloatControl(this, "mod ratio", 0) {
            @Override
            public void valueChanged(double control_val) {// Write your DynamicControl code below this line
                setModLevel((float)control_val);
            }
        }.setDisplayRange(0, 3, DynamicControl.DISPLAY_TYPE.DISPLAY_DEFAULT);// End DynamicControl floatControl code
    }

    void lightLoopTrigger() {
        if(modLevel.getCurrentValue() < 2) {
            r = g = b = 1;
        }
    }

    void lightUpdate() {
        if(modLevel.getCurrentValue() < 2) {        //TODO get the crossfades working
            rgb[0] *= 0.09f;                             //TODO between light modes
            g *= 0.09f;
            b *= 0.09f;
        } else {
            //light sparkles

        }

        //code to set the light??

    }


    void setModLevel(float level) {
        modLevel.setValue(level);
        if(level >= 2 && !pluck.getSample().equals(noiseSample)) {
            pluck.setSample(noiseSample);
            pluck.setLoopType(SamplePlayer.LoopType.LOOP_ALTERNATING);
        } else if(level < 2 && pluck.getSample().equals(noiseSample)) {
            pluck.setSample(SampleManager.fromGroup("guitar_notes", chosenNote));
            pluck.setLoopType(SamplePlayer.LoopType.NO_LOOP_FORWARDS);
            modulator.setFrequency(freqs[chosenNote]);
        }
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
