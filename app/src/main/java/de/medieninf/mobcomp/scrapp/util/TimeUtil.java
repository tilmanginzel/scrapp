package de.medieninf.mobcomp.scrapp.util;

/**
 * Time constants, like days or hours and utils to convert gui values to database values.
 */
public class TimeUtil {

    public static final int MIN = 0;
    public static final String TIME_PATTERN = "HH:mm";
    public static final String DATE_PATTERN = "dd-MM-yyyy";

    /**
     * Converts days, hours and minutes to minute-interval.
     * @return interval in minutes
     */
    public static int calculateInterval(int entityPosition, int interval){
        ENTITY entity = ENTITY.values()[entityPosition];
        return entity.getFactor() * interval;
    }

    /**
     * Returns the matching entity to given interval.
     * @param interval amount of minutes
     * @return matching entity
     */
    public static ENTITY getEntity(int interval){
        for(ENTITY entity: ENTITY.values()) {
            if (interval %  entity.getFactor() == 0) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Re-converts a minute interval to days, hours and minutes.
     * @param interval interval in minutes
     * @return list of entities and related values
     */
    public static int reCalculateInterval(int interval, ENTITY entity){
        return interval / entity.getFactor();
    }

    /**
     * Representing all existing entities for time.
     */
    public enum ENTITY {
        MINUTE("Minuten", 1, 59),
        DAY("Tage", 24 * 60, 365),
        HOUR("Stunden", 60, 23),
        WEEK("Wochen", 24 * 60 * 7, 52);

        private final String name;
        private final int factor;
        private final int maxValue;

        ENTITY(String s, int f, int m) {
            name = s;
            factor = f;
            maxValue = m;
        }

        public String toString() {
            return this.name;
        }

        public int getFactor() { return this.factor; }

        public int getMaxValue(){
            return this.maxValue;
        }

        public static String [] getStringValues(){
            ENTITY [] entities = values();
            String [] strings = new String[entities.length];
            for(int i = 0; i < entities.length; i++){
                strings[i] = entities[i].toString();
            }
            return strings;
        }
    }
}
