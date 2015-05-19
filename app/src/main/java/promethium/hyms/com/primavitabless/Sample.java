package promethium.hyms.com.primavitabless;

import com.orm.SugarRecord;

/**
 * Created by nephilim on 4/28/15.
 */
public class Sample extends SugarRecord<Sample> {
    float temperature;
    float breathing_rate;
    float heart_rate;

    public Sample(){
    }

    public Sample(float temperature){
        this.temperature = temperature;
    }

    public Sample(float temperature, float breathing_rate, float heart_rate){
        this.temperature = temperature;
        this.breathing_rate = breathing_rate;
        this.heart_rate = heart_rate;
    }
}
