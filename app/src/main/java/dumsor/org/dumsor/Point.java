package dumsor.org.dumsor;

/**
 * Created by Jim on 11/7/2015.
 */
public class Point {
    public int id;
    public double lat, lng;
    public String auth, source;
    public long timestamp, previous, power;

    public Point() {

    }

    public Point(int lat, int lng, String auth, String source, int timestamp, int previous, int power) {
        this.id = 0;
        this.lat = lat;
        this.lng = lng;
        this.auth = auth;
        this.source = source;
        this.timestamp = timestamp;
        this.previous = previous;
        this.power = power;
    }
}
