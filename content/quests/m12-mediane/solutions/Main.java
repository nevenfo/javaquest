import java.util.Arrays;

void main() {
    int[] t = new int[4];
    int n = 0;
    int poids = Integer.parseInt(IO.readln("Poids (0 pour finir) ? "));
    while (poids != 0) {
        if (n == t.length) {
            t = Arrays.copyOf(t, t.length * 2);
        }
        t[n] = poids;
        n++;
        poids = Integer.parseInt(IO.readln("Poids (0 pour finir) ? "));
    }
    Arrays.sort(t, 0, n);
    double mediane;
    if (n % 2 == 1) {
        mediane = t[n / 2];
    } else {
        mediane = (t[n / 2 - 1] + t[n / 2]) / 2.0;
    }
    IO.println("Médiane : " + mediane);
}
