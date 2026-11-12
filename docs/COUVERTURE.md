# Couverture pédagogique

Généré par `node tools/coverage.mjs` le 2026-11-12 à partir de `content/`. Chaque activité est validée automatiquement (`Validate` : solutions de référence acceptées, solutions fautives refusées).

**131 activités**, **41 compétences**.

| guidé | entraînement | autonome | transfert | réactivation | examen |
|---|---|---|---|---|---|
| 22 | 26 | 29 | 21 | 29 | 4 |

| Compétence | Région | Provenance (cours) | Guidé | Autonome | Transfert | Réactivation | Examen | Parcours complet |
|---|---|---|---|---|---|---|---|---|
| Structure d'un programme (`base.programme`) | village | hello : classe HelloWorld, méthode main, javac/java, bytecode ; annale Q1 | 1 | 1 | 1 | 1 | 1 | ✔ |
| Afficher du texte (`base.affichage`) | village | hello : System.out.println, IO.println (Java 25), System.err.println | 2 | 1 | 1 | 1 | 0 | ✔ |
| Variables et affectation (`base.variables`) | village | hello : int len = args.length, String name ; point : var | 1 | 1 | 1 | 1 | 0 | ✔ |
| Expressions et opérateurs (`base.expressions`) | village | hello : calcul de l'âge (année courante − naissance) ; complément | 2 | 1 | 1 | 1 | 0 | ✔ |
| Types primitifs et limites (`base.types`) | village | annale : Q7 (valeurs d'un byte), Q10 (plus grande factorielle sur int / long) | 3 | 1 | 2 | 1 | 1 | ✔ |
| Arguments de la ligne de commande (`base.args`) | village | hello : java HelloWorld toto 2000, args.length, args[i] | 1 | 1 | 2 | 1 | 0 | ✔ |
| Lire au clavier et convertir (`base.lecture`) | village | hello : IO.readln, Integer.parseInt ; median : Double.parseDouble(IO.readln(...)) | 1 | 2 | 1 | 1 | 0 | ✔ |
| Conditions if / else (`cond.if`) | foret | hello : structure conditionnelle if/else ; année bissextile | 1 | 3 | 1 | 1 | 0 | ✔ |
| Comparer des String (equals vs ==) (`cond.equals`) | foret | annale Q8 (==, equals, hashCode) ; point : égalité par contenu | 2 | 1 | 1 | 1 | 0 | ✔ |
| switch (`cond.switch`) | foret | address : switch (elements[0]) pour créer un ContactSpecifier | 2 | 1 | 1 | 1 | 0 | ✔ |
| Capturer une exception (try / catch) (`exc.trycatch`) | foret | hello : try { Integer.parseInt } catch (NumberFormatException e), redemander tant que non valide | 2 | 2 | 1 | 1 | 0 | ✔ |
| Boucle while (`loop.while`) | mines | median : while (true) … break sur la sentinelle −1 ; hello : redemander tant que invalide | 2 | 1 | 1 | 1 | 1 | ✔ |
| Boucle for et accumulation (`loop.for`) | mines | collections : for (int i = 0; i < size; i++) ; annale Q10 (factorielle) | 3 | 3 | 1 | 1 | 0 | ✔ |
| Parcourir une chaîne (`str.parcours`) | mines | hello : isPalindromic avec length() et charAt(i) ; annale : domaines palindromes | 2 | 1 | 2 | 1 | 1 | ✔ |
| Tableaux (`arr.base`) | mines | median : double[] values = new double[10] ; hello : args[i] | 1 | 1 | 2 | 1 | 0 | ✔ |
| Algorithmes sur tableaux (`arr.algos`) | mines | median : Arrays.copyOf, Arrays.sort(values, 0, i), médiane pair/impair | 1 | 2 | 1 | 1 | 0 | ✔ |
| Écrire des méthodes (`meth.static`) | mines | collections : public static Map<String,Integer> buildBag(List<String> l) ; hello : méthodes age(), isPalindromic() | 1 | 2 | 2 | 1 | 0 | ✔ |
| Classes, champs, constructeurs (`obj.classe`) | artisans | point : classe Point avec x, y, constructeur | 3 | 1 | 1 | 1 | 1 | ✔ |
| Encapsulation : private, getters, final (`obj.encapsulation`) | artisans | point : final, private, getters/setters, mutable vs immuable | 1 | 2 | 2 | 1 | 1 | ✔ |
| Méthodes d'instance (`obj.methodes`) | artisans | point : distanceTo(Point p), distanceToOrigin() | 1 | 2 | 2 | 1 | 0 | ✔ |
| toString (`obj.tostring`) | artisans | point : String toString() redéfinie (Object affiche nom@adresse) | 1 | 2 | 2 | 1 | 0 | ✔ |
| equals et hashCode (`obj.equals`) | artisans | point : equals(Object obj), hashCode (Objects.hash) ; annale Q8 | 2 | 1 | 2 | 1 | 1 | ✔ |
| Membres static et constantes (`obj.static`) | artisans | point : public static final Point ORIGIN | 2 | 1 | 2 | 1 | 0 | ✔ |
| Records (`obj.record`) | artisans | hello : record Person(String name, int birthYear) + age(), isPalindromic(), bornOnLeapYear() ; point : record Point | 1 | 1 | 2 | 1 | 0 | ✔ |
| Lever des exceptions (`obj.exceptions`) | artisans | annale : GasPump.takeGas lève une exception si la cuve est insuffisante | 1 | 2 | 3 | 1 | 1 | ✔ |
| Visibilité et paquetages (`obj.visibilite`) | artisans | annale Q6 ; GasPump : changement de prix réservé au paquetage | 1 | 2 | 1 | 1 | 1 | ✔ |
| Aléatoire et fabrique d'objets (`obj.random`) | artisans | point : PointFactory.build() avec Random, estimation de π par fléchettes | 1 | 1 | 1 | 1 | 0 | ✔ |
| Interfaces (`inh.interface`) | tour | point : interface Shape (area, perimeter) ; address : interface ContactSpecifier | 1 | 1 | 1 | 1 | 1 | ✔ |
| Méthodes default et static d'interface (`inh.default`) | tour | point : Polygon.vertexNumber() default, perimeter() par les sommets ; address : ContactSpecifier.create(String) | 1 | 1 | 2 | 1 | 0 | ✔ |
| Polymorphisme (`inh.polymorphisme`) | tour | point : hiérarchie Shape ; annale : animaux qui marchent / nagent / volent | 1 | 1 | 1 | 1 | 1 | ✔ |
| Héritage, redéfinition, surcharge (`inh.heritage`) | tour | annale Q2, Q3 ; point : débat interface / class / abstract class | 2 | 1 | 1 | 1 | 1 | ✔ |
| Comparable et compareTo (`inh.comparable`) | tour | point : Point implements Comparable<Point> pour l'égalité de triangles ; postier : trier les envois par masse | 1 | 1 | 1 | 1 | 0 | ✔ |
| Concevoir une hiérarchie (`inh.modelisation`) | tour | annale : Shark, Human, Pigeon, Duck (walk/swim/fly) ; postier : Mail et Parcel | 1 | 1 | 1 | 1 | 1 | ✔ |
| ArrayList (`col.arraylist`) | bibliotheque | collections : intérêt d'ArrayList vs tableau ; annale Q4 | 1 | 1 | 2 | 1 | 0 | ✔ |
| ArrayList ou LinkedList ? (`col.linkedlist`) | bibliotheque | collections : SpeedList ; annale Q4–Q5 | 2 | 1 | 1 | 1 | 1 | ✔ |
| Ensembles : HashSet, TreeSet (`col.set`) | bibliotheque | collections : HashSet vs TreeSet ; annale : domaine dupliqué | 1 | 1 | 1 | 1 | 1 | ✔ |
| Tables associatives : HashMap (`col.map`) | bibliotheque | collections : buildBag (sac de mots) ; address : HashMap<Person, ContactSpecifier> ; celebs : HashMap<String, Celebrity> | 1 | 1 | 1 | 1 | 1 | ✔ |
| Trier avec un Comparator (`col.tri`) | bibliotheque | celebs : trier par popularité décroissante ; collections : mots par fréquence décroissante | 1 | 1 | 1 | 2 | 0 | ✔ |
| Découper et analyser du texte (`str.split`) | bibliotheque | address : s.split(":") ; celebs : fromString(String) ; collections : découpage en mots | 1 | 1 | 1 | 1 | 0 | ✔ |
| Boucle de commandes (REPL) (`proj.repl`) | citadelle | jalist : shell de listes de chaînes, variables $, commandes empty_list, len, append… | 1 | 1 | 1 | 1 | 0 | ✔ |
| Architecture évolutive (`proj.architecture`) | citadelle | jalist : « mettre en œuvre une architecture orientée objet évolutive » | 1 | 1 | 1 | 1 | 0 | ✔ |

**Parcours complet** (≥ 1 activité guidée/entraînement, autonome, transfert et réactivation) : 41/41.

Les énoncés sont originaux ; le champ « Provenance » cite l'objectif du support ESIEE dont la compétence est issue, sans en reproduire le texte.
