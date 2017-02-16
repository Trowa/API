package esialrobotik.ia.utils.gpio.raspberry;

import com.pi4j.io.gpio.GpioPinDigital;

/**
 * Gpio abstract class
 *
 * Représentation des GPIOs pour une raspberry pi via la libraire Pi4J
 * @see <a href="http://pi4j.com/usage.html">Pi4J Usage</a>
 * @see <a href="http://pi4j.com/pins/model-3b-rev1.html">Raspberry Pi 3 Model B - Pin numbering</a>
 */
public abstract class Gpio {

    /**
     * Pi4J GPIO
     */
    protected GpioPinDigital gpioPinDigital;

    /**
     * Vérifie si l'état du GPIO est haut
     * @return true if GPIO is high
     */
    public boolean isHigh() {
        return gpioPinDigital.isHigh();
    }

    /**
     * Vérifie si l'état du GPIO est bas
     * @return true if GPIO is low
     */
    public boolean isLow() {
        return gpioPinDigital.isLow();
    }

}
