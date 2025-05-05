import java.util.Arrays;

import javax.print.DocFlavor.STRING;

public class MandelbrotPoint {

    private double maxRad = 2.0;
    public double a;
    public double b;

    public double x;
    public double y;

    public MandelbrotPoint(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public MandelbrotPoint() {
        a = 0;
        b = 0;
    }
    public int iters(int maxIters) {
        x = 0;
        y = 0;
        int i = 0;
        while (x * x + y * y <= maxRad * maxRad && ++i < maxIters) {
            double xTmp = x * x - y * y + a;
            y = 2 * x * y + b;
            x = xTmp;
        }
        return i;
    }

    public String toString() {
        return "("+a+","+b+"i)";
    }

    public static MandelbrotPoint parseString(String s) {
        int len = s.length();
        String[] numberStrings = s.substring(1, len-2).split(",");
        return new MandelbrotPoint(Double.parseDouble(numberStrings[0]), Double.parseDouble(numberStrings[1]));
    }
}
