class Cle {
    final int id;
    Cle(int id) { this.id = id; }
    public boolean equals(Cle autre) { return autre != null && id == autre.id; }
    public int hashCode() { return id; }
}

void main() {
    Cle a = new Cle(7), b = new Cle(7);
    Object c = b;
    IO.println(a.equals(b));
    IO.println(a.equals(c));
    java.util.Set<Cle> s = new java.util.HashSet<>();
    s.add(a);
    s.add(b);
    IO.println(s.size());
    IO.println(java.util.List.of(a).contains(b));
}
