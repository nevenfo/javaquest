void main() {
    String prenom = IO.readln("Prénom ? ");
    int naissance = Integer.parseInt(IO.readln("Année de naissance ? "));
    IO.println("Salut " + prenom + " ! Tu as " + (2026 - naissance) + " ans.");
}
