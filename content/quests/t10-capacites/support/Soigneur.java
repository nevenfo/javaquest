public interface Soigneur {
    int soin();

    default String priere() {
        return "Je soigne " + soin() + " PV.";
    }
}
