void main() {
    int a = 7;
    int b = a;
    a = a + 3;
    String s = "a=" + a + ", b=" + b;
    IO.println(s);
    IO.println(a + b);
    IO.println("a+b=" + a + b);
}
