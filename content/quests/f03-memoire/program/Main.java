void main() {
    String secret = "lune";
    String saisie = new String("lune");
    String debut = "lu";
    String fin = debut + "ne";
    String copie = secret;

    IO.println(secret == saisie);
    IO.println(secret.equals(saisie));
    IO.println(secret == fin);
    IO.println(secret.equals(fin));
    IO.println(secret == copie);
}
