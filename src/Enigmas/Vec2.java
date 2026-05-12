public class Vec2{
    public double x = 0;
    public double y = 0;
    public Vec2(double x, double y){
        this.x = x;
        this.y = y;
    }
    public double distanceTo(Vec2 point){
        double dx = this.x - point.x;
        double dy = this.y - point.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}