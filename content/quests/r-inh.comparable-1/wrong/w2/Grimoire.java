public class Grimoire implements Comparable<Grimoire> {
    private final String titre;
    private final int pages;

    public Grimoire(String titre, int pages) {
        this.titre = titre;
        this.pages = pages;
    }

    public String getTitre() { return titre; }
    public int getPages() { return pages; }

    @Override
    public String toString() { return titre + "/" + pages; }

    @Override
    public int compareTo(Grimoire autre) {
        return titre.compareTo(autre.titre);
    }
}
