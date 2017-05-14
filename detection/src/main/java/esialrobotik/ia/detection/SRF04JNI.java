package esialrobotik.ia.detection;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Created by Trowa on 04/05/2016.
 */
public class SRF04JNI implements UltraSoundInterface{
    private int gpio_in;
    private int gpio_out;

    private int fd_in = -1;
    private int fd_out = -1;

    static{
        System.loadLibrary("srf04");
    }

    public SRF04JNI(int gpio_in, int gpio_out){
        this.gpio_in = gpio_in;
        this.gpio_out = gpio_out;

        this.init();
    }

    @Inject
    public SRF04JNI(@Assisted DetectionModuleConfiguration.GPioPair pair) {
        this.gpio_in = pair.gpio_in;
        this.gpio_out = pair.gpio_out;

        this.init();
    }


    public native void init();

    /**
     * WARNING !!! Attender 12ms entre 2 mesures, même sur des capteurs différents pour ne pas capter des echos foireux
     * @return mesure du télémètre en mm
     */
    public native long getMeasure();

    public int getGpio_out() {
        return gpio_out;
    }

    public void setGpio_out(int gpio_out) {
        this.gpio_out = gpio_out;
    }

    public int getGpio_in() {
        return gpio_in;
    }

    public void setGpio_in(int gpio_in) {
        this.gpio_in = gpio_in;
    }

    public int getFd_in() {
        return fd_in;
    }

    public void setFd_in(int fd_in) {
        this.fd_in = fd_in;
    }

    public int getFd_out() {
        return fd_out;
    }

    public void setFd_out(int fd_out) {
        this.fd_out = fd_out;
    }
}

